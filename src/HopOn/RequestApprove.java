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

public class RequestApprove extends HttpServlet {
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

            Action = request.getParameter("ActionID");
            if (Action.equals("GetRequests")) {
                GetRequests(request, out, conn, context, UserId, CityIndex);
            } else if (Action.equals("DeleteRecord")) {
                DeleteRecord(request, out, conn, context, UserId, CityIndex);
            } else {
                out.println("Under Development ... " + Action);
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

    private void GetRequests(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex) {
        stmt = null;
        rset = null;
        int SrlNo = 1;
        Query = "";
        StringBuilder RequestApprovalsList = new StringBuilder();

        try {

            Query = "SELECT a.Id,a.UserType, b.UserType, a.AccountName, (CASE WHEN a.Status=0 THEN \'Active\' ELSE \'InActive\' END) as Status, " +
                    "DATE_FORMAT(a.CreatedDate,'%d-%b-%Y'),a.Status \n" +
                    "FROM RequestApprovals a\n" +
                    " STRAIGHT_JOIN UserType b ON a.UserType = b.Id";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                RequestApprovalsList.append("<tr>");
                RequestApprovalsList.append("<td width=02%>" + SrlNo + "</td>");
                RequestApprovalsList.append("<td width=10%> " + rset.getString(3) + "</td>");//UserType
                RequestApprovalsList.append("<td width=10%> " + rset.getString(4) + "</td>");//AccountName
                if (rset.getInt(7) == 0)
                    RequestApprovalsList.append("<td width=10%> <span class=\"label label-primary\">" + rset.getString(5) + "</span></td>");//Status
                else
                    RequestApprovalsList.append("<td width=10%> <span class=\"label label-danger\">" + rset.getString(5) + "</span></td>");//Status
                RequestApprovalsList.append("<td width=10%> " + rset.getString(6) + "</td>");//CreatedDate
                if (rset.getInt(7) == 0) {
                    RequestApprovalsList.append("<td width=15%> ");
                    RequestApprovalsList.append("<a href=\"#\" class=\"btn btn-primary btn-xs\"><i class=\"fa fa-folder\"></i> View </a>&nbsp;&nbsp;");
                    RequestApprovalsList.append("<a href=\"#\" class=\"btn btn-info btn-xs\"><i class=\"fa fa-pencil\"></i> Edit </a>&nbsp;&nbsp;");
                    //RequestApprovalsList.append("<a class=\"btn btn-danger btn-xs mylink\" value=" + rset.getInt(1) + " target=NewFrame1><i class=\"fa fa-trash-o\"></i> Delete </a>");
                    RequestApprovalsList.append("<button class=\"btn btn-danger btn-xs mylink\" value=" + rset.getInt(1) + " target=NewFrame1> <font color = \"FFFFFF\"> <i class=\"fa fa-trash-o\"></i> [Delete] </font></button>");
                    RequestApprovalsList.append("</td>");
                } else {
                    RequestApprovalsList.append("<td width=15%> ");
                    RequestApprovalsList.append("<a href=\"#\" onclick=\"return false;\" class=\"btn btn-primary btn-xs\"><i class=\"fa fa-folder\"></i> View </a>&nbsp;&nbsp;");
                    RequestApprovalsList.append("<a href=\"#\" onclick=\"return false;\" class=\"btn btn-info btn-xs\"><i class=\"fa fa-pencil\"></i> Edit </a>&nbsp;&nbsp;");
                    RequestApprovalsList.append("<a href=\"#\" onclick=\"return false;\" class=\"btn btn-danger btn-xs\"><i class=\"fa fa-trash-o\"></i> Delete </a>");
                    RequestApprovalsList.append("</td>");
                }

                RequestApprovalsList.append("</tr>");
                SrlNo++;
            }
            rset.close();
            stmt.close();

            //
            Parsehtm var20 = new Parsehtm(request);
            var20.SetField("RequestApprovalsList", RequestApprovalsList.toString());
            var20.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/ApprovalRequest.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            Supportive.DumException("RequestApprove", "First Method -- GetInfo--MAIN", request, var19, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "RequestApprove");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void DeleteRecord(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex) {
        Query = "";
        stmt = null;
        rset = null;
        Query = request.getParameter("RequestApprovalIndex").trim();
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
//        if (Status == 0) {
//            Result = helper.DeleteHolidayInfo(request, HolidayIndex, 1, conn, servletContext);
//        } else if (Status == 1) {
//            Result = helper.DeleteHolidayInfo(request, HolidayIndex, 0, conn, servletContext);
//        }
        out.println(Result);
    }
}
