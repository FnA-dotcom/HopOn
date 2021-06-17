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

@SuppressWarnings("Duplicates")
public class ViewJobs extends HttpServlet {
    private String ScreenNo = "19";
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.HandleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.HandleRequest(request, response);
    }

    public void HandleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = getServletContext();
        int CityIndex = 0;
        int isAdmin = 0;
        try {
            HttpSession session = request.getSession(true);
            if ((session.getAttribute("UserId") == null) || (session.getAttribute("UserId").equals(""))) {
                out.println(" <center><table cellpadding=3 cellspacing=2><tr><td bgcolor=\"#FFFFFF\">  <font color=green face=arial><b>Your Session Has Been Expired</b></font>  </td></tr></table>  <p>  <font face=arial size=+1><b><a href=/TAMAPP/index.html target=_top> Return to Login Page  </a></b></font> <br><font face=arial size=-2>(You will need to sign in again.)</font><br>  </center> ");
                out.flush();
                out.close();
                session.removeAttribute("UserId");
                return;
            }
            String UserId = session.getAttribute("UserId").toString();
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
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var14) {
                conn = null;
                out.println("Exception excp conn: " + var14.getMessage());
            }
            if (request.getParameter("Action") == null) {
                Action = "Home";
                return;
            }
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }

            Action = request.getParameter("Action");

            if (Action.compareTo("ShowJobs") == 0) {
                ShowJobs(request, out, conn, context,CityIndex,isAdmin);
            } else if (Action.compareTo("CreateHistory") == 0) {
                CreateHistory(request, response, out, conn, context, UserId);
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

    private void ShowJobs(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext,int CityIndex,int isAdmin) {
        String FormName = "ViewJobs";
        String ActionID = "ShowJobs";
        String Form = "View Jobs";
        int SerialNo = 0;
        String ReplaceComplainNumber = "";
        StringBuilder AssignedJobs = new StringBuilder();
        StringBuilder UnAssignedJobs = new StringBuilder();
        StringBuilder PostponedJobs = new StringBuilder();
        StringBuilder CancelledJobs = new StringBuilder();

        try {
//            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s') ,a.Make,a.Model, " +
//                    " b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude, " +
//                    " c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType," +
//                    " DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address," +
//                    " CASE  WHEN b.Distance IS NULL THEN 'Manual/Transfer' ELSE b.Distance END" +
//                    " FROM CustomerData a " +
//                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus NOT IN(7,8) " +
//                    " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) " +
//                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
//                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
//                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
//                    " ORDER BY a.AssignDate DESC ";
            Query = " SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s') ,a.Make,a.Model,\n" +
                    "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,\n" +
                    "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType,\n" +
                    "DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address,\n" +
                    "CASE  WHEN b.Distance IS NULL AND b.ManualStatus = 1 THEN 'Manual' \n" +
                    "\t\t\tWHEN b.Distance IS NOT NULL AND b.TransferFlag = 1 THEN 'Transfer'\n" +
                    "ELSE b.Distance \n" +
                    "END\n" +
                    "FROM CustomerData a\n" +
                    "STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus NOT IN(7,8)\n" +
                    "AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)\n" +
                    "STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id\n" +
                    "LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id\n" +
                    "STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    "WHERE a.CityIndex = "+CityIndex+" \n" +
                    "ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                if (rset.getString(14).equals("Job Completed")) {
                    AssignedJobs.append("<tr>");
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + SerialNo + "</td>\n");// SerialNo
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                    // AssignedJobs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Complain Location' ><a href = \"/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=\"_self\"> " + rset.getString(15) + " </a></td>");

                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(3) + "</td>\n");// CustName
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(5) + "</td>\n");// Make
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(6) + "</td>\n");// Model
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(16) + "</td>\n");// JobType
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(17) + "</td>\n");// StatusDate
                    AssignedJobs.append("<td bgcolor=#A2DA62 align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(18) + "</td>\n");// Address
                    AssignedJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(19) + "</td>\n");// Distance
                    AssignedJobs.append("</tr>");
                } else {
                    AssignedJobs.append("<tr>");
                    AssignedJobs.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                    AssignedJobs.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                    // AssignedJobs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Complain Location' ><a href = \"/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=\"_self\"> " + rset.getString(15) + " </a></td>");

                    AssignedJobs.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                    AssignedJobs.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                    AssignedJobs.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                    AssignedJobs.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                    AssignedJobs.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                    AssignedJobs.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                    AssignedJobs.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                    AssignedJobs.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                    AssignedJobs.append("<td align=left>" + rset.getString(17) + "</td>\n");// StatusDate
                    AssignedJobs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                    AssignedJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                    AssignedJobs.append("<td align=left>" + rset.getString(18) + "</td>\n");// Address
                    AssignedJobs.append("<td align=left>" + rset.getString(19) + "</td>\n");// Distance
                    AssignedJobs.append("</tr>");
                }
            }
            rset.close();
            stmt.close();

            SerialNo = 0;
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,IFNULL(a.AssignDate,'0000-00-00'),a.Make," +
                    "a.Model,a.Longtitude,a.Latitude,a.ComplainNumber,e.JobType,a.Address " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " WHERE a.AssignStatus = 0 AND a.CityIndex = "+CityIndex+" " +
                    " ORDER BY a.CreatedDate DESC";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(9).replace("#", "^");
                UnAssignedJobs.append("<tr>");
                UnAssignedJobs.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                UnAssignedJobs.append("<td align=left>" + rset.getString(9) + "</td>\n");// ComplainNumber
                UnAssignedJobs.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                UnAssignedJobs.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                UnAssignedJobs.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                UnAssignedJobs.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                UnAssignedJobs.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                UnAssignedJobs.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                UnAssignedJobs.append("<td align=left>" + rset.getString(10) + "</td>\n");// JobType
                UnAssignedJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                UnAssignedJobs.append("<td align=left>" + rset.getString(11) + "</td>\n");// Address
                UnAssignedJobs.append("</tr>");
            }
            rset.close();
            stmt.close();

            //postponed
            SerialNo = 0;
            //previous Logic
//            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model, " +
//                    " b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude, " +
//                    " c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber," +
//                    "e.JobType,DATE_FORMAT(b.CreatedDate,'%Y-%m-%d  %H:%i:%s'),a.Id,c.UserName " +
//                    " FROM CustomerData a " +
//                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus = 7 " +
//                    " AND b.Status = 0 AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)" +
//                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
//                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
//                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
//                    " ORDER BY a.AssignDate DESC ";

            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s'),a.Make,a.Model, " +
                    " b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude, " +
                    " c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber," +
                    " e.JobType,DATE_FORMAT(b.CreatedDate,'%Y-%m-%d  %H:%i:%s'),a.Id,c.UserName,a.Address  " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus = 7 " +
                    " AND b.Status = 0 AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)" +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    "WHERE a.CityIndex = "+CityIndex+" \n" +
                    " ORDER BY a.AssignDate DESC ";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                PostponedJobs.append("<tr>");
                PostponedJobs.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                PostponedJobs.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                PostponedJobs.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                PostponedJobs.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                PostponedJobs.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                PostponedJobs.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                PostponedJobs.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                PostponedJobs.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                PostponedJobs.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                PostponedJobs.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                PostponedJobs.append("<td align=left>" + rset.getString(17) + "</td>\n");// PostponedDate
                //PostponedJobs.append("<td align=left>" + rset.getString(12) + "</td>\n");// UserName
                PostponedJobs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                PostponedJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                // PostponedJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ViewJobs?Action=CreateHistory&Id=" + rset.getString(18) + "&UserName=" + rset.getString(19) + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ ReAlive ] </a></td>");
                PostponedJobs.append("<td align=left>" + rset.getString(20) + "</td>\n");// Address
                PostponedJobs.append("</tr>");

            }
            rset.close();
            stmt.close();

            //" WHERE a.AssignStatus = 0 " +
            SerialNo = 0;
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s'),a.Make,a.Model,  "
                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,a.Id,e.JobType," +
                    " DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address "
                    + "FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus=8 " +
                    " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) " +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    "WHERE a.CityIndex = "+CityIndex+" \n" +
                    " ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                CancelledJobs.append("<tr>");
                CancelledJobs.append("<td align=left>" + SerialNo + "</td>\n");
                CancelledJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                CancelledJobs.append("<td align=left>" + rset.getString(15) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(1) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(2) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(3) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(4) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(5) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(6) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(14) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(17) + "</td>\n");// JobType
                CancelledJobs.append("<td align=left>" + rset.getString(18) + "</td>\n");
                CancelledJobs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                CancelledJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ViewJobs?Action=CreateHistory&Id=" + rset.getString(16) + "&UserName=" + rset.getString(12) + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ ReAlive ] </a></td>");
                CancelledJobs.append("<td align=left>" + rset.getString(19) + "</td>\n");//Address
                CancelledJobs.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("AssignedJobs", AssignedJobs.toString());
            Parser.SetField("UnAssignedJobs", UnAssignedJobs.toString());
            Parser.SetField("PostponedJobs", PostponedJobs.toString());
            Parser.SetField("CancelledJobs", CancelledJobs.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowJobs.html");
        } catch (Exception var14) {
            out.println("Unable to process the request..." + var14.getMessage());
        }
    }

    private void CreateHistory(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        String FormName = "ViewJobs";
        String Action = "ShowJobs";

        PreparedStatement pStmt = null;
        stmt = null;
        rset = null;
        Query = "";

        String Id = request.getParameter("Id");
        String UserName = request.getParameter("UserName");

        try {
            Query = "SELECT Id,RegistrationNum,CustName,CellNo,PhoneNo,Make,Model,Color,ChesisNo,Address,JobTypeIndex,DeviceNo," + //12
                    "Status,CreatedDate,Insurance,ComplainNumber,JobNatureIndex,Latitude,Longtitude," + //19
                    "AssignDate,AssignStatus,ReAssigned FROM CustomerData WHERE Id='" + Id + "'";//22

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                pStmt = conn.prepareStatement(
                        "INSERT INTO CustomerDataHistory(OldId,RegistrationNum,CustName,CellNo,PhoneNo,Make,Model,Color,ChesisNo,Address,"
                                + "JobTypeIndex,DeviceNo,Status,CreatedDate,Insurance,ComplainNumber,JobNatureIndex,Latitude,Longtitude,AssignDate,"
                                + "AssignStatus,CreatedBy,ReAssignedStatus,RegisteredDate) "
                                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())");

                pStmt.setInt(1, rset.getInt(1));//OldId
                pStmt.setString(2, rset.getString(2).trim());//RegistrationNum
                pStmt.setString(3, rset.getString(3));//CustName
                pStmt.setString(4, rset.getString(4));//CellNo
                pStmt.setString(5, rset.getString(5));//PhoneNo
                pStmt.setString(6, rset.getString(6));//Make
                pStmt.setString(7, rset.getString(7));//Model
                pStmt.setString(8, rset.getString(8));//Color
                pStmt.setString(9, rset.getString(9));//ChesisNo
                pStmt.setString(10, rset.getString(10));//Address
                pStmt.setInt(11, rset.getInt(11));//JobTypeIndex
                pStmt.setString(12, rset.getString(12));//DeviceNo
                pStmt.setInt(13, rset.getInt(13));//Status
                pStmt.setString(14, rset.getString(14));//CreatedDate
                pStmt.setString(15, rset.getString(15));//Insurance
                pStmt.setString(16, rset.getString(16));//ComplainNumber
                pStmt.setInt(17, rset.getInt(17));//JobNatureIndex
                pStmt.setString(18, rset.getString(18));//Latitude
                pStmt.setString(19, rset.getString(19));//Longtitude
                pStmt.setString(20, rset.getString(20));//AssignDate
                pStmt.setInt(21, rset.getInt(21));//AssignStatus
                pStmt.setString(22, UserId);//Register By
                pStmt.setInt(23, rset.getInt(22));//ReAssignedStatus
                pStmt.executeUpdate();
                pStmt.close();
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            out.println("Error while creating history " + e.getMessage());
            out.close();
            out.flush();
            return;
        }

        ////updating in customerData
        //ReAssigned 2 means cancel job making un-assigned
        try {
            int maxAssignmentId = 0;
            Query = "SELECT  MAX(Id) FROM Assignment WHERE ComplaintId = " + Id;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                maxAssignmentId = rset.getInt(1);
            rset.close();
            stmt.close();


            Query = "UPDATE CustomerData SET AssignStatus=0,ReAssigned = 2 WHERE Id='" + Id + "' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            Query = "UPDATE Assignment SET CancelReAliveDate = NOW() WHERE Id = " + maxAssignmentId;
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", Action);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
            out.flush();
            out.close();
        } catch (Exception e) {
            out.println("ERROR " + e.getMessage());
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
