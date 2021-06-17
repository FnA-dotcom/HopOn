package TAMAPP;

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
import javax.servlet.http.HttpSession;


@SuppressWarnings("Duplicates")
public class ManualAssignment extends HttpServlet {
    private String ScreenNo = "17";
    private String Query = "";
    private PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;
    private CallableStatement cStmt = null;
    private String LogString = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

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
        response.setContentType("text/html; charset=UTF-8");
        Supportive supp = new Supportive();
        String connect_string = supp.GetConnectString();
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
            CityIndex = Integer.parseInt(session.getAttribute("CityIndex").toString());
            isAdmin = Integer.parseInt(session.getAttribute("isAdmin").toString());
            //UserId = Supportive.GetCookie("UserId", request);
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
                GETINPUT(request, out, conn, context, UserId,CityIndex,isAdmin);
            } else if (ActionID.equals("FetchComplainData")) {
                FetchComplainData(request, out, conn, context, UserId);
            } else if (ActionID.compareTo("SaveAssignment") == 0) {
                SaveRecords(request, out, conn, context, UserId);
            } else if (ActionID.compareTo("GetTechLocation") == 0) {
                GetTechLocation(request, out, conn, context, UserId);
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

    private void GETINPUT(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId,int CityIndex,int isAdmin) {
        stmt = null;
        rset = null;
        int SrlNo = 1;
        Query = "";
        StringBuilder UserNames = new StringBuilder();
        StringBuilder ComplainNumber = new StringBuilder();
        StringBuilder ExistingData = new StringBuilder();
        String ReplaceComplainNumber = "";
        try {

            //old Query-------- //Query = "SELECT Id,UserName FROM MobileUsers WHERE Status=0 ORDER BY Id";
            //new Query---logic: showing those technicians which are online and not busy or quota <=3
//            Query = "SELECT Id,UserName FROM MobileUsers WHERE UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
//                    "AND Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10))";
            //Change -- one more check added of job quota
//            Query = "SELECT a.Id, a.UserName, b.cnt FROM MobileUsers a, \n" +
//                    " (SELECT UserId, count(*) cnt FROM Assignment WHERE ComplaintStatus = 0 GROUP BY UserId) b \n" +
//                    " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
//                    " AND a.Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10)) \n" +
//                    " AND a.id=b.UserId AND b.cnt < (SELECT totalcount FROM TotalJobsHand) ORDER BY a.Id ";
            //Replaced inner join with  left join
            Query = "SELECT a.Id, a.UserName, IFNULL(b.cnt,0) FROM MobileUsers a \n" +
                    " LEFT JOIN (SELECT UserId, count(*) cnt FROM Assignment WHERE ComplaintStatus = 0 GROUP BY UserId) b ON \n" +
                    " a.id=b.UserId AND b.cnt < (SELECT totalcount FROM TotalJobsHand WHERE CityIndex = "+CityIndex+") \n" +
                    " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') \n" +
                    " AND a.Id IN (SELECT UserId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10)) AND " +
                    " a.CityIndex = "+CityIndex+" \n" +
                    " UNION ALL \n" +
                    " SELECT a.Id, a.UserName,0 FROM MobileUsers a \n" +
                    " WHERE a.UserId IN (SELECT UserId FROM LoginTrail WHERE UserType='M') AND \n" +
                    " a.Id NOT IN (SELECT UserId FROM Assignment) AND a.CityIndex = "+CityIndex+" ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            UserNames.append("<option value=0 Selected Disabled> Select Technician </option>");
            while (rset.next()) {
                UserNames.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            stmt.close();

            //-----------Old Query
//            Query = " SELECT Id,ComplainNumber FROM CustomerData WHERE " +
//                    " Id NOT IN (SELECT ComplaintId FROM Assignment WHERE ComplaintStatus NOT IN (1,2,3,4,5,10) )" +
//                    " ORDER BY Id";
            //Query = "SELECT Id,ComplainNumber FROM CustomerData WHERE  Id NOT IN (SELECT ComplaintId FROM Assignment) ORDER BY Id";
            //---------New Query--------------
/*            Query = "(SELECT Id,ComplainNumber FROM CustomerData \n" +
                    "WHERE AssignStatus = 0) UNION (SELECT a.Id,a.ComplainNumber \n" +
                    "FROM CustomerData a STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
                    "AND b.ComplaintStatus = 7 AND b.Status = 0 AND b.Id = (SELECT MAX(x.Id) \n" +
                    "FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)) \n" +
                    "UNION (SELECT a.Id,a.ComplainNumber FROM \n" +
                    "CustomerData a STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
                    "AND b.ComplaintStatus=8 AND b.Id = (SELECT MAX(x.Id) \n" +
                    "FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) \n" +
                    "WHERE a.ReAssigned <> 2)";*/

            Query = "(SELECT Id,ComplainNumber,'No Status' FROM CustomerData \n" +
                    " WHERE AssignStatus = 0 AND CityIndex = "+CityIndex+") \n" +
                    " UNION \n" +
                    " (SELECT a.Id,a.ComplainNumber,c.ComplaintStatus \n" +
                    " FROM CustomerData a \n" +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
                    " AND b.ComplaintStatus = 7 AND b.Status = 0 AND b.Id = (SELECT MAX(x.Id) \n" +
                    " FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) \n" +
                    " STRAIGHT_JOIN ComplaintStatus c on b.ComplaintStatus = c.Id " +
                    " WHERE a.CityIndex = "+CityIndex+" ) \n" +
                    " UNION \n" +
                    " (SELECT a.Id,a.ComplainNumber,c.ComplaintStatus FROM \n" +
                    " CustomerData a \n" +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
                    " AND b.ComplaintStatus=8 AND b.Id = (SELECT MAX(x.Id) \n" +
                    " FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) \n " +
                    " STRAIGHT_JOIN ComplaintStatus c on b.ComplaintStatus = c.Id \n" +
                    " WHERE a.ReAssigned <> 2 AND a.CityIndex = "+CityIndex+" ) " ;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            ComplainNumber.append("<option value=0 Selected Disabled> Select Job Number </option>");
            while (rset.next()) {
                ComplainNumber.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + " - [" + rset.getString(3) + "] </option>");
            }
            rset.close();
            stmt.close();

/*            Query = "SELECT b.UserName,c.ComplainNumber,a.RegisterBy," +
                    "CASE WHEN a.ComplaintStatus=0 THEN 'New Job' " +
                    "WHEN a.ComplaintStatus=1 THEN 'Pick Job'" +
                    "WHEN a.ComplaintStatus=2 THEN 'Reach Spot' " +
                    "WHEN a.ComplaintStatus=3 THEN 'Fill Vehicle Inspection' " +
                    "WHEN a.ComplaintStatus=4 THEN 'Start the Job' " +
                    "WHEN a.ComplaintStatus=5 THEN 'Fill QA Form' " +
                    "WHEN a.ComplaintStatus=6 THEN 'Job Completed' " +
                    "WHEN a.ComplaintStatus=7 THEN 'Job Postponed' " +
                    "WHEN a.ComplaintStatus=8 THEN 'Job Cancelled' " +
                    "WHEN a.ComplaintStatus=10 THEN 'Testing Phase' " +
                    "ELSE 'N/A' END," +
                    "a.ComplaintStatus,b.UserId " +
                    "FROM Assignment a " +
                    "STRAIGHT_JOIN MobileUsers b ON  a.UserId=b.Id " +
                    "STRAIGHT_JOIN CustomerData c ON a.ComplaintId=c.Id " +
                    " ORDER BY c.CreatedDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                if (rset.getString(2).contains("#"))
                    ReplaceComplainNumber = rset.getString(2).replace("#", "^");

                if (rset.getInt(5) == 7 || rset.getInt(5) == 7) {
                    ExistingData.append("<tr>");
                    ExistingData.append("<td>" + SrlNo + "</td>");
                    //ExistingData.append("<td>" + rset.getString(1).trim() + "</td>");//TechName
                    ExistingData.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(6) + " \" target=\"_self\"> " + rset.getString(1) + " </a></td>");

                    ExistingData.append("<td>" + rset.getString(2).trim() + "</td>");//ComplainNumber
                    ExistingData.append("<td>" + rset.getString(3) + "</td>");//RegisterBy
                    ExistingData.append("<td>" + rset.getString(4) + "</td>");//ComplaintStatus
                    ExistingData.append("<td align=center><a class=\"btn-xs btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [View] </a></td>");
                    ExistingData.append("</tr>");
                } else {
                    ExistingData.append("<tr>");
                    ExistingData.append("<td>" + SrlNo + "</td>");
                    ExistingData.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location'><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(6) + " \" target=\"_self\"> " + rset.getString(1) + " </a></td>");
                    ExistingData.append("<td>" + rset.getString(2) + "</td>");//ComplainNumber
                    ExistingData.append("<td>" + rset.getString(3) + "</td>");//RegisterBy
                    ExistingData.append("<td>" + rset.getString(4) + "</td>");//ComplaintStatus
                    ExistingData.append("<td align=center><a class=\"btn-xs btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [View] </a></td>");
                    ExistingData.append("</tr>");
                }
                ++SrlNo;
            }
            rset.close();
            stmt.close();*/

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("UserNames", UserNames.toString());
            Parser.SetField("ComplainNumber", ComplainNumber.toString());
            //Parser.SetField("ExistingData", ExistingData.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/ManualAssignment.html");
            out.flush();
            out.close();
        } catch (Exception Ex) {
            Supportive.doLog(servletContext, "Manual Assignment -- First Function", Ex.getMessage(), Ex);
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ManualAssignment");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
        out.flush();
        out.close();
    }

    private void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        CallableStatement cStmt = null;
        int TechCount = 0;
        String TechId = "";
        int TotalJobCount = 0;
        int LoginCount = 0;
        String UserName = "", Latitude = "", Longtitude = "";
        Query = request.getParameter("Technician").trim();
        int Technician = Integer.parseInt(Query);

        Query = request.getParameter("Complain").trim();
        int Complain = Integer.parseInt(Query);

        int Flag = 0;
        int AssignStatus = -1;
        int AssignmentStatus = 0;
        Query = "(SELECT Id,ComplainNumber , 0 flag, AssignStatus,-1 AssignmentStatus FROM CustomerData \n" +
                "WHERE AssignStatus = 0 AND Id = " + Complain + ") " +
                "UNION " +
                "(SELECT a.Id,a.ComplainNumber, 7 flag, a.AssignStatus,b.AssignmentStatus AssignmentStatus\n" +
                "FROM CustomerData a STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId   \n" +
                "AND b.ComplaintStatus = 7 AND b.Status = 0 AND b.Id = (SELECT MAX(x.Id)  \n" +
                "FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) WHERE a.Id = " + Complain + " )  \n" +
                "UNION " +
                "(SELECT a.Id,a.ComplainNumber, 8 flag, a.AssignStatus,b.AssignmentStatus AssignmentStatus FROM \n" +
                "CustomerData a STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
                "AND b.ComplaintStatus=8 AND b.Id = (SELECT MAX(x.Id) \n" +
                "FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) \n" +
                "WHERE a.ReAssigned <> 2 AND a.Id = " + Complain + " )";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                Flag = rset.getInt(3);
                AssignStatus = rset.getInt(4);
                AssignmentStatus = rset.getInt(5);
            }
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            out.println(" Exception " + Ex.getMessage());
            Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 001 ", Ex.getMessage(), Ex);
            out.close();
            out.flush();
            return;
        }

        if (Flag == 0 && AssignStatus < 0) {
            //out.println("Already Assigned!!");
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ManualAssignment");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.SetField("Message", "This job is already assigned to technician!!");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
                return;
            } catch (Exception Ex) {
                out.println("Error3" + Ex.getMessage());
                Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 002 ", Ex.getMessage(), Ex);
                out.close();
                out.flush();
                return;
            }
        }

//        if (Flag == 8 && AssignmentStatus == 1) {
//            out.println("Already Assigned!!");
//            try {
//                Parsehtm Parser = new Parsehtm(request);
//                Parser.SetField("FormName", "ManualAssignment");
//                Parser.SetField("ActionID", "GETINPUT");
//                Parser.SetField("Message", "This job is already assigned to technician");
//                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
//                return;
//            } catch (Exception e) {
//                out.println("Error3" + e.getMessage());
//            }
//        }

//        if (Flag == 0) {
//            try {
//                Query = "UPDATE CustomerData SET AssignStatus=1 WHERE Id = " + Complain;
//                stmt = conn.createStatement();
//                stmt.executeUpdate(Query);
//                stmt.close();
//
//            } catch (Exception E) {
//                out.println(" Exception " + E.getMessage());
//            }
//        }
//        try {
//            pStmt = conn.prepareStatement(
//                    "INSERT INTO Assignment(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
//                            "AssignmentStatus,ManualStatus) VALUES(?,?,0,now(),?,0,0,1)");
//
//            pStmt.setInt(1, Technician);
//            pStmt.setInt(2, Complain);
//            pStmt.setString(3, UserId);
//            pStmt.executeUpdate();
//            pStmt.close();
//
//        } catch (Exception E) {
//            out.println(" Exception " + E.getMessage());
//        }
//        else if (Flag == 8) {
//            try {
//                pStmt = conn.prepareStatement(
//                        "INSERT INTO Assignment(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
//                                "AssignmentStatus,ManualStatus) VALUES(?,?,0,now(),?,0,0,1)");
//
//                pStmt.setInt(1, Technician);
//                pStmt.setInt(2, Complain);
//                pStmt.setString(3, UserId);
//                pStmt.executeUpdate();
//                pStmt.close();
//
//                int maxAssignId = 0;
//                Query = "SELECT max(Id) FROM Assignment WHERE ComplaintId = " + Complain;
//                try {
//                    stmt = conn.createStatement();
//                    rset = stmt.executeQuery(Query);
//                    if (rset.next())
//                        maxAssignId = rset.getInt(1);
//                    rset.close();
//                    stmt.close();
//                } catch (Exception E) {
//                        out.println("ERROR IN MAX ID " + E.getMessage());
//                }
//                Query = "UPDATE Assignment SET AssignmentStatus=1 WHERE Id = " + maxAssignId;
//                stmt = conn.createStatement();
//                stmt.executeUpdate(Query);
//                stmt.close();
//            } catch (Exception E) {
//                out.println(" Exception " + E.getMessage());
//            }
//        }

        LogString += " \n *********************************************************************  \n ";
        LogString += " Technician ID : " + Technician + " | ";
        LogString += " Complain ID : " + Complain + " | ";

        Query = "SELECT UserId FROM MobileUsers WHERE Id = " + Technician;
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                UserName = rset.getString(1).trim();
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 003 ", Ex.getMessage(), Ex);
            out.close();
            out.flush();
            return;
        }
        LogString += " User Name : " + UserName + " | ";
        try {
            Query = "SELECT count(*) FROM  LoginTrail WHERE UserId = '" + UserName + "' AND UserType = 'M' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                LoginCount = rset.getInt(1);
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            out.println("Error 0" + Ex.getMessage());
            Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 004 ", Ex.getMessage(), Ex);
            out.close();
            out.flush();
            return;
        }
        LogString += " Login Count : " + LoginCount + " | ";
        if (LoginCount == 0) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ManualAssignment");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.SetField("Message", "Technician is offline.Please select another Technician for this job");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
            } catch (Exception Ex) {
                out.println("Error3" + Ex.getMessage());
                Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 005 ", Ex.getMessage(), Ex);
                out.close();
                out.flush();
                return;
            }
            return;
        }
        //Stored Procedure
        try {
            Query = "{CALL TotalJobsNumber()}";
            cStmt = conn.prepareCall(Query);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                TotalJobCount = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            out.println("Error1" + Ex.getMessage());
            Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 006 ", Ex.getMessage(), Ex);
            out.close();
            out.flush();
            return;
        }

        LogString += " Total Job Count : " + TotalJobCount + " | ";

        // Nature of Job
        //1. Medium Job
        //2. Difficult Job
        try {
            Query = "{CALL AssignmentCount(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, Technician);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                TechCount = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            out.println("Assignment Count" + Ex.getMessage());
            Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 007 ", Ex.getMessage(), Ex);
            out.close();
            out.flush();
            return;
        }

        LogString += " Tech Count : " + TechCount + " | ";

        String ComplainNumber = "";
        Query = "SELECT  ComplainNumber FROM CustomerData WHERE Id= " + Complain;
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                ComplainNumber = rset.getString(1).trim();
            rset.close();
            stmt.close();

        } catch (Exception Ex) {
            out.println("Complain Number" + Ex.getMessage());
            Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 008 ", Ex.getMessage(), Ex);
            out.close();
            out.flush();
            return;
        }

        LogString += " Complain Number : " + ComplainNumber + " | ";

        Query = "SELECT Latitude,Longtitude,UserId, DATE_FORMAT(CreatedDate,'%d-%m-%Y %H:%i:%s') " +
                "FROM TechnicianLocation WHERE UserId='" + UserName + "' AND  " +
                "CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId='" + UserName + "')";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Latitude = rset.getString(1);
                Longtitude = rset.getString(2);
            }
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 009 ", Ex.getMessage(), Ex);
            out.close();
            out.flush();
            return;
        }

        //Status Assigmment History
        //In Mobile: Status= 2
        //In Auotmatic Scheduler: Status= 1
        //In Manual Status: Status= 4
        //In TransferJob Status: Status= 3
        // It will be use to pick user name from respective table.

        //out.println("TechCount " + TechCount);
        //out.println("TotalJobCount " + TotalJobCount);


        // if (TechCount <= TotalJobCount) {
        if (TotalJobCount > TechCount) {
            try {
                pStmt = conn.prepareStatement(
                        "INSERT INTO AssignmentHistory(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
                                "AssignmentStatus,Latitude,Longtitude,ManualStatus,Distance,FalconRadius,TransferFlag,ManualDate) " +
                                "VALUES (?,?,4,now(),?,?,?,?,?,1,0,0,0,NOW())");

                pStmt.setInt(1, Technician);
                pStmt.setInt(2, Complain);
                pStmt.setString(3, UserId);
                pStmt.setInt(4, 0);
                pStmt.setInt(5, 0);
                pStmt.setString(6, Latitude);
                pStmt.setString(7, Longtitude);
                pStmt.executeUpdate();
                pStmt.close();

                pStmt = conn.prepareStatement(
                        "INSERT INTO Assignment(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
                                "AssignmentStatus,ManualStatus,ManualDate) VALUES (?,?,0,now(),?,0,0,1,NOW())");

                pStmt.setInt(1, Technician);
                pStmt.setInt(2, Complain);
                pStmt.setString(3, UserId);
                pStmt.executeUpdate();
                pStmt.close();

                //Updating the status of the Complain in CustomerData
                //Making ensure that data has been downloaded or not on tech phone
                //1 Means it is assigned to respective Tech
                //0 Means it is waiting to be download. Although it is assigned but it is not downloaded in tech mobile
                Query = "{CALL UpdateJobStatus(?,?)}";
                cStmt = conn.prepareCall(Query);
                cStmt.setString(1, ComplainNumber);
                cStmt.setInt(2, 1);
                rset = cStmt.executeQuery();
                rset.close();
                cStmt.close();

                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ManualAssignment");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
                out.flush();
                out.close();
            } catch (Exception Ex) {
                out.println("Error2" + Ex.getMessage());
                Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 0010 ", Ex.getMessage(), Ex);
                out.close();
                out.flush();
                return;
            }
            LogString += " \n *********************************************************************  \n ";
            Supportive.LogString(this.getServletContext(), LogString);
        } else {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ManualAssignment");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.SetField("Message", "This Technician has already been assigned according to define quota jobs.Please select another Technician for this job");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
            } catch (Exception Ex) {
                out.println("Error3" + Ex.getMessage());
                Supportive.doLog(servletContext, "Manual Assignment -- Second Function ** 0010 ", Ex.getMessage(), Ex);
                out.close();
                out.flush();
                return;
            }
        }
    }

    private void FetchComplainData(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";

        Query = request.getParameter("ComplainNumber");
        int ComplainNumber = Integer.parseInt(Query);

        String RegistrationNumber = "";
        String CustomerName = "";
        String CellNo = "";
        String PhoneNo = "";
        String Make = "";
        String Model = "";
        String Color = "";
        String ChassisNo = "";
        String JobNature = "";
        String JobType = "";
        try {


            Query = "SELECT  IFNULL(a.RegistrationNum,'-'),IFNULL(a.CustName,'-'),IFNULL(a.CellNo,'-'),IFNULL(a.PhoneNo,'-'), " +
                    "IFNULL(a.Make,'-'),IFNULL(a.Model,'-'),IFNULL(a.Color,'-'),IFNULL(a.ChesisNo,'-'),IFNULL(b.JobNature,'-')," +
                    "IFNULL(c.JobType,'-')  " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN JobNature b ON a.JobNatureIndex=b.Id " +
                    " STRAIGHT_JOIN JobType c ON a.JobTypeIndex=c.Id" +
                    " WHERE a.Id=" + ComplainNumber;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            if (rset.next()) {
                RegistrationNumber = rset.getString(1).trim();
                CustomerName = rset.getString(2).trim();
                CellNo = rset.getString(3).trim();
                PhoneNo = rset.getString(4).trim();
                Make = rset.getString(5).trim();
                Model = rset.getString(6).trim();
                Color = rset.getString(7).trim();
                ChassisNo = rset.getString(8).trim();
                JobNature = rset.getString(9).trim();
                JobType = rset.getString(10).trim();
            }
            rset.close();
            stmt.close();

            out.println(RegistrationNumber + "|" + CustomerName + "|" + CellNo + "|" + PhoneNo + "|" + Make + "|" +
                    Model + "|" + Color + "|" + ChassisNo + "|" + JobNature + "|" + JobType);
        } catch (Exception e) {
            out.println("Error " + e.getMessage());
        }
    }

    private void GetTechLocation(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        int SerialNo = 0;
        String TechnicianId = request.getParameter("TechId");
        StringBuilder TechLocation = new StringBuilder();
        String TechName = "";
        String lat = "";
        String lon = "";
        String URL = "";
        try {
            Query = "SELECT Latitude,Longtitude,UserId, DATE_FORMAT(CreatedDate,'%d-%m-%Y %H:%i:%s') " +
                    " FROM TechnicianLocation WHERE UserId='" + TechnicianId + "' AND " +
                    "CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId='" + TechnicianId + "')";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                ++SerialNo;
                lat = rset.getString(1).trim();
                lon = rset.getString(2).trim();
                TechName = rset.getString(3);
                if (!rset.getString(1).equals("-") && !rset.getString(2).equals("-") &&
                        !rset.getString(1).equals("null") && !rset.getString(2).equals(null) &&
                        !lat.isEmpty() && !lon.isEmpty() && !lat.equals(null) && !lon.equals(null)) {
                    TechLocation.append("['" + rset.getString(4) + "' ,");
                    TechLocation.append(rset.getString(1) + ",");
                    TechLocation.append(rset.getString(2) + ",");
                    TechLocation.append(SerialNo + "],");
                }
            }

            rset.close();
            stmt.close();
            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Location", TechLocation.toString());
            Parser.SetField("URL", URL);
            Parser.SetField("lat", lat);
            Parser.SetField("lon", lon);
            Parser.SetField("CustomerName", TechName);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ViewComplainLocation.html");
        } catch (Exception var35) {
            out.println("Unable to process the request..." + var35.getMessage());
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