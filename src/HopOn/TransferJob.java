package HopOn;

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
public class TransferJob extends HttpServlet {
    private String ScreenNo = "18";
    private String Query = "";
    private PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;
    private CallableStatement cStmt = null;

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
//        String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = getServletContext();
        String UserId = "";
        int CityIndex = 0;
        int isAdmin = 0;
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
            isAdmin = Integer.parseInt(session.getAttribute("isAdmin").toString());
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
//                Class.forName("com.mysql.jdbc.Driver");
//                conn = DriverManager.getConnection(connect_string);
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
            if (ActionID.equals("GetTransferInput")) {
                GetTransferInput(request, out, conn, context, UserId, CityIndex, isAdmin);
            } else if (ActionID.equals("TransferSave")) {
                TransferSave(request, out, conn, context, UserId, CityIndex, isAdmin);
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

    private void GetTransferInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, int isAdmin) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuilder TechnicianNames = new StringBuilder();
        StringBuilder JobNumber = new StringBuilder();

        try {
            int ParamCheck = -1;
            ParamCheck = (isAdmin == 1 ? ParamCheck : CityIndex);
            //--old Query  Query = "SELECT Id,UserName FROM MobileUsers WHERE Status=0 ORDER BY Id";
            //- new Query---logic: showing those technicians which are online and not busy or quota <=3
//            Query = "SELECT Id,UserName FROM MobileUsers WHERE UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
//                    "AND Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10))";
            //JOIN ISSUE RESOLVED. IMPLEMENTED LEFT JOIN
            // This query was not showing technicians who doesnot have any complains with the complain status = 0
            //Inner join was neglecting them.
/*            Query = "SELECT a.Id, a.UserName, b.cnt FROM MobileUsers a, \n" +
                    " (SELECT UserId, count(*) cnt FROM Assignment WHERE ComplaintStatus = 0 GROUP BY UserId) b \n" +
                    " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
                    " AND a.Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10)) \n" +
                    " AND a.id=b.UserId AND b.cnt < (SELECT totalcount FROM TotalJobsHand) ORDER BY a.Id ";*/
/*            Query = " SELECT a.Id, a.UserName, IFNULL(b.cnt,0) FROM MobileUsers a \n" +
                    " LEFT JOIN (SELECT UserId, count(*) cnt FROM Assignment WHERE ComplaintStatus = 0 GROUP BY UserId) b ON \n" +
                    " a.id=b.UserId AND b.cnt < (SELECT totalcount FROM TotalJobsHand) \n" +
                    " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
                    " AND a.Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10)) \n" +
                    " ORDER BY a.Id ";*/
//            Query = "SELECT a.Id, a.UserName, b.cnt FROM MobileUsers a, \n" +
//                    " (SELECT UserId, count(*) cnt FROM Assignment WHERE ComplaintStatus = 0 GROUP BY UserId) b \n" +
//                    " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
//                    " AND a.Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10)) \n" +
//                    " AND a.id=b.UserId AND b.cnt < (SELECT totalcount FROM TotalJobsHand) ORDER BY a.Id ";
            if (isAdmin == 1)
                Query = "SELECT a.Id, a.UserName, IFNULL(b.cnt,0) FROM MobileUsers a \n" +
                        " LEFT JOIN (SELECT UserId, count(*) cnt FROM Assignment WHERE ComplaintStatus = 0 GROUP BY UserId) b ON \n" +
                        " a.id=b.UserId AND b.cnt < (SELECT totalcount FROM TotalJobsHand WHERE CityIndex = " + CityIndex + ") \n" +
                        " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
                        " AND a.Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10)) \n" +
                        " UNION ALL \n" +
                        " SELECT a.Id, a.UserName,0 FROM MobileUsers a \n" +
                        " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') AND \n" +
                        " a.Id NOT IN (SELECT UserId FROM Assignment) ";
            else
                Query = "SELECT a.Id, a.UserName, IFNULL(b.cnt,0) FROM MobileUsers a \n" +
                        " LEFT JOIN (SELECT UserId, count(*) cnt FROM Assignment WHERE ComplaintStatus = 0 GROUP BY UserId) b ON \n" +
                        " a.id=b.UserId AND b.cnt < (SELECT totalcount FROM TotalJobsHand WHERE CityIndex = " + CityIndex + ") \n" +
                        " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
                        " AND a.Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10)) AND " +
                        " a.CityIndex = " + CityIndex + " \n" +
                        " UNION ALL \n" +
                        " SELECT a.Id, a.UserName,0 FROM MobileUsers a \n" +
                        " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') AND \n" +
                        " a.Id NOT IN (SELECT UserId FROM Assignment) AND a.CityIndex = " + ParamCheck + " ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            TechnicianNames.append("<option value=0 Selected Disabled> Select Technician </option>");
            while (rset.next()) {
                TechnicianNames.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            stmt.close();

/*            Query = " SELECT a.Id,CONCAT(a.ComplainNumber ,' - ' , c.UserName) FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)" +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
                    " WHERE " +
                    " a.Id IN (SELECT ComplaintId FROM Assignment WHERE ComplaintStatus=0 ) " +
                    " ORDER BY a.Id";*/
            if (isAdmin == 1)
                Query = " SELECT a.Id,CONCAT(a.ComplainNumber ,' - ' , c.UserName) ComplainNumber,b.ComplaintStatus " +
                        " FROM CustomerData a " +
                        " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND " +
                        " b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) AND ComplaintStatus = 0 " +
                        " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
                        " ORDER BY a.Id ";
            else
                Query = " SELECT a.Id,CONCAT(a.ComplainNumber ,' - ' , c.UserName) ComplainNumber,b.ComplaintStatus " +
                        " FROM CustomerData a " +
                        " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND " +
                        " b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) AND ComplaintStatus = 0 " +
                        " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
                        " WHERE a.CityIndex = " + ParamCheck + " " +
                        " ORDER BY a.Id ";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            JobNumber.append("<option value=0 Selected Disabled> Select Job Number </option>");
            while (rset.next()) {
                JobNumber.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("TechnicianNames", TechnicianNames.toString());
            Parser.SetField("JobNumber", JobNumber.toString());
            //Parser.SetField("ExistingData", ExistingData.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/TransferJob.html");
            out.flush();
            out.close();
        } catch (Exception Ex) {
            try {
                Supportive.doLog(servletContext, "First Function", Ex.getMessage(), Ex);
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "TransferJob");
                Parser.SetField("ActionID", "GetTransferInput");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

    }

    private void TransferSave(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, int isAdmin) {
        stmt = null;
        rset = null;
        Query = "";

        int TotalJobCount = 0;
        int TechnicianCount = 0;
        String ComplainNumber = "";
        int PreviousTechId = 0;
        int LoginCount = 0;
        int AssignmentIndex = 0;

        String UserName = "";
        Query = request.getParameter("Complain").trim();
        int Complain = Integer.parseInt(Query);

        Query = request.getParameter("Technician");
        int TechId = Integer.parseInt(Query);

//        int TFlag = 0;
//        int TAssignStatus = -1;
//        int TAssignmentStatus = 0;
//        Query = "(SELECT Id,ComplainNumber , 0 flag, AssignStatus,-1 AssignmentStatus FROM CustomerData \n" +
//                "WHERE AssignStatus = 0 AND Id = " + Complain + ") " +
//                "UNION " +
//                "(SELECT a.Id,a.ComplainNumber, 7 flag, a.AssignStatus,b.AssignmentStatus AssignmentStatus\n" +
//                "FROM CustomerData a STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId   \n" +
//                "AND b.ComplaintStatus = 7 AND b.Status = 0 AND b.Id = (SELECT MAX(x.Id)  \n" +
//                "FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) WHERE a.Id = " + Complain + " )  \n" +
//                "UNION " +
//                "(SELECT a.Id,a.ComplainNumber, 8 flag, a.AssignStatus,b.AssignmentStatus AssignmentStatus FROM \n" +
//                "CustomerData a STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
//                "AND b.ComplaintStatus=8 AND b.Id = (SELECT MAX(x.Id) \n" +
//                "FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) \n" +
//                "WHERE a.ReAssigned <> 2 AND a.Id = " + Complain + " )";
//        try {
//            stmt = conn.createStatement();
//            rset = stmt.executeQuery(Query);
//            if (rset.next()) {
//                TFlag = rset.getInt(3);
//                TAssignStatus = rset.getInt(4);
//            }
//            rset.close();
//            stmt.close();
//        } catch (Exception E) {
//            out.println(" Exception in Transfer " + E.getMessage());
//        }
//
//        if (TFlag == 0 && TAssignStatus < 0) {
//            //out.println("Already Assigned!!");
//            try {
//                Parsehtm Parser = new Parsehtm(request);
//                Parser.SetField("FormName", "TransferJob");
//                Parser.SetField("ActionID", "GetTransferInput");
//                Parser.SetField("Message", "This job is already assigned to technician!!");
//                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
//                return;
//            } catch (Exception e) {
//                out.println("Error3" + e.getMessage());
//            }
//        }
        int ParamCheck = -1;
        ParamCheck = (isAdmin == 1 ? ParamCheck : CityIndex);

        if (isAdmin == 1)
            Query = "SELECT UserId FROM MobileUsers WHERE Id = " + TechId;
        else
            Query = "SELECT UserId FROM MobileUsers WHERE Id = " + TechId + " AND CityIndex = " + ParamCheck;
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                UserName = rset.getString(1);
            rset.close();
            stmt.close();
        } catch (Exception E) {
            out.println("Error while getting User Id " + E.getMessage());
        }
        try {
            Query = "SELECT Max(Id) FROM Assignment WHERE ComplaintId = " + Complain;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                AssignmentIndex = rset.getInt(1);
            rset.close();
            stmt.close();

            if (isAdmin == 1)
                Query = "SELECT count(*) FROM  LoginTrail WHERE UserId = '" + UserName + "' AND UserType = 'M' ";
            else
                Query = "SELECT count(*) FROM  LoginTrail WHERE UserId = '" + UserName + "' AND UserType = 'M' AND CityIndex = " + ParamCheck;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                LoginCount = rset.getInt(1);
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            out.println("Error 0" + Ex.getMessage());
            out.flush();
            out.close();
            return;
        }

        if (LoginCount == 0) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "TransferJob");
                Parser.SetField("ActionID", "GetTransferInput");
                Parser.SetField("Message", "Technician is offline.Please select another Technician for this job");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
            } catch (Exception e) {
                out.println("Error3" + e.getMessage());
            }
            return;
        }

        try {
            Query = "SELECT  UserId FROM Assignment WHERE ComplaintId=" + Complain + " AND Id = " + AssignmentIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                PreviousTechId = rset.getInt(1);
            rset.close();
            stmt.close();

            if (TechId == PreviousTechId) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "TransferJob");
                    Parser.SetField("ActionID", "GetTransferInput");
                    Parser.SetField("Message", "This Job is already assign to this Technician.Please select another");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
                } catch (Exception e) {
                    out.println("Error1" + e.getMessage());
                }
                return;
            }

            Query = "{CALL TotalJobsNumber(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, CityIndex);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                TotalJobCount = rset.getInt(1);
            }
            rset.close();
            cStmt.close();

            //Numeric Tech ID should be sent to Procedure Call
            Query = "{CALL AssignmentCount(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, TechId);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                TechnicianCount = rset.getInt(1);
            }
            rset.close();
            cStmt.close();

//            out.println("Total" + TotalJobCount);
//            out.println("Tech Count" + TechnicianCount);
            //if (TechnicianCount <= TotalJobCount) {
            if (TotalJobCount > TechnicianCount) {
                //if (TotalJobCount >= TechnicianCount) {
                //out.println("IN IF");
                //Updating the status of the Complain in CustomerData
                //Making ensure that data has been downloaded or not on tech phone
                //1 Means it is assigned to respective Tech
                //0 Means it is waiting to be download. Although it is assigned but it is not downloaded in tech mobile
//                Query = "{CALL UpdateJobStatus(?,?)}";
//                cStmt = conn.prepareCall(Query);
//                cStmt.setString(1, ComplainNumber);
//                cStmt.setInt(2, 0);
//                rset = cStmt.executeQuery();
//                rset.close();
//                cStmt.close();

                String CurrDate = "";
                Query = "{CALL CurrentDate()}";
                cStmt = conn.prepareCall(Query);
                rset = cStmt.executeQuery();
                if (rset.next())
                    CurrDate = rset.getString(1).trim();
                rset.close();
                cStmt.close();

                try {
                    Query = "{CALL assignment_update_tech(?,?,?,?,?,?,?)}";
                    cStmt = conn.prepareCall(Query);
                    cStmt.setInt(1, TechId);
                    cStmt.setInt(2, Complain);
                    cStmt.setInt(3, 1);
                    cStmt.setInt(4, AssignmentIndex);
                    cStmt.setString(5, CurrDate);
                    cStmt.setString(6, UserId);
                    cStmt.setInt(7, CityIndex);
                    rset = cStmt.executeQuery();
                    rset.close();
                    cStmt.close();
                } catch (Exception Ex) {
                    out.println("Error while transferring job to new tech " + Ex.getMessage());
                    out.close();
                    out.flush();
                    return;
                }

                //Update CustomerData Status on Behalf ComplainNumber
//                Query = "{CALL UpdateJobStatus(?,?)}";
//                cStmt = conn.prepareCall(Query);
//                cStmt.setString(1, ComplainNumber);
//                cStmt.setInt(2, 1);
//                rset = cStmt.executeQuery();
//                rset.close();
//                cStmt.close();

                //Status Assigmment History
                //In Mobile: Status= 2
                //In Auotmatic Scheduler: Status= 1
                //In Manual Status: Status= 0
                //In TransferJob Status: Status= 3
                // It will be use to pick user name from respective table.

                pStmt = conn.prepareStatement(
                        "INSERT INTO AssignmentHistory(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
                                "AssignmentStatus,Latitude,Longtitude,ManualStatus,Distance,FalconRadius,TransferFlag,TransferDate,CityIndex) " +
                                "VALUES (?,?,3,now(),?,?,?,?,?,0,0,0,1,?,?)");

                pStmt.setInt(1, TechId);
                pStmt.setInt(2, Complain);
                pStmt.setString(3, UserId);
                pStmt.setInt(4, 0);
                pStmt.setInt(5, 0);
                pStmt.setString(6, "-");
                pStmt.setString(7, "-");
                pStmt.setString(8, CurrDate);
                pStmt.setInt(9, CityIndex);
                pStmt.executeUpdate();
                pStmt.close();

                //Insertion of Transfer History in order to get which complain was assigned to who earlier
                //Status is set to 1 that means that job number need to be deleted in phone db
                Query = "{CALL history_transfer_insert(?,?,?,?,?,?)}";
                try {
                    cStmt = conn.prepareCall(Query);
                    cStmt.setInt(1, Complain);
                    cStmt.setInt(2, PreviousTechId);
                    cStmt.setString(3, UserId);
                    cStmt.setString(4, CurrDate);
                    cStmt.setInt(5, 1);
                    cStmt.setString(6, CurrDate);
                    rset = cStmt.executeQuery();
                    rset.close();
                    cStmt.close();

                } catch (SQLException ex) {
                    out.println("EXCEPTION 5 <BR>");
                    try {
                        Parsehtm Parser = new Parsehtm(request);
                        Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
                    } catch (Exception var15) {
                    }
                    Supportive.doLog(this.getServletContext(), "Record History Insertion-05", ex.getMessage(), ex);
                    out.flush();
                    out.close();
                    return;
                }

                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "TransferJob");
                Parser.SetField("ActionID", "GetTransferInput");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
                out.flush();
                out.close();
            } else {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "TransferJob");
                    Parser.SetField("ActionID", "GetTransferInput");
                    Parser.SetField("Message", "This Technician has already been assigned " + TotalJobCount + " jobs.Please select another Technician for this job");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
                } catch (Exception e) {
                    out.println("Error1" + e.getMessage());
                    out.close();
                    out.flush();
                    return;
                }
            }

        } catch (Exception e) {
            out.println("Main Function" + e.getMessage());
            out.close();
            out.flush();
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
