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
public class DashboardTechnician extends HttpServlet {
    private String ScreenNo = "3";
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
            if (ActionID.equals("TechnicianDashboard")) {
                TechnicianDashboard(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowLoggedInTech")) {
                ShowLoggedInTech(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowLoggedOutTech")) {
                ShowLoggedOutTech(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowOfflineTech")) {
                ShowOfflineTech(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowUnAssignedJobs")) {
                ShowUnAssignedJobs(request, out, conn, context, UserId);
            } else if (ActionID.equals("ShowAssignedJobs")) {
                ShowAssignedJobs(request, out, conn, context, UserId);
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

    private void TechnicianDashboard(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String Query2 = "";
        Statement stmt2 = null;
        ResultSet rset2 = null;

        int TotalTech = 0;
//        int Online = 0;
//        int LogOut = 0;
//        int Offline = 0;

        String TechName = "";
        int TAssigned = 0;
        int TPicked = 0;
        int TCompleted = 0;
        int TPospond = 0;
        int TCanceled = 0;

        StringBuilder TechnicianWise = new StringBuilder();
        StringBuilder TotalTechStatus = new StringBuilder();
        try {

            TechnicianWise.append("<div class=\"tab-content\">");
            TechnicianWise.append("<div id=\"tab-1\" class=\"tab-pane active\">");
            TechnicianWise.append("<div class=\"full-height-scroll\">");
            TechnicianWise.append("<div class=\"table-responsive\" style=\"height:130px;\">");
            TechnicianWise.append("<table class=\"table table-striped table-hover\">");
            TechnicianWise.append("<tbody>");
            Query = "SELECT a.Id, a.UserName, ifnull(b.Cnt,0),a.UserId FROM MobileUsers a " +
                    "LEFT JOIN (SELECT UserId, Count(*) Cnt FROM Assignment " +
                    "WHERE CreatedDate >= DATE_SUB(NOW(), INTERVAL 30 DAY) GROUP BY UserId) b ON a.Id = b.UserId  " +
                    "WHERE a.Status=0 ORDER BY a.UserName";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    TechnicianWise.append("<tr>");
                    TechnicianWise.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(4) + " \" target=\"_self\"> <small>" + rset.getString(2) + "</small> </a></td>");
                    TechnicianWise.append("<td style=\"text-align:right;\"><small>" + rset.getInt(3) + "</small></td>");
                    TechnicianWise.append("</tr>");
                }
                rset.close();
                stmt.close();
            } catch (Exception e) {
                out.println("<br>Error No.: 0002");
                out.println("<br>Error Is : No Technician Found ...!!! \n\n\n</b>");
                out.println("<input type=hidden name=ActionID value=\"GETINPUT\">");
                out.println("<input class=\"buttonERP\" type=button name=Back Value=\"  Back  \" onclick=history.back()>");
                out.println("</body></html>");
                out.flush();
                out.close();
                return;
            }
            TechnicianWise.append("</tbody>");
            TechnicianWise.append("</table>");
            TechnicianWise.append("</div></div></div></div>");

            Query1 = "{CALL TotalTechs()}";
            cStmt = conn.prepareCall(Query1);
            rset1 = cStmt.executeQuery();
            if (rset1.next()) {
                TotalTech = rset1.getInt(1);
            }
            rset1.close();
            cStmt.close();

//            stage = "2";
//            Query1 = "{CALL TotalOnline()}";
//            cStmt = conn.prepareCall(Query1);
//            rset1 = cStmt.executeQuery();
//            if (rset1.next()) {
//                Online = rset1.getInt(1);
//            }
//            rset1.close();
//            cStmt.close();

//            stage = "3";
//            Query1 = "{CALL TotalLogOut()}";
//            cStmt = conn.prepareCall(Query1);
//            rset1 = cStmt.executeQuery();
//            if (rset1.next()) {
//                LogOut = rset1.getInt(1);
//            }
//            rset1.close();
//            cStmt.close();

//            Query1 = "{CALL TotalOffline()}";
//            cStmt = conn.prepareCall(Query1);
//            rset1 = cStmt.executeQuery();
//            if (rset1.next()) {
//                Offline = rset1.getInt(1);
//            }
//            rset1.close();
//            cStmt.close();
//
//            if (Offline > 0)
//                LogOut = LogOut - Offline;

//            int LoginCount = 0;
//            String TechId = "";
//            Query1 = "{CALL TotalTechStatus()}";
//            cStmt = conn.prepareCall(Query1);
//            rset1 = cStmt.executeQuery();
//            while (rset1.next()) {
//                TechId = rset1.getString(8);
//                TechName = rset1.getString(2).trim();
//                TAssigned = rset1.getInt(3);
//                TPicked = rset1.getInt(4);
//                TCompleted = rset1.getInt(5);
//                TPospond = rset1.getInt(6);
//                TCanceled = rset1.getInt(7);
//
//                Query2 = " SELECT count(*)  FROM LoginTrail " +
//                        " WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + rset1.getString(8) + "')) AND " +
//                        " UserType = 'M' ";
//                stmt2 = conn.createStatement();
//                rset2 = stmt2.executeQuery(Query2);
//                if (rset2.next())
//                    LoginCount = rset2.getInt(1);
//                rset2.close();
//                stmt2.close();
//
//                TotalTechStatus.append("<tr>");
////                TotalTechStatus.append("<td class=\"text-left medium\" >" + TechName + "</td>");
//                TotalTechStatus.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + TechId + " \" target=\"_self\"> <small>" + TechName + "</small> </a></td>");
//
//                TotalTechStatus.append("<td class=\"text-center medium\" ><span class=\"label label-info\">" + TAssigned + "</span></td>");
//                TotalTechStatus.append("<td class=\"text-center medium\" ><span class=\"label label-success\">" + TPicked + "</span></td>");
//                TotalTechStatus.append("<td class=\"text-center medium\" ><span class=\"label label-primary\">" + TCompleted + "</span></td>");
//                TotalTechStatus.append("<td class=\"text-center medium\" ><span class=\"label label-warning\">" + TPospond + "</span></td>");
//                TotalTechStatus.append("<td class=\"text-center medium\" ><span class=\"label label-danger\">" + TCanceled + "</span></td>");
//                if (LoginCount == 0)
//                    TotalTechStatus.append("<td class=\"text-center \"><img src=/TAMAPP/images/OfflineImage.png width=20 height=10 ><br></td>");// Status
//                else
//                    TotalTechStatus.append("<td class=\"text-center \" ><img src=/TAMAPP/images/OnlineImage.png width=20 height=10 ><br></td>");// Status
//                TotalTechStatus.append("</tr>");
//            }
//            rset1.close();
//            cStmt.close();


//            int UnAssignedJobs = 0;
//            Query1 = "{CALL UnAssignedJobs()}";
//            cStmt = conn.prepareCall(Query1);
//            rset1 = cStmt.executeQuery();
//            if (rset1.next()) {
//                UnAssignedJobs = rset1.getInt(1);
//            }
//            rset1.close();
//            cStmt.close();
//
//            int AssignedJobs = 0;
//            Query1 = "{CALL AssignedJobs()}";
//            cStmt = conn.prepareCall(Query1);
//            rset1 = cStmt.executeQuery();
//            if (rset1.next()) {
//                AssignedJobs = rset1.getInt(1);
//            }
//            rset1.close();
//            cStmt.close();

            Parsehtm Parser = new Parsehtm();

            Parser.SetField("TechnicianWise", TechnicianWise.toString());


            Parser.SetField("TotalTech", String.valueOf(TotalTech));
//            Parser.SetField("Online", String.valueOf(Online));
//            Parser.SetField("LogOut", String.valueOf(LogOut));
//            Parser.SetField("Offline", String.valueOf(Offline));
//            Parser.SetField("UnAssignedJobs", String.valueOf(UnAssignedJobs));
//            Parser.SetField("AssignedJobs", String.valueOf(AssignedJobs));

            Parser.SetField("TotalTechStatus", TotalTechStatus.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Dashboards/TechnicianDashboard.html");
        } catch (Exception Ex) {
            out.println("Error -- > " + Ex.getMessage());
        }
    }

    private void ShowLoggedInTech(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String Query1 = "";
        Statement stmt1 = null;
        ResultSet rset1 = null;
        StringBuilder LoggedIn = new StringBuilder();
        int LoginCount = 0;
        int SrlNo = 0;
        try {
            Query = "SELECT Id,UserName,UserId FROM MobileUsers WHERE Status=0 ORDER BY Id";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {

                Query1 = "SELECT count(*),IFNULL(DATE_FORMAT(CreatedDate,'%Y-%m-%d %H:%i:%s'),'0000-00-00'), " +
                        " IFNULL(TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(CreatedDate,'%Y-%m-%d %H:%i:%s')),'00:00:00') " +
                        " FROM LoginTrail WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + rset.getString(3) + "')) AND UserType = 'M' ";
                stmt1 = conn.createStatement();
                rset1 = stmt1.executeQuery(Query1);
                if (rset1.next())
                    LoginCount = rset1.getInt(1);
                if (LoginCount == 1) {
                    SrlNo++;
                    LoggedIn.append("<tr>");
                    LoggedIn.append("<td align=left>" + SrlNo + "</td>\n");// Horn
                    LoggedIn.append("<td align=left>" + rset.getString(2) + "</td>\n");// UserName
                    LoggedIn.append("<td align=left>" + rset.getString(3) + "</td>\n");// UserId
                    LoggedIn.append("<td><img src=/TAMAPP/images/OnlineImage.png width=30 height=20 ><br></td>");// Status
                    LoggedIn.append("<td align=left>" + rset1.getString(2) + "</td>\n");// LoginDate
                    LoggedIn.append("<td align=left>" + rset1.getString(3) + "</td>\n");// Duration

                    LoggedIn.append("</tr>");
                }
            }
            rset.close();
            stmt.close();


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("LoggedIn", LoggedIn.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowLoggedInTech.html");
            out.flush();
            out.close();
        } catch (Exception Ex) {
            try {
                Supportive.doLog(servletContext, "First Function", Ex.getMessage(), Ex);
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "DashboardTechnician");
                Parser.SetField("ActionID", "TechnicianDashboard");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void ShowLoggedOutTech(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String Query1 = "";
        Statement stmt1 = null;
        ResultSet rset1 = null;
        StringBuilder LoggedOut = new StringBuilder();
        int LoginCount = 0;
        int SrlNo = 0;
        try {
            Query = "SELECT Id,UserName,UserId FROM MobileUsers WHERE Status=0 ORDER BY Id";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {

                Query1 = "SELECT count(*),IFNULL(DATE_FORMAT(CreatedDate,'%Y-%m-%d %H:%i:%s'),'0000-00-00'), " +
                        " IFNULL(TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(CreatedDate,'%Y-%m-%d %H:%i:%s')),'00:00:00') " +
                        " FROM LoginTrail WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + rset.getString(3) + "')) AND UserType = 'M' ";
                stmt1 = conn.createStatement();
                rset1 = stmt1.executeQuery(Query1);
                if (rset1.next())
                    LoginCount = rset1.getInt(1);
                if (LoginCount == 0) {
                    SrlNo++;
                    LoggedOut.append("<tr>");
                    LoggedOut.append("<td align=left>" + SrlNo + "</td>\n");// Horn
                    LoggedOut.append("<td align=left>" + rset.getString(2) + "</td>\n");// UserName
                    LoggedOut.append("<td align=left>" + rset.getString(3) + "</td>\n");// UserId
                    LoggedOut.append("<td><img src=/TAMAPP/images/OfflineImage.png width=30 height=20 ><br></td>");// Status
                    LoggedOut.append("<td align=left>" + rset1.getString(2) + "</td>\n");// LoginDate
                    LoggedOut.append("<td align=left>" + rset1.getString(3) + "</td>\n");// Duration

                    LoggedOut.append("</tr>");
                }
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("LoggedOut", LoggedOut.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowLoggedOutTech.html");
            out.flush();
            out.close();
        } catch (Exception Ex) {
            try {
                Supportive.doLog(servletContext, "Second Function", Ex.getMessage(), Ex);
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "DashboardTechnician");
                Parser.SetField("ActionID", "TechnicianDashboard");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void ShowOfflineTech(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String Query1 = "";
        Statement stmt1 = null;
        ResultSet rset1 = null;
        StringBuilder Offline = new StringBuilder();
        int SrlNo = 0;
        try {
/*            Query = "SELECT a.Id,a.UserName,a.UserId,IFNULL(DATE_FORMAT(MAX(b.CreatedDate),'%Y-%m-%d %H:%i:%s'),'0000-00-00')," +
                    "IFNULL(TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s')),'00:00:00')" +
                    " FROM MobileUsers a " +
                    " STRAIGHT_JOIN TechnicianLoginTrail b ON a.UserId = b.TechId AND LoginFlag = 'Offline' " +
                    " WHERE a.Status=0 ORDER BY a.Id ";*/
/*            Query = "SELECT b.UserId,b.UserName,a.LoggedTime,DATE_FORMAT(a.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.LoginFlag," +
                    " IFNULL(TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(a.CreatedDate,'%Y-%m-%d %H:%i:%s')),'00:00:00')" +
                    " FROM TechnicianLoginTrail a " +
                    " STRAIGHT_JOIN MobileUsers b ON a.TechId = b.UserId " +
                    " WHERE " +
                    " a.LoginFlag = 'Offline' AND " +
                    " a.CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLoginTrail x ) ";*/
/*            Query = "SELECT b.UserId, b.UserName, a.LoginFlag, DATE_FORMAT(max(a.CreatedDate),'%Y-%m-%d %H:%i:%s'), \n" +
                    "IFNULL(TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(max(a.CreatedDate),'%Y-%m-%d %H:%i:%s')),'00:00:00')\n" +
                    "FROM TechnicianLoginTrail a \n" +
                    "STRAIGHT_JOIN MobileUsers b ON a.TechId = b.UserId \n" +
                    "WHERE \n" +
                    "a.LoginFlag = 'Offline' \n" +
                    "AND a.Id = (SELECT MAX(Id) FROM TechnicianLoginTrail x WHERE x.TechId = TechId)\n" +
                    "GROUP BY  b.UserId, b.UserName, a.LoginFlag";*/
            Query = "SELECT b.UserId, b.UserName, a.LoginFlag, DATE_FORMAT(max(a.CreatedDate),'%Y-%m-%d %H:%i:%s'), \n" +
                    "IFNULL(TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(max(a.CreatedDate),'%Y-%m-%d %H:%i:%s')),'00:00:00')," +
                    "concat(timestampdiff(day,DATE_FORMAT(max(a.CreatedDate),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')),'d ',lpad (MOD(timestampdiff(hour,DATE_FORMAT(max(a.CreatedDate),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')), 24),2,'0' ),'h',lpad (MOD(timestampdiff(MINUTE,DATE_FORMAT(max(a.CreatedDate),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')), 60),2,'0' ),'m' ) as TotalTime \n" +
                    "FROM TechnicianLoginTrail a, MobileUsers b, (SELECT TechId, MAX(Id) Id FROM TechnicianLoginTrail GROUP BY TechId) x\n" +
                    "WHERE a.TechId = b.UserId AND b.`Status` = 0 AND a.LoginFlag = 'Offline' AND a.TechId=x.TechId AND a.Id=x.Id\n" +
                    "GROUP BY  b.UserId, b.UserName, a.LoginFlag " +
                    "ORDER BY TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(max(a.CreatedDate),'%Y-%m-%d %H:%i:%s')) DESC ";

            // Query = "SELECT Id,UserId,UserName FROM MobileUsers WHERE Status = 0";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
//                Query1 = "SELECT LoggedTime,DATE_FORMAT(CreatedDate,'%Y-%m-%d %H:%i:%s'),LoginFlag," +
//                        " IFNULL(TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(CreatedDate,'%Y-%m-%d %H:%i:%s')),'00:00:00')" +
//                        " FROM TechnicianLoginTrail b " +
//                        " WHERE TechId = '" + rset.getString(2) + "' AND " +
//                        " CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLoginTrail x " +
//                        " WHERE x.TechId = '" + rset.getString(2) + "') AND LoginFlag = 'Offline' ";
//                stmt1 = conn.createStatement();
//                rset1 = stmt1.executeQuery(Query1);
//                if (rset1.next()) {
                //if (rset1.getString(3).equals("Offline")) {
                SrlNo++;
                Offline.append("<tr>");
                Offline.append("<td align=left>" + SrlNo + "</td>\n");// Srl
                Offline.append("<td align=left>" + rset.getString(2) + "</td>\n");// UserName
                Offline.append("<td align=left>" + rset.getString(1) + "</td>\n");// UserId
                Offline.append("<td align=center><img src=/TAMAPP/images/Offline.png width=30 height=40 ><br></td>");// Status
                Offline.append("<td align=left>" + rset.getString(4) + "</td>\n");// LoginDate
                Offline.append("<td align=left>" + rset.getString(5) + "</td>\n");// Duration
                Offline.append("<td align=left>" + rset.getString(6) + "</td>\n");// TotalDuration(Days)
                Offline.append("</tr>");
                // }
                //}
            }
            rset.close();
            stmt.close();


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Offline", Offline.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowOfflineTech.html");
            out.flush();
            out.close();
        } catch (Exception Ex) {
            try {
                Supportive.doLog(servletContext, "Third Function", Ex.getMessage(), Ex);
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "DashboardTechnician");
                Parser.SetField("ActionID", "TechnicianDashboard");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
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

    private void ShowUnAssignedJobs(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String ReplaceCN = "";
        int SerialNo = 0;

        StringBuilder DuAJ = new StringBuilder();
        try {
            SerialNo = 0;
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,IFNULL(a.AssignDate,'0000-00-00'),a.Make," +
                    "a.Model,a.Longtitude,a.Latitude,a.ComplainNumber,e.JobType,a.Address " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " WHERE a.AssignStatus = 0 " +
                    " ORDER BY a.CreatedDate DESC";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceCN = rset.getString(9).replace("#", "^");
                DuAJ.append("<tr>");
                DuAJ.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                DuAJ.append("<td align=left>" + rset.getString(9) + "</td>\n");// ComplainNumber
                DuAJ.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                DuAJ.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                DuAJ.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                DuAJ.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                DuAJ.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                DuAJ.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                DuAJ.append("<td align=left>" + rset.getString(10) + "</td>\n");// JobType
                DuAJ.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceCN + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                DuAJ.append("<td align=left>" + rset.getString(11) + "</td>\n");// Address
                DuAJ.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("DuAJ", DuAJ.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowUnAssginedJobs.html");
            out.flush();
            out.close();
        } catch (Exception E) {
            out.println(E.getMessage() + " ERROR ");
        }
    }

    private void ShowAssignedJobs(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String ReplaceCN = "";
        int SerialNo = 0;

        StringBuilder DAJ = new StringBuilder();
        try {
            Query = " SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s') ,a.Make,a.Model,\n" +
                    "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,\n" +
                    "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType,\n" +
                    "DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address,\n" +
                    "CASE  WHEN b.Distance IS NULL AND b.ManualStatus = 1 THEN 'Manual' \n" +
                    "\t\t\tWHEN b.Distance IS NOT NULL AND b.TransferFlag = 1 THEN 'Transfer'\n" +
                    " ELSE b.Distance \n" +
                    " END \n" +
                    " FROM CustomerData a \n" +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
                    " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) AND " +
                    " date_format(b.CreatedDate,'%Y-%m-%d') =  DATE_FORMAT(NOW(),'%Y-%m-%d') \n" +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id \n" +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id \n" +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id \n" +
                    " ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceCN = rset.getString(15).replace("#", "^");
                DAJ.append("<tr>");
                DAJ.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                DAJ.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                DAJ.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                DAJ.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                DAJ.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                DAJ.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                DAJ.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                DAJ.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                DAJ.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                DAJ.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                DAJ.append("<td align=left>" + rset.getString(17) + "</td>\n");// StatusDate
                DAJ.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                DAJ.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceCN + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                DAJ.append("<td align=left>" + rset.getString(18) + "</td>\n");// Address
                DAJ.append("<td align=left>" + rset.getString(19) + "</td>\n");// Distance
                DAJ.append("</tr>");

            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("DAJ", DAJ.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/dShowAssginedJobs.html");
            out.flush();
            out.close();
        } catch (Exception Ex) {
            out.println("" + Ex.getMessage());
        }
    }

}
