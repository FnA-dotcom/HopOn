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

public class ChildMatrix extends HttpServlet {
    String ScreenNo = "0";
    String Query;
    PreparedStatement pStmt;
    Statement stmt;
    ResultSet rset;
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
//        String connect_string = supp.GetConnectString();
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
                case "FetchChildData":
                    FetchChildData(request, out, conn, context, UserId, helper);
                    break;
                case "SaveMatrix":
                    this.SaveMatrix(request, out, conn, context, UserId, helper);
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
        StringBuilder ChildrenList;

        int SystemUserIndex;
        int ParentIndex;
        String ParentName;
        try {

            SchoolList = helper.SchoolList(request, conn, servletContext);
            SystemUserIndex = helper.UserIndex(request, UserId, conn, servletContext);
            ParentIndex = helper.ParentIndex(request, SystemUserIndex, conn, servletContext);
            ParentName = helper.SystemUserName(request, SystemUserIndex, conn, servletContext);
            ChildrenList = helper.ChildList(request, ParentIndex, conn, servletContext);

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("SchoolNameList", SchoolList.toString());
            Parser.SetField("ChildrenList", ChildrenList.toString());
            Parser.SetField("SystemUserIndex", String.valueOf(SystemUserIndex));
            Parser.SetField("ParentIndex", String.valueOf(ParentIndex));
            Parser.SetField("ParentName", String.valueOf(ParentName));
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/ChildMatrix.html");
        } catch (Exception Ex) {
            Supportive.DumException("ChildMatrix", "First Method -- GetInfo--MAIN", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ChildMatrix");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void FetchChildData(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";

        Query = request.getParameter("ChildIndex");
        int ChildIndex = Integer.parseInt(Query);

        String ChildName = "";
        String DOB = "";
        String Age = "";
        String Grade = "";
        String _Child_Allergy = "";
        int InstructionIndex = 0;
        String _Child_Instructions = "";
        int EquipmentIndex = 0;
        String _Child_Equipments = "";
        String Child_Description = "";
        int SchoolIndex = 0;
        String _Child_School = "";
        String ParentName = "";
        String NewAllergy = "";
        String NewGrade = "";

        int SystemUserIndex = 0;
        int ParentIndex = 0;

        try {
            if (ChildIndex > 0) {
                SystemUserIndex = helper.UserIndex(request, UserId, conn, servletContext);
                ParentIndex = helper.ParentIndex(request, SystemUserIndex, conn, servletContext);
                String[] childInfo = helper.getChildInfo(request, ParentIndex, ChildIndex, conn, servletContext);

                ChildName = childInfo[0];
                DOB = childInfo[1];
                Age = childInfo[2];
                Grade = childInfo[3];
                EquipmentIndex = Integer.parseInt(childInfo[4]);
                _Child_Equipments = childInfo[5];
                _Child_Allergy = childInfo[6];
                Child_Description = childInfo[7];
                InstructionIndex = Integer.parseInt(childInfo[8]);
                _Child_Instructions = childInfo[9];
                SchoolIndex = Integer.parseInt(childInfo[10]);
                _Child_School = childInfo[11];
                ParentName = childInfo[12];
                NewGrade = childInfo[13];
                NewAllergy = childInfo[14];

            } else {
                ChildName = "";
                DOB = "";
                Age = "";
                Grade = "";
                _Child_Allergy = "";
                _Child_Instructions = "";
                _Child_Equipments = "";
                Child_Description = "";
                ParentName = "";
            }

            out.println(ChildName + "|" + Grade + "|" + _Child_Allergy + "|" + _Child_Instructions + "|" + DOB + "|" + _Child_Equipments + "|" +
                    Child_Description + "|" + _Child_School + "|" + Age + "|" + NewGrade + "|" + NewAllergy + "|" + SchoolIndex);

        } catch (Exception Ex) {
            Supportive.DumException("ChildMatrix", "Second Method -- FetchChildData-- 002 ", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ChildMatrix");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }


    }

    private void SaveMatrix(HttpServletRequest req, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
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
        String ChildData[][] = new String[hdn_ChildCounter][7];
        i = j = k = 0;
        for (i = 1; i < myInfo.length; i++) {
            if (myInfo[i].length() <= 0)
                continue;

            if (myInfo[i].substring(myInfo[i].indexOf("=") + 1).equals("^"))
                ChildData[k][j] = "-";
            else
                ChildData[k][j] = myInfo[i].substring(myInfo[i].indexOf("=") + 1);

//            out.println("ARR VAL [" + k + "] [" + j + "] --> " + ChildData[k][j] + " <br> ");

            j++;
            if (j > 6) {
                j = 0;
                k++;
            }
        }

        String CurrDate = "";

        try {
            int SystemUserIndex = 0;
            int ParentIndex = 0;

            CurrDate = helper.getCurrDate(req, conn);
            SystemUserIndex = helper.UserIndex(req, UserId, conn, servletContext);
            ParentIndex = helper.ParentIndex(req, SystemUserIndex, conn, servletContext);

            for (i = 0; i < hdn_ChildCounter; i++) {
                if (ChildData[i][0] == null || ChildData[i][0].length() < 1 || ChildData[i][0].isEmpty())
                    continue;

                String DropOffLat = "";
                String DropOffLLon = "";
                Query1 = "SELECT Latitude,Longitude FROM School WHERE Id = " + ChildData[i][6];
                stmt = conn.createStatement();
                rset1 = stmt.executeQuery(Query1);
                if (rset1.next()) {
                    DropOffLat = rset1.getString(1).trim();
                    DropOffLLon = rset1.getString(2).trim();
                }
                rset1.close();
                stmt.close();

                Query = "{CALL SP_SAVE_Child_Scheduling(?,?,?,?,?,?,?,?,?,?,?,?)}";
                cStmt = conn.prepareCall(Query);

                cStmt.setInt(1, ParentIndex);
                cStmt.setInt(2, Integer.parseInt(ChildData[i][1])); //StudentIndex
                cStmt.setString(3, ChildData[i][2]);//PickupTime
                cStmt.setString(4, ChildData[i][3]);//DropOffTime
                cStmt.setString(5, ChildData[i][4]);//PickUpLatitude
                cStmt.setString(6, ChildData[i][5]);//PickUpLongitude
                cStmt.setString(7, DropOffLat);//DropOffLat
                cStmt.setString(8, DropOffLLon);//DropOffLon
                cStmt.setString(9, UserId);//UserId
                cStmt.setString(10, CurrDate);//CurrDate
                cStmt.setInt(11, 0);//Status
                cStmt.setString(12, ChildData[i][6]);//SchoolIndex

                rset = cStmt.executeQuery();
                rset.close();
                cStmt.close();
/*
                out.println("VAL 1 StudentName --> " + ChildData[i][0] + "<br>");
                out.println("VAL 2 StudentIndex  --> " + ChildData[i][1] + "<br>");//12/31/2002
                out.println("VAL 3 PickupTime --> " + ChildData[i][2] + "<br>");
                out.println("VAL 4 DropOffTime --> " + ChildData[i][3] + "<br>");
                out.println("VAL 5 PickUpLatitude --> " + ChildData[i][4] + "<br>");
                out.println("VAL 6 PickUpLongitude --> " + ChildData[i][5] + "<br>");
                out.println("VAL 7 SchoolIndex --> " + ChildData[i][6] + "<br>");*/
            }

            Parsehtm Parser = new Parsehtm(req);
            Parser.SetField("FormName", "ChildMatrix");
            Parser.SetField("ActionID", "GetInfo");
            Parser.SetField("Message", "Child Matrix Has been entered Successfully!!");
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
        } catch (Exception Ex) {
            out.println("Error!");
            Supportive.doLog(this.getServletContext(), "ChildMatrix -- Record Insertion-02", Ex.getMessage(), Ex);
            out.close();
            out.flush();
        }

    }
}
