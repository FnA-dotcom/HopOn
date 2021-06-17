package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@SuppressWarnings("Duplicates")
public class AddChild extends HttpServlet {
    String ScreenNo = "0";
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;
    CallableStatement cStmt;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.handleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.handleRequest(request, response);
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = this.getServletContext();
        String UserId;
        int CityIndex = 0;
        try {
            HttpSession session = request.getSession(true);
            if (session.getAttribute("UserId") == null || session.getAttribute("UserId").equals("")) {
                out.println(" <center><table cellpadding=3 cellspacing=2><tr><td bgcolor=\"#FFFFFF\"> " +
                        " <font color=green face=arial><b>Your Session Has Been Expired</b></font> " +
                        " </td></tr></table> " +
                        " <p> " +
                        " <font face=arial size=+1><b><a href=/HopOn/index.html target=_top> Return to Login Page " +
                        " </a></b></font>" +
                        " <br><font face=arial size=-2>(You will need to sign in again.)</font><br> " +
                        " </center> ");
                out.flush();
                out.close();
                session.removeAttribute("UserId");
                return;
            }
            UserId = session.getAttribute("UserId").toString();
            CityIndex = Integer.parseInt(session.getAttribute("CityIndex").toString());
            try {
                boolean ValidSession = Supportive.checkSession(out, request);
                if (!ValidSession) {
                    out.flush();
                    out.close();
                    return;
                }
                if (UserId == "") {
                    out.println("<font size=\"3\" face=\"Calibri\">Your session has been expired, please login again.</font>");
                    out.flush();
                    out.close();
                    return;
                }

                conn = Supportive.getMysqlConn(this.getServletContext());
                if (conn == null) {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("Error", "Unable to connect. Our team is looking into it!");
                    Parser.GenerateHtml(out, String.valueOf(Supportive.GetHtmlPath(this.getServletContext())) + "index.html");
                    return;
                }
            } catch (Exception var14) {
                conn = null;
                out.println("Exception excp conn: " + var14.getMessage());
            }
/*            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }*/

            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }

            UtilityHelper helper = new UtilityHelper();

            Action = request.getParameter("ActionID");
            switch (Action) {
                case "GetInfo":
                    GetInformation(request, out, conn, context, UserId, CityIndex, helper);
                    break;
                case "SaveChild":
                    this.SaveChild(request, out, conn, context, UserId, helper);
                    break;
                default:
                    out.println("Under Development ... " + Action);
                    break;
            }

            conn.close();
        } catch (Exception var15) {
            out.println("Exception in main... " + var15.getMessage());
            out.flush();
            out.close();
            return;
        }

        out.flush();
        out.close();
    }

    private void GetInformation(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";

        StringBuilder SchoolList;
        StringBuilder InstructionList;
        StringBuilder EquipmentList;
        StringBuilder GradeList;
        StringBuilder AllergyList;

        try {

            SchoolList = helper.SchoolList(request, conn, servletContext);
            InstructionList = helper.InstructionsList(request, conn, servletContext);
            EquipmentList = helper.EquipmentsList(request, conn, servletContext);
            GradeList = helper.GradeList(request, conn, servletContext);
            AllergyList = helper.AllergyList(request, conn, servletContext);


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("SchoolNameList", SchoolList.toString());
            Parser.SetField("InstructionList", InstructionList.toString());
            Parser.SetField("EquipmentList", EquipmentList.toString());
            Parser.SetField("GradeList", GradeList.toString());
            Parser.SetField("AllergyList", AllergyList.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddChild.html");
        } catch (Exception Ex) {
            Supportive.DumException("AddChild", "First Method -- GetInfo--MAIN", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddChild");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void SaveChild(HttpServletRequest req, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        Query = "";
        cStmt = null;
        rset = null;
        int i, j, k = 0;
        ResultSet rset1;
        String Query1;

        String ChildInfoData = req.getParameter("ChildInfoData").trim();
        int hdn_ChildCounter = (req.getParameter("hdn_ChildCounter").trim() == null ? 0 : Integer.parseInt(req.getParameter("hdn_ChildCounter").trim()));

//        out.println("ChildInfoData " + ChildInfoData + "<br>");
//        out.println("hdn_ChildCounter " + hdn_ChildCounter + "<br>");

        String[] myInfo;
        myInfo = new String[0];
        myInfo = ChildInfoData.split("\\^");
        String ChildData[][] = new String[hdn_ChildCounter][20];
        i = j = k = 0;
        for (i = 1; i < myInfo.length; i++) {
            if (myInfo[i].length() <= 0)
                continue;

            if (myInfo[i].substring(myInfo[i].indexOf("=") + 1).equals("^"))
                ChildData[k][j] = "-";
            else
                ChildData[k][j] = myInfo[i].substring(myInfo[i].indexOf("=") + 1);

//            out.println("ARR VAL ["+k+"] ["+j+"] --> " + ChildData[k][j] + " <br> ");

            j++;
            if (j > 19) {
                j = 0;
                k++;
            }
        }

        String CurrDate = "";
        Query = "{CALL CurrentDate()}";
        try {
            cStmt = conn.prepareCall(Query);
            rset = cStmt.executeQuery();
            if (rset.next())
                CurrDate = rset.getString(1).trim();
            rset.close();
            cStmt.close();
        } catch (SQLException ex) {
            Supportive.doLog(this.getServletContext(), "AddChild--Record Insertion-01", ex.getMessage(), ex);
            out.flush();
            out.close();
        }


        try {
            int SystemUserIndex = 0;
            int ParentIndex = 0;

            SystemUserIndex = helper.UserIndex(req, UserId, conn, servletContext);
            ParentIndex = helper.ParentIndex(req, SystemUserIndex, conn, servletContext);

            for (i = 0; i < hdn_ChildCounter; i++) {
                if (ChildData[i][0] == null || ChildData[i][0].length() < 1 || ChildData[i][0].isEmpty())
                    continue;


                String Month = ChildData[i][1].substring(0, 2);
                String Day = ChildData[i][1].substring(3, 5);
                String Year = ChildData[i][1].substring(6, 10);
                String DateOfBirth = Year + "-" + Month + "-" + Day + " 00:00:00";

/*                out.println("VAL 1 Name --> " + ChildData[i][0] + "<br>");
                out.println("VAL 2 DOB  --> " + ChildData[i][1] + "<br>");//12/31/2002
                out.println("VAL 3 Age --> " + ChildData[i][2] + "<br>");
                out.println("VAL 4 Grade --> " + ChildData[i][3] + "<br>");
                out.println("VAL 5 Equipment --> " + ChildData[i][4] + "<br>");
                out.println("VAL 6 Allergy --> " + ChildData[i][5] + "<br>");
                out.println("VAL 7 Description --> " + ChildData[i][6] + "<br>");
                out.println("VAL 8 Instruction --> " + ChildData[i][7] + "<br>");
                out.println("VAL 9 EquipmentIndex --> " + ChildData[i][8] + "<br>");
                out.println("VAL 10 AllergyIndex --> " + ChildData[i][9] + "<br>");
                out.println("VAL 11 InstructionsIndex --> " + ChildData[i][10] + "<br>");
                out.println("VAL 12 GradeIndex --> " + ChildData[i][11] + "<br>");
                out.println("VAL 13 School --> " + ChildData[i][12] + "<br>");
                out.println("VAL 14 _Child_Phone --> " + ChildData[i][13] + "<br>");
                out.println("VAL 14 _txtPickUpLocationLat --> " + ChildData[i][14] + "<br>");
                out.println("VAL 15 _txtPickUpLocationLon --> " + ChildData[i][15] + "<br>");
                out.println("VAL 16 _txtDropOffLocationLat --> " + ChildData[i][16] + "<br>");
                out.println("VAL 17 _txtDropOffLocationLon --> " + ChildData[i][17] + "<br>");
                out.println("VAL 18 _txtPickUpTime --> " + ChildData[i][18] + "<br>");
                out.println("VAL 19 txtDropOffTime --> " + ChildData[i][19] + "<br>");*/


                Query = "{CALL SP_SAVE_ChildInfo(?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?,?,?,?,?,?)}";
                cStmt = conn.prepareCall(Query);
                cStmt.setString(1, ChildData[i][0]);//Name
                cStmt.setString(2, DateOfBirth);//DOB
                cStmt.setInt(3, Integer.parseInt(ChildData[i][2]));//Age
                cStmt.setInt(4, Integer.parseInt(ChildData[i][11]));//GradeIndex
                cStmt.setInt(5, Integer.parseInt(ChildData[i][8]));//EquipmentIndex
                cStmt.setInt(6, Integer.parseInt(ChildData[i][9]));//AllergyIndex
                cStmt.setString(7, ChildData[i][6]);//Description
                cStmt.setInt(8, Integer.parseInt(ChildData[i][10]));//InstructionsIndex
                cStmt.setInt(9, Integer.parseInt(ChildData[i][12]));//SchoolIndex
                cStmt.setInt(10, 0);//Status
                cStmt.setString(11, CurrDate);//CreatedDate
                cStmt.setInt(12, ParentIndex);//ParentIndex
                cStmt.setString(13, UserId);//UserId
                cStmt.setString(14, ChildData[i][13]);//Phone

                cStmt.setString(15, ChildData[i][14]);//PickUpLocationLat
                cStmt.setString(16, ChildData[i][15]);//PickUpLocationLon
                cStmt.setString(17, ChildData[i][16]);//DropOffLocationLat
                cStmt.setString(18, ChildData[i][17]);//DropOffLocationLon
                cStmt.setString(19, ChildData[i][18]);//PickUpTime
                cStmt.setString(20, ChildData[i][19]);//DropOffTime

                rset = cStmt.executeQuery();
                rset.close();
                cStmt.close();

//                out.println("SUCCESS ");
//                out.println("VAL of I --> " +  i  + "<br>");
            }

            Query = "SELECT Id,SchoolIndex FROM Childrens WHERE ParentIndex = " + ParentIndex + " AND Status = 0 AND " +
                    " Id NOT IN (SELECT x.StudentIndex FROM BindSchoolStudent x WHERE x.ParentIndex = " + ParentIndex + " )";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Query1 = "{CALL SP_SAVE_BindageSchoolStudent(?,?,?,?,?,?)}";
                cStmt = conn.prepareCall(Query1);

                cStmt.setInt(1, rset.getInt(1));
                cStmt.setInt(2, rset.getInt(2));
                cStmt.setInt(3, 0);
                cStmt.setString(4, CurrDate);//CreatedDate
                cStmt.setString(5, UserId);//UserId
                cStmt.setInt(6, ParentIndex);//ParentIndex

                rset1 = cStmt.executeQuery();
                rset1.close();
                cStmt.close();
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(req);
            Parser.SetField("FormName", "AddChild");
            Parser.SetField("ActionID", "GetInfo");
            Parser.SetField("Message", "Child Information Has been entered Successfully!!");
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
        } catch (Exception Ex) {
            out.println("Error!");
            Supportive.doLog(this.getServletContext(), "AddChild -- Record Insertion-02", Ex.getMessage(), Ex);
            out.close();
            out.flush();
        }

    }
}
