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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")

public class ShowHoliday extends HttpServlet {
    String ScreenNo = "0";
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;

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

            Action = request.getParameter("ActionID");
            UtilityHelper helper = new UtilityHelper();
            switch (Action) {
                case "GetInfo":
                    GetInformation(request, out, conn, context, UserId, CityIndex);
                    break;
                case "showReport":
                    showReport(request, out, conn, context, UserId, CityIndex);
                    break;
                case "deleteRecord":
                    DeleteRecord(request, out, conn, context, UserId, CityIndex, helper);
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

    private void GetInformation(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex) {
        stmt = null;
        rset = null;
        Query = "";

        try {
            Parsehtm var20 = new Parsehtm(request);
            var20.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowHoliday.html");
        } catch (Exception var19) {
            Supportive.DumException("ShowHoliday", "First Method -- GetInfo--MAIN", request, var19, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ShowHoliday");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void showReport(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex) {
        Query = "";
        stmt = null;
        rset = null;

        String StartDate = request.getParameter("FromDate");

        String SMonth = StartDate.substring(0, 2);
        String SDay = StartDate.substring(3, 5);
        String SYear = StartDate.substring(6, 10);

        String EndDate = request.getParameter("ToDate");

        String EMonth = EndDate.substring(0, 2);
        String EDay = EndDate.substring(3, 5);
        String EYear = EndDate.substring(6, 10);

        StartDate = SYear + "-" + SMonth + "-" + SDay;
        EndDate = EYear + "-" + EMonth + "-" + EDay;
        int SrlNo = 1;
        int statusMarked = 0;
        StringBuilder ShowHoliday = new StringBuilder();

        Query = "SELECT " +
                "CASE  " +
                "WHEN a.HolidayType = 1 THEN 'National Holiday'\n" +
                "WHEN a.HolidayType = 3 THEN 'Personal Holiday'\n" +
                "ELSE 'No Day' END AS HolidayType, DATE_FORMAT(a.StartDate,'%d-%b-%Y') As StartDate,\n" +
                "CASE " +
                "WHEN a.CheckBoxVal = 0 THEN 'No'\n" +
                "WHEN a.CheckBoxVal = 1 THEN 'Yes'\n" +
                "ELSE 'Not Selected!' END As OneDay, IFNULL(b.FullName,'No Driver Selected') AS Driver,\n" +
                "CASE " +
                "WHEN a.EndHoliday = '0000-00-00 00:00:00' THEN 'Date not Selected'\n" +
                "ELSE DATE_FORMAT(a.EndHoliday,'%d-%b-%Y') END AS EndHoliday,a.Comments,\n" +
                "(CASE WHEN a.Status=0 THEN 'Active' ELSE 'Inactive' END) As Status, a.Id, a.Status," +
                "DATE_FORMAT(a.CreatedDate,'%d-%b-%Y') " +
                " FROM Holidays a " +
                " LEFT JOIN Drivers b ON a.DriverIndex = b.Id " +
                " WHERE DATE_FORMAT(a.CreatedDate,'%Y-%m-%d') BETWEEN '" + StartDate + "' AND '" + EndDate + "' ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                statusMarked = rset.getInt(9);
                ShowHoliday.append("<tr>");
                ShowHoliday.append("<td width=02%>" + SrlNo + "</td>");
                ShowHoliday.append("<td width=08%>" + rset.getString(1) + "</td>");//HolidayType
                ShowHoliday.append("<td width=08%>" + rset.getString(2) + "</td>");//StartDate
                ShowHoliday.append("<td width=08%>" + rset.getString(3) + "</td>");//OneDay
                ShowHoliday.append("<td width=08%>" + rset.getString(4) + "</td>");//Driver
                ShowHoliday.append("<td width=08%>" + rset.getString(5) + "</td>");//EndHoliday
                ShowHoliday.append("<td width=08%>" + rset.getString(6) + "</td>");//Comments
                ShowHoliday.append("<td width=05%>" + rset.getString(10) + "</td>");//CreatedDate
                if (rset.getInt(9) == 0)
                    ShowHoliday.append("<td width=05%> <span class=\"label label-primary\">" + rset.getString(7) + "</span></td>");//Status
                else
                    ShowHoliday.append("<td width=05%> <span class=\"label label-danger\">" + rset.getString(7) + "</span></td>");//Status
                if (statusMarked == 0) {
                    ShowHoliday.append("<td width=15%> ");
                    ShowHoliday.append("<a href=\"#\" onclick=\"return false;\" class=\"btn btn-success btn-xs\"><i class=\"fa fa-pencil\"></i> Edit </a>&nbsp;&nbsp;");
                    ShowHoliday.append("<button class=\"btn btn-danger btn-xs mylink\" value=" + rset.getInt(8) + " target=NewFrame1> <font color = \"FFFFFF\"> <i class=\"fa fa-trash-o\"></i> [Delete] </font></button>");
                    ShowHoliday.append("</td>");
                } else if (statusMarked == 1) {
                    ShowHoliday.append("<td width=15%> ");
                    ShowHoliday.append("<a href=\"#\" onclick=\"return false;\" class=\"btn btn-success btn-xs\"><i class=\"fa fa-pencil\"></i> Edit </a>&nbsp;&nbsp;");
                    ShowHoliday.append("<button class=\"btn btn-info btn-xs activateLink\" value=" + rset.getInt(8) + " target=NewFrame1> <font color = \"FFFFFF\"> <i class=\"fa fa-exchange\"></i> [Active] </font></button>");
                    ShowHoliday.append("</td>");
                }

                ShowHoliday.append("</tr>");
                ++SrlNo;
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("ShowHoliday", ShowHoliday.toString());
            Parser.SetField("FromDate", StartDate);
            Parser.SetField("ToDate", EndDate);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/showHolidayReport.html");
            out.flush();
            out.close();
        } catch (Exception Ex) {
            Supportive.DumException("ShowHoliday", "Second Method -- showReport ", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ShowHoliday");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void DeleteRecord(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, UtilityHelper helper) {
        Query = "";
        stmt = null;
        rset = null;
        Query = request.getParameter("HolidayIndex").trim();
        int HolidayIndex = Integer.parseInt(Query);
        int Status = 0;
        int Result = 0;

        Query = "Select Id,Status from Holidays where Id=" + HolidayIndex;

        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Status = rset.getInt(2);
            }
            rset.close();
            stmt.close();
        } catch (Exception var35) {
            out.flush();
            out.close();
            return;
        }

        //Making it InActive
        if (Status == 0) {
            Result = helper.DeleteHolidayInfo(request, HolidayIndex, 1, conn, servletContext);
        } else if (Status == 1) {
            Result = helper.DeleteHolidayInfo(request, HolidayIndex, 0, conn, servletContext);
        }
        out.println(Result);
    }
}
