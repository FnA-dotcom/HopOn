package TAMAPP;

import Parsehtm.Parsehtm;

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
public class Dashboard extends HttpServlet {
    private String ScreenNo = "2";
    private String Query = "";
    private PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;
    private CallableStatement cStmt = null;
    private ResultSet rset1 = null;
    private String Query1 = "";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String ActionID = null;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = getServletContext();
        String UserId = "";
        try {
            HttpSession session = request.getSession(true);
            if (session.getAttribute("UserId") == null || session.getAttribute("UserId").equals("")) {
                out.println(" <center><table cellpadding=3 cellspacing=2><tr><td bgcolor=\"#FFFFFF\"> " +
                        " <font color=green face=arial><b>Your Session Has Been Expired</b></font> " +
                        " </td></tr></table> " +
                        " <p> " +
                        " <font face=arial size=+1><b><a href=/TAMAPP/index.html target=_top> Return to Login Page " +
                        " </a></b></font>" +
                        " <br><font face=arial size=-2>(You will need to sign in again.)</font><br> " +
                        " </center> ");
                out.flush();
                out.close();
                session.removeAttribute("UserId");
                return;
            }
            UserId = session.getAttribute("UserId").toString();
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
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var14) {
                conn = null;
                out.println("Exception excp conn: " + var14.getMessage());
            }
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }
            if (request.getParameter("ActionID") == null) {
                ActionID = "Home";
                return;
            }
            ActionID = request.getParameter("ActionID");
            if (ActionID.equals("GETINPUT")) {
                GetValue(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowAssignedJobs")) {
                ShowAssignedJobs(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowCompleteJobs")) {
                ShowCompleteJobs(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowCanceledJobs")) {
                ShowCanceledJobs(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowOtherJobs")) {
                ShowOtherJobs(request, out, conn, context, UserId);
            } else {
                out.println("Under Development ... " + ActionID);
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

    private void GetValue(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";

        int AllowedJobs = 0;

        int totalToday = 0;
        int cTodayStatus = 0;
        int cancelledTodayStatus = 0;
        int OtherTodayStatus = 0;

        int UnderWorkingYearly = 0;
        int UnderWorkingMonthly = 0;
        int UnderWorkingDay = 0;

        int StatusMarkedYearly = 0;
        int StatusMarkedMonthly = 0;
        int StatusMarkedDay = 0;

        int Radius = 0;
        int FalconRadius = 0;
        String FalconLat = "";
        String FalconLon = "";

        StringBuilder ComplainStatusWise = new StringBuilder();

        try {
            //Total Assign Today
            Query = "SELECT count(*) FROM Assignment WHERE  " +
                    "date_format(CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d')";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                totalToday = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            //Complete Status Today
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus = 6 AND " +
                    "date_format(CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d')";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                cTodayStatus = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            //Cancelled Status Today
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus = 8 AND " +
                    "date_format(CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d')";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                cancelledTodayStatus = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            //Other Status Today
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus NOT IN (6,8) AND " +
                    "date_format(CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d')";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                OtherTodayStatus = rset.getInt(1);
            }
            rset.close();
            stmt.close();


            //Yearly
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus NOT IN (6,7,8) AND " +
                    "CreatedDate >=  DATE_SUB(NOW(), INTERVAL 365 DAY) ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                UnderWorkingYearly = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            //Monthly
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus NOT IN (6,7,8) AND " +
                    "CreatedDate >=  DATE_SUB(NOW(), INTERVAL 30 DAY) ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                UnderWorkingMonthly = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            //Today
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus NOT IN (6,7,8) AND " +
                    "date_format(CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d')";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                UnderWorkingDay = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            //Yearly
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus IN (6,7,8) AND " +
                    "CreatedDate >=  DATE_SUB(NOW(), INTERVAL 365 DAY) ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                StatusMarkedYearly = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            //Monthly
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus IN (6,7,8) AND " +
                    "CreatedDate >=  DATE_SUB(NOW(), INTERVAL 30 DAY) ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                StatusMarkedMonthly = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            //Today
            Query = "SELECT count(*) FROM Assignment WHERE ComplaintStatus IN (6,7,8) AND " +
                    "date_format(CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d')";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                StatusMarkedDay = rset.getInt(1);
            }
            rset.close();
            stmt.close();


            ComplainStatusWise.append("<div class=\"tab-content\">");
            ComplainStatusWise.append("<div id=\"tab-1\" class=\"tab-pane active\">");
            ComplainStatusWise.append("<div class=\"full-height-scroll\">");
            ComplainStatusWise.append("<div class=\"table-responsive\" style=\"height:130px;\">");
            ComplainStatusWise.append("<table class=\"table table-striped table-hover\">");
            ComplainStatusWise.append("<tbody>");
            Query = "SELECT a.Id, a.ComplaintStatus, ifnull(b.Cnt,0) FROM ComplaintStatus a " +
                    "LEFT JOIN (SELECT ComplaintStatus, Count(*) Cnt FROM Assignment " +
                    "WHERE CreatedDate >= DATE_SUB(NOW(), INTERVAL 30 DAY) GROUP BY ComplaintStatus) b ON a.Id = b.ComplaintStatus " +
                    " WHERE  a.Status=0 ORDER BY a.ComplaintStatus";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    ComplainStatusWise.append("<tr>");
                    ComplainStatusWise.append("<td style=\"text-align:left;\"><small>" + rset.getString(2) + "</small></td>");
                    ComplainStatusWise.append("<td style=\"text-align:right;\"><small>" + rset.getInt(3) + "</small></td>");
                    ComplainStatusWise.append("</tr>");
                }
                rset.close();
                stmt.close();
            } catch (Exception e) {
                out.println("<br>Error No.: 0002");
                out.println("<br>Error Is : No Complain Found ...!!! \n\n\n</b>");
                out.println("<input type=hidden name=ActionID value=\"GETINPUT\">");
                out.println("<input class=\"buttonERP\" type=button name=Back Value=\"  Back  \" onclick=history.back()>");
                out.println("</body></html>");
                out.flush();
                out.close();
                return;
            }
            ComplainStatusWise.append("</tbody>");
            ComplainStatusWise.append("</table>");
            ComplainStatusWise.append("</div></div></div></div>");

            Query1 = "{CALL radius_check()}";
            cStmt = conn.prepareCall(Query1);
            rset1 = cStmt.executeQuery();
            if (rset1.next()) {
                Radius = rset1.getInt(1);
                FalconRadius = rset1.getInt(2);
                FalconLat = rset1.getString(3);
                FalconLon = rset1.getString(4);
            }
            rset1.close();
            cStmt.close();

            Query1 = "{CALL TotalJobsNumber()}";
            cStmt = conn.prepareCall(Query1);
            rset1 = cStmt.executeQuery();
            if (rset1.next()) {
                AllowedJobs = rset1.getInt(1);
            }
            rset1.close();
            cStmt.close();


            Parsehtm Parser = new Parsehtm();
            Parser.SetField("totalToday", String.valueOf(totalToday));
            Parser.SetField("cTodayStatus", String.valueOf(cTodayStatus));
            Parser.SetField("cancelledTodayStatus", String.valueOf(cancelledTodayStatus));
            Parser.SetField("OtherTodayStatus", String.valueOf(OtherTodayStatus));

            Parser.SetField("UnderWorkingYearly", String.valueOf(UnderWorkingYearly));
            Parser.SetField("UnderWorkingMonthly", String.valueOf(UnderWorkingMonthly));
            Parser.SetField("UnderWorkingDay", String.valueOf(UnderWorkingDay));

            Parser.SetField("StatusMarkedYearly", String.valueOf(StatusMarkedYearly));
            Parser.SetField("StatusMarkedMonthly", String.valueOf(StatusMarkedMonthly));
            Parser.SetField("StatusMarkedDay", String.valueOf(StatusMarkedDay));

            Parser.SetField("ComplainStatusWise", ComplainStatusWise.toString());

            Parser.SetField("Radius", String.valueOf(Radius));
            Parser.SetField("FalconRadius", String.valueOf(FalconRadius));
            Parser.SetField("FalconLat", String.valueOf(FalconLat));
            Parser.SetField("FalconLon", String.valueOf(FalconLon));

            Parser.SetField("AllowedJobs", String.valueOf(AllowedJobs));

            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Dashboards/JobsDashboard.html");
        } catch (Exception Ex) {

        }
    }

    private void ShowAssignedJobs(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String FormName = "ViewJobs";
        String ActionID = "ShowJobs";
        String Form = "View Jobs";

        StringBuilder AssignedJob = new StringBuilder();
        String ReplaceComplainNumber = "";

        int SrlNo = 0;
        try {
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model,  "
                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType "
                    + "FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND date_format(b.CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d') " +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SrlNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                AssignedJob.append("<tr>");
                AssignedJob.append("<td align=left>" + SrlNo + "</td>\n");// SerialNo
                AssignedJob.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                AssignedJob.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                AssignedJob.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                AssignedJob.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                AssignedJob.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                AssignedJob.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                AssignedJob.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                AssignedJob.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                AssignedJob.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                AssignedJob.append("<td align=left>" + rset.getString(12) + "</td>\n");// UserName
                AssignedJob.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                AssignedJob.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("AssignedJob", AssignedJob.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowAssignedJobs.html");
        } catch (Exception var15) {
            out.println("Exception in main... " + var15.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private void ShowCompleteJobs(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String FormName = "ViewJobs";
        String ActionID = "ShowJobs";
        String Form = "View Jobs";

        StringBuilder CompleteStatus = new StringBuilder();
        String ReplaceComplainNumber = "";

        int SrlNo = 0;
        try {
            //Complete Status Today
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model,  "
                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType "
                    + "FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND date_format(b.CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d') AND ComplaintStatus = 6 " +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SrlNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                CompleteStatus.append("<tr>");
                CompleteStatus.append("<td align=left>" + SrlNo + "</td>\n");// SerialNo
                CompleteStatus.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                CompleteStatus.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                CompleteStatus.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                CompleteStatus.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                CompleteStatus.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                CompleteStatus.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                CompleteStatus.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                CompleteStatus.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                CompleteStatus.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                CompleteStatus.append("<td align=left>" + rset.getString(12) + "</td>\n");// UserName
                CompleteStatus.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                CompleteStatus.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("CompleteStatus", CompleteStatus.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowCompleteJobs.html");
        } catch (Exception var15) {
            out.println("Exception in main... " + var15.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private void ShowCanceledJobs(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String FormName = "ViewJobs";
        String ActionID = "ShowJobs";
        String Form = "View Jobs";

        StringBuilder CanceledStatus = new StringBuilder();
        String ReplaceComplainNumber = "";

        int SrlNo = 0;
        try {
            //Canceled Status Today
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model,  "
                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType "
                    + "FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND date_format(b.CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d') AND ComplaintStatus = 8 " +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SrlNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                CanceledStatus.append("<tr>");
                CanceledStatus.append("<td align=left>" + SrlNo + "</td>\n");// SerialNo
                CanceledStatus.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                CanceledStatus.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                CanceledStatus.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                CanceledStatus.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                CanceledStatus.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                CanceledStatus.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                CanceledStatus.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                CanceledStatus.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                CanceledStatus.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                CanceledStatus.append("<td align=left>" + rset.getString(12) + "</td>\n");// UserName
                CanceledStatus.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                CanceledStatus.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("CanceledStatus", CanceledStatus.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowCanceledJobs.html");
        } catch (Exception var15) {
            out.println("Exception in main... " + var15.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private void ShowOtherJobs(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String FormName = "ViewJobs";
        String ActionID = "ShowJobs";
        String Form = "View Jobs";

        StringBuilder OtherStatus = new StringBuilder();
        String ReplaceComplainNumber = "";

        int SrlNo = 0;
        try {
            //Other Status Today
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model,  "
                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType "
                    + "FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND date_format(b.CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d') AND ComplaintStatus NOT IN (6,8) " +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SrlNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                OtherStatus.append("<tr>");
                OtherStatus.append("<td align=left>" + SrlNo + "</td>\n");// SerialNo
                OtherStatus.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                OtherStatus.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                OtherStatus.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                OtherStatus.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                OtherStatus.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                OtherStatus.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                OtherStatus.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                OtherStatus.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                OtherStatus.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                OtherStatus.append("<td align=left>" + rset.getString(12) + "</td>\n");// UserName
                OtherStatus.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                OtherStatus.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("OtherStatus", OtherStatus.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowOtherJobs.html");
        } catch (Exception var15) {
            out.println("Exception in main... " + var15.getMessage());
            out.flush();
            out.close();
            return;
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
