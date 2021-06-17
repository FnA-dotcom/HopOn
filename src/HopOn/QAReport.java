package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class QAReport extends HttpServlet {
    private String ScreenNo = "24";

    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HandleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HandleRequest(request, response);
    }

    public void HandleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        String UserId = "";
        int CityIndex = 0;
        int isAdmin = 0;

        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
//        String connect_string = supp.GetConnectString();

        try {
            ServletContext context = null;
            context = getServletContext();

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
                UserId = Supportive.GetCookie("UserId", request);
                CityIndex = Integer.parseInt(session.getAttribute("CityIndex").toString());
                isAdmin = Integer.parseInt(session.getAttribute("isAdmin").toString());
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

//                Class.forName("com.mysql.jdbc.Driver");
//                conn = DriverManager.getConnection(connect_string);
            } catch (Exception excp) {
                conn = null;
                out.println("Exception excp conn: " + excp.getMessage());
            }
            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }
            Action = request.getParameter("ActionID");

            if (Action.equals("GETINPUT")) {
                this.InputReport(request, response, out, conn, context, CityIndex, isAdmin);
            } else if (Action.equals("ShowReport")) {
                ShowRecords(request, out, conn, response, context);
            } else if (Action.trim().equals("ViewImage")) {
                ViewImage(request, response, out);
            } else {
                out.println("Under Development ... " + Action);
            }
            conn.close();
        } catch (Exception e) {
            out.println("Exception in main... " + e.getMessage());
            out.flush();
            out.close();
            return;
        }
        out.flush();
        out.close();
    }

    private void InputReport(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext, int cityIndex, int isAdmin) {
        this.stmt = null;
        this.rset = null;
        this.Query = "";
        StringBuilder ComplainNumber = new StringBuilder();
        try {

            //Old Logic
//            Query = " SELECT Id,ComplainNumber FROM CustomerData WHERE " +
//                    " Id IN (SELECT ComplaintId FROM Assignment)" +
//                    " ORDER BY Id";

            //new Logic
            Query = "SELECT Id,CONCAT(ComplainNumber , ' - ' , IFNULL(Insurance,'-'), ' - ' , IFNULL(ChesisNo,'-')) FROM CustomerData WHERE \n" +
                    "Id IN (SELECT ComplaintId FROM Assignment WHERE ComplaintStatus=6)\n" +
                    "ORDER BY AssignDate DESC";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            ComplainNumber.append("<option value=0 Selected Disabled> Select Complain Number </option>");
            while (rset.next()) {
                ComplainNumber.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            stmt.close();
            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("ComplainNumber", ComplainNumber.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/QAForm.html");
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "Error Record 001", e.getMessage(), e);
        }
    }

    private void ShowRecords(HttpServletRequest request, PrintWriter out, Connection conn, HttpServletResponse response, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        String Longitude = "";
        String Latitude = "";
        String TechName = "";
        int ServerIndex = 0;
        //For Switching Image Directory Just Change below variable value
        //Live Server
        ServerIndex = 1;
        //Test Server
        //ServerIndex = 2;

        Query = request.getParameter("Complain").trim();
        int Complain = Integer.parseInt(Query);

/*        String DatePicker = request.getParameter("daterange");
        String SMonth = DatePicker.substring(0, 2);
        String SDay = DatePicker.substring(3, 5);
        String SYear = DatePicker.substring(6, 10);

        String EDay = DatePicker.substring(16, 18);
        String EMonth = DatePicker.substring(12, 15).trim();
        String EYear = DatePicker.substring(19);

        String StartDate = SYear + "-" + SDay + "-" + SMonth + " 00:00:00";
        String EndDate = EYear + "-" + EDay + "-" + EMonth + " 23:59:59";*/
        String QAComplainNumber = "";

        StringBuilder ComplainDetail1 = new StringBuilder();
        StringBuilder ComplainDetail2 = new StringBuilder();
        StringBuilder Table1 = new StringBuilder();
        StringBuilder Table2 = new StringBuilder();
        StringBuilder ImageList = new StringBuilder();
        try {
            Query = "SELECT ComplainNumber FROM CustomerData WHERE Id=" + Complain;
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next())
                    QAComplainNumber = rset.getString(1).trim();
                rset.close();
                stmt.close();
            } catch (Exception Ex) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "QA Form - Show Report 00", Ex.getMessage(), Ex);
            }

/*            Query = " SELECT a.Ans, a.Ans1, a.Ans2, a.Ans3, a.Ans4,a.Ans5,b.Latitude,b.Longtitude "
                    + "FROM QAForm a " +
                    " STRAIGHT_JOIN Assignment b ON b.ComplaintId=" + Complain + " "
                    + "WHERE a.ComplainNumber='" + QAComplainNumber + "' "
                    + "AND a.CreatedDate=(SELECT max(CreatedDate) FROM QAForm WHERE ComplainNumber='" + QAComplainNumber + "' ) "
                    + "Order by a.CreatedDate DESC limit 1";*/
            Query = " SELECT a.Ans, a.Ans1, a.Ans2, a.Ans3, a.Ans4,a.Ans5,b.Latitude,b.Longtitude "
                    + "FROM QAForm a " +
                    " STRAIGHT_JOIN Assignment b ON b.ComplaintId=" + Complain + " "
                    + "WHERE a.ComplainNumber='" + QAComplainNumber + "' "
                    + "AND a.CreatedDate=(SELECT max(CreatedDate) FROM QAForm WHERE ComplainNumber='" + QAComplainNumber + "' ) "
                    + "Order by a.CreatedDate limit 1";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {

                Latitude = rset.getString(7);
                Longitude = rset.getString(8);

                String URL = "http://maps.google.com?q=" + Latitude + "," + Longitude;

                Table1.append("<tr>");
                Table1.append("<td align=left>" + rset.getString(1) + "</td>\n");// Ans
                Table1.append("<td align=left>" + rset.getString(2) + "</td>\n");// Ans1
                Table1.append("<td align=left>" + rset.getString(3) + "</td>\n");// Ans2
                Table1.append("<td align=left>" + rset.getString(4) + "</td>\n");// Ans3
                Table1.append("<td align=left>" + rset.getString(5) + "</td>\n");// Ans4
                Table1.append("<td align=left>" + rset.getString(6) + "</td>\n");// Ans5
                Table1.append("<td><a href=\"" + URL + "\" target=\"_blank\"><i class=\"fa fa-share\"></i>[View]</a></td>\n");
                Table1.append("</tr>");
            }
            rset.close();
            stmt.close();

/*            Query = "SELECT Pic1,Pic2,Pic3,Pic4 " +
                    " FROM QAForm WHERE " +
                    " ComplainNumber='" + QAComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM QAForm WHERE ComplainNumber='" + QAComplainNumber + "' ) " +
                    " ORDER BY CreatedDate DESC LIMIT 1";*/
            Query = "SELECT Pic1,Pic2,Pic3,Pic4 " +
                    " FROM QAForm WHERE " +
                    " ComplainNumber='" + QAComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM QAForm WHERE ComplainNumber='" + QAComplainNumber + "' ) " +
                    " ORDER BY CreatedDate LIMIT 1";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                ImageList.append("<tr>");
                if (!rset.getString(1).equals("-"))
                    ImageList.append("<td align=left> <a href=\"JavaScript:newPopup('/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.QAReport?ActionID=ViewImage&FileName=" + rset.getString(1) + "');\"><i class=\"fa fa-share\">[View]</i></a></td>\n");// Shop Pic

                else
                    ImageList.append("<td align=left>No Image Taken</td>\n");
                if (!rset.getString(2).equals("-"))
                    ImageList.append("<td align=left> <a href=\"JavaScript:newPopup('/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.QAReport?ActionID=ViewImage&FileName=" + rset.getString(2) + "');\"><i class=\"fa fa-share\">[View]</i></a></td>\n");// Shop Pic
                else
                    ImageList.append("<td align=left>No Image Taken</td>\n");
                if (!rset.getString(3).equals("-"))
                    ImageList.append("<td align=left> <a href=\"JavaScript:newPopup('/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.QAReport?ActionID=ViewImage&FileName=" + rset.getString(3) + "');\"><i class=\"fa fa-share\">[View]</i></a></td>\n");// Shop Pic
                else
                    ImageList.append("<td align=left>No Image Taken</td>\n");
                if (!rset.getString(4).equals("-"))
                    ImageList.append("<td align=left> <a href=\"JavaScript:newPopup('/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.QAReport?ActionID=ViewImage&FileName=" + rset.getString(4) + "');\"><i class=\"fa fa-share\">[View]</i></a></td>\n");// Shop Pic
                else
                    ImageList.append("<td align=left>No Image Taken</td>\n");
                ImageList.append("</tr>");
            }
            rset.close();
            stmt.close();

            Query = "SELECT  a.RegistrationNum,a.CustName,a.CellNo,ifnull(a.PhoneNo,'-'),a.Make,a.Model,a.Color,a.ChesisNo," +
                    "b.JobType,a.Address,IFNULL(a.DeviceNo,'-'),IFNULL(a.Insurance,'-'),a.ComplainNumber,d.UserName " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id " +
                    " STRAIGHT_JOIN Assignment c ON a.Id=c.ComplaintId " +
                    " AND c.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = c.ComplaintId) " +
                    " STRAIGHT_JOIN  MobileUsers d ON c.UserId=d.Id " +
                    " WHERE a.ComplainNumber='" + QAComplainNumber + "' ";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                ComplainDetail1.append("<tr>");
                ComplainDetail1.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                ComplainDetail1.append("<td align=left>" + rset.getString(2) + "</td>\n");// CustName
                ComplainDetail1.append("<td align=left>" + rset.getString(3) + "</td>\n");// CellNo
                ComplainDetail1.append("<td align=left>" + rset.getString(4) + "</td>\n");// PhoneNo
                ComplainDetail1.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                ComplainDetail1.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                ComplainDetail1.append("<td align=left>" + rset.getString(7) + "</td>\n");// Color
                ComplainDetail1.append("<td align=left>" + rset.getString(8) + "</td>\n");// ChesisNo
                ComplainDetail1.append("</tr>");
                TechName = rset.getString(14).trim();
            }
            rset.close();
            stmt.close();

            Query = "SELECT  b.JobType,a.Address,IFNULL(a.DeviceNo,'-'),IFNULL(a.Insurance,'-') " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id " +
                    " STRAIGHT_JOIN Assignment c ON a.Id=c.ComplaintId " +
                    " AND c.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = c.ComplaintId) " +
                    " STRAIGHT_JOIN  MobileUsers d ON c.UserId=d.Id " +
                    " WHERE a.ComplainNumber='" + QAComplainNumber + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                ComplainDetail2.append("<tr>");
                ComplainDetail2.append("<td align=left>" + rset.getString(1) + "</td>\n");// JobType
                ComplainDetail2.append("<td align=left>" + rset.getString(2) + "</td>\n");// Address
                ComplainDetail2.append("<td align=left>" + rset.getString(3) + "</td>\n");// DeviceNo
                ComplainDetail2.append("<td align=left>" + rset.getString(4) + "</td>\n");// Insurance
                ComplainDetail2.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Table1", Table1.toString());
            Parser.SetField("Table2", Table2.toString());
            Parser.SetField("ImageList", ImageList.toString());
            Parser.SetField("ComplainDetail1", ComplainDetail1.toString());
            Parser.SetField("ComplainDetail2", ComplainDetail2.toString());
            Parser.SetField("ComplainNumber", QAComplainNumber);
            Parser.SetField("TechName", TechName);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "/Reports/ShowRecords1.html");
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "QA Form - Show Report 01", e.getMessage(), e);
        }
    }

    private void ViewImage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String FileName = request.getParameter("FileName");

        try {
            //String path = "d:\\HTMLS\\EngroTSM\\pics\\" + FileName;
            String path = "/opt/Htmls/HopOn/pics/" + FileName;
            out.println(path);
            FileInputStream fin = new FileInputStream(new File(path));
            byte content[] = new byte[fin.available()];
            fin.read(content);
            fin.close();

            OutputStream os = response.getOutputStream();
            response.setContentType("image/jpeg");
            os.write(content);
            os.flush();
            os.close();

        } catch (Exception e) {
            out.println("Image not found..." + e.getMessage());
        }
    }

    private void UnAuthorize(AuthorizeUser authent, PrintWriter out, Connection conn) {
        out.println(AuthorizeUser.ReturnedErrMsg);
        out.flush();
        out.close();
        try {
            conn.close();
        } catch (Exception ex) {
        }
    }
}
