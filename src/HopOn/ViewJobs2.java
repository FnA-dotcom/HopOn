package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class ViewJobs2 extends HttpServlet {
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        String UserId = "";
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();

        try {
            ServletContext context = null;
            context = this.getServletContext();

            try {
                conn = Supportive.getMysqlConn(this.getServletContext());
                if (conn == null) {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("Error", "Unable to connect. Our team is looking into it!");
                    Parser.GenerateHtml(out, String.valueOf(Supportive.GetHtmlPath(this.getServletContext())) + "index.html");
                    return;
                }
            } catch (Exception excp) {
                conn = null;
                out.println("Exception excp conn: " + excp.getMessage());
            }


            if (request.getParameter("Action") == null) {
                Action = "Home";
                return;
            }

            Action = request.getParameter("Action");
            if (Action.compareTo("WLF_ShowJobs") == 0) {
                WLF_ShowJobs(request, response, out, conn, context);
            } else if (Action.compareTo("LocationJobs") == 0) {
                LocationJobs(request, response, out, conn, context);
            } else if (Action.compareTo("GetTechLocation") == 0) {
                GetTechLocation(request, out, conn, context);
            } else {
                out.println("Under Development ... " + Action);
            }

            conn.close();

            out.close();
            out.flush();
        } catch (Exception Ex) {
            out.println("Exception in main... " + Ex.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private void WLF_ShowJobs(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        String FormName = "ViewJobs2";
        String ActionID = "WLF_ShowJobs";
        String Form = "View Jobs";
        int SerialNo = 0;
        String ReplaceComplainNumber = "";
        StringBuilder AJs = new StringBuilder();
        StringBuilder UAJs = new StringBuilder();
        StringBuilder PJs = new StringBuilder();
        StringBuilder CJs = new StringBuilder();
        int ServerIndex = 0;
        //For Switching Image Directory Just Change below variable value
        //Live Server
        ServerIndex = 1;
        //Test Server
        //ServerIndex = 2;
        String SecurityToken = request.getParameter("SecurityToken").trim();

        if (SecurityToken.equals("") || SecurityToken.length() < 1) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ViewJobs2");
                Parser.SetField("ActionID", "WLF_ShowJobs");
                Parser.SetField("Message", "Security Token is not provided");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
            } catch (Exception e) {
                out.println("Error3" + e.getMessage());
            }
            return;
        }

        if (!SecurityToken.equals("FSkrs4ksdk4kpew2123mFdFeqertQ*53jj$0kmqzRdzwq")) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "ViewJobs2");
                Parser.SetField("ActionID", "WLF_ShowJobs");
                Parser.SetField("Message", "Please do not change Security Token!! Use provided token");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
            } catch (Exception e) {
                out.println("Error3" + e.getMessage());
            }
            return;
        }


        try {

            //old Query
//            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model, " +
//                    " b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude, " +
//                    " c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType" +
//                    " FROM CustomerData a " +
//                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus NOT IN(7,8) AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) " +
//                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
//                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
//                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
//                    " ORDER BY a.AssignDate DESC ";

            //new Query
/*            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model, " +
                    " b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude, " +
                    " c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType" +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus NOT IN(7,8) " +
                    " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) " +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " ORDER BY a.AssignDate DESC ";*/
//            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s') ,a.Make,a.Model, " +
//                    " b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude, " +
//                    " c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType," +
//                    " DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address, " +
//                    " CASE  WHEN b.Distance IS NULL THEN 'Manual/Transfer' ELSE b.Distance END " +
//                    " FROM CustomerData a " +
//                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus NOT IN(7,8) " +
//                    " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) " +
//                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
//                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
//                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
//                    " ORDER BY a.AssignDate DESC ";
            Query = " SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s') ,a.Make,a.Model,\n" + //6
                    "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,\n" + //11
                    "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType,\n" +//16
                    "DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address,\n" + //18
                    "CASE  WHEN b.Distance IS NULL AND b.ManualStatus = 1 THEN 'Manual' \n" +
                    "\t\t\tWHEN b.Distance IS NOT NULL AND b.TransferFlag = 1 THEN 'Transfer'\n" +
                    "ELSE b.Distance \n" +
                    "END\n" +
                    "FROM CustomerData a\n" +
                    "STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus NOT IN(7,8)\n" +
                    "AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)\n" +
                    "STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id\n" +
                    "LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id\n" +
                    "STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id\n" +
                    "ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                if (rset.getString(14).equals("Job Completed")) {
                    AJs.append("<tr>");
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + SerialNo + "</td>\n");// SerialNo
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                    // AJs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Complain Location' ><a href = \"/HopOn/HopOn.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=\"_self\"> " + rset.getString(15) + " </a></td>");

                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(3) + "</td>\n");// CustName
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(5) + "</td>\n");// Make
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(6) + "</td>\n");// Model
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(16) + "</td>\n");// JobType
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(17) + "</td>\n");// StatusDate
                    AJs.append("<td bgcolor=#A2DA62 align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/HopOn/HopOn.ViewJobs2?Action=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                    AJs.append("<td bgcolor=#A2DA62 align=center><a class=\"btn-sm btn btn-info\" href=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.ViewJobs2?Action=LocationJobs&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(18) + "</td>\n");// Address
                    AJs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(19) + "</td>\n");// Distance
                    AJs.append("</tr>");

                } else {
                    AJs.append("<tr>");
                    AJs.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                    AJs.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                    // AJs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Complain Location' ><a href = \"/HopOn/HopOn.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=\"_self\"> " + rset.getString(15) + " </a></td>");

                    AJs.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                    AJs.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                    AJs.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                    AJs.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                    AJs.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                    AJs.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                    AJs.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                    AJs.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                    AJs.append("<td align=left>" + rset.getString(17) + "</td>\n");// StatusDate
                    AJs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/HopOn/HopOn.ViewJobs2?Action=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                    AJs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.ViewJobs2?Action=LocationJobs&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                    AJs.append("<td align=left>" + rset.getString(18) + "</td>\n");// Address
                    AJs.append("<td align=left>" + rset.getString(19) + "</td>\n");// Distance
                    AJs.append("</tr>");

                }
            }
            rset.close();
            stmt.close();

            SerialNo = 0;
            //Old Query
//            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,IFNULL(a.AssignDate,'0000-00-00'),a.Make," +
//                    "a.Model,a.Longtitude,a.Latitude,a.ComplainNumber,e.JobType " +
//                    " FROM CustomerData a " +
//                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
//                    " WHERE a.AssignStatus = 0 " +
//                    " ORDER BY a.CreatedDate DESC";
            //new Query
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
                ReplaceComplainNumber = rset.getString(9).replace("#", "^");
                UAJs.append("<tr>");
                UAJs.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                UAJs.append("<td align=left>" + rset.getString(9) + "</td>\n");// ComplainNumber
                UAJs.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                UAJs.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                UAJs.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                UAJs.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                UAJs.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                UAJs.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                UAJs.append("<td align=left>" + rset.getString(10) + "</td>\n");// JobType
                UAJs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.ViewJobs2?Action=LocationJobs&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                UAJs.append("<td align=left>" + rset.getString(11) + "</td>\n");// Address
                UAJs.append("</tr>");
            }
            rset.close();
            stmt.close();

            //postponed
            SerialNo = 0;
            //Old Query
//            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model, " +
//                    " b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude, " +
//                    " c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType " +
//                    " FROM CustomerData a " +
//                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus = 7 AND b.Status = 0 AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)" +
//                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
//                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
//                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
//                    " ORDER BY a.AssignDate DESC ";
            //new Query
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model, " +
                    " b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude, " +
                    " c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber," +
                    "e.JobType,DATE_FORMAT(b.CreatedDate,'%Y-%m-%d  %H:%i:%s'),a.Id,c.UserName,a.Address,f.PostponedReason " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus = 7 " +
                    " AND b.Status = 0 AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)" +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " LEFT JOIN PostponedReasons f ON a.Id = f.JobId AND " +
                    " f.Id = (SELECT MAX(x.Id) FROM PostponedReasons x WHERE x.JobId = a.Id ) " +
                    " ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                PJs.append("<tr>");
                PJs.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                PJs.append("<td align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                PJs.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                PJs.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                PJs.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                PJs.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                PJs.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                PJs.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                PJs.append("<td align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                PJs.append("<td align=left>" + rset.getString(16) + "</td>\n");// JobType
                PJs.append("<td align=left>" + rset.getString(17) + "</td>\n");// PostponedDate
                PJs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/HopOn/HopOn.ViewJobs2?Action=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                PJs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.ViewJobs2?Action=LocationJobs&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                PJs.append("<td align=left>" + rset.getString(20) + "</td>\n");// Address
                PJs.append("<td align=left>" + rset.getString(21) + "</td>\n");// PostponedReason
                PJs.append("</tr>");

            }
            rset.close();
            stmt.close();

            SerialNo = 0;
            //old Query
//            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model,  "
//                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
//                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,a.Id,e.JobType "
//                    + "FROM CustomerData a " +
//                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus=8 AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)" +
//                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
//                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
//                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
//                    " ORDER BY a.AssignDate DESC ";

            //new Query
/*            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model,  "
                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,a.Id,e.JobType "
                    + "FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus=8 " +
                    " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) " +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " ORDER BY a.AssignDate DESC ";*/
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s'),a.Make,a.Model,  "
                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,a.Id,e.JobType," +
                    " DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address, " +
                    "CASE WHEN \n" +
                    "(SELECT COUNT(*) FROM ApiChange c WHERE c.ComplaintId = a.Id) = 1 THEN 'Back To Report' ELSE\n" +
                    "(CASE WHEN b.CancelledRemarks IS NULL THEN 'N/A' ELSE b.CancelledRemarks END) END APIRemarks\n" +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus=8 " +
                    " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) " +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " ORDER BY a.AssignDate DESC ";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                CJs.append("<tr>");
                CJs.append("<td align=left>" + SerialNo + "</td>\n");
                CJs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.ViewJobs2?Action=LocationJobs&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                CJs.append("<td align=left>" + rset.getString(15) + "</td>\n");
                CJs.append("<td align=left>" + rset.getString(1) + "</td>\n");
                CJs.append("<td align=left>" + rset.getString(2) + "</td>\n");
                CJs.append("<td align=left>" + rset.getString(3) + "</td>\n");
                CJs.append("<td align=left>" + rset.getString(4) + "</td>\n");
                CJs.append("<td align=left>" + rset.getString(5) + "</td>\n");
                CJs.append("<td align=left>" + rset.getString(6) + "</td>\n");
                CJs.append("<td align=left>" + rset.getString(14) + "</td>\n");
                CJs.append("<td align=left>" + rset.getString(17) + "</td>\n");// JobType
                CJs.append("<td align=left>" + rset.getString(18) + "</td>\n");
                CJs.append("<td align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/HopOn/HopOn.ViewJobs2?Action=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");
                CJs.append("<td align=left>" + rset.getString(19) + "</td>\n");//Address
                CJs.append("<td align=left>" + rset.getString(20) + "</td>\n");//CancelledRemarks
                CJs.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("AJs", AJs.toString());
            Parser.SetField("UAJs", UAJs.toString());
            Parser.SetField("PJs", PJs.toString());
            Parser.SetField("CJs", CJs.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/SJ2.html");
        } catch (Exception var14) {
            out.println("Unable to process the request..." + var14.getMessage());
        }
    }

    private void LocationJobs(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        String ComplainNumber = "";
        String CustomerName = "";
        int SNo = 0;
        String lat = "";
        String lon = "";
        String URL = "";
        String Time = "";
        StringBuilder L = new StringBuilder();
        Query = request.getParameter("Flag").trim();
        int Flag = Integer.parseInt(Query);

        if (Flag == 1) {
            ComplainNumber = request.getParameter("ComplainNumber").trim();
            ComplainNumber = ComplainNumber.replace("^", "#");
        }
        try {
            Query = " SELECT ifnull(a.Latitude,'-'),ifnull(a.Longtitude,'-'), DATE_FORMAT(a.CreatedDate,'%d-%m-%Y %H:%i:%s'), a.CustName " +
                    "FROM CustomerData a WHERE a.ComplainNumber = '" + ComplainNumber + "' ";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            while (rset.next()) {
                ++SNo;
                lat = rset.getString(1).trim();
                lon = rset.getString(2).trim();
                Time = rset.getString(3).trim();
                CustomerName = rset.getString(4);
                if (!rset.getString(1).equals("-") && !rset.getString(2).equals("-") &&
                        !rset.getString(1).equals("null") && !rset.getString(2).equals(null) &&
                        !lat.isEmpty() && !lon.isEmpty() && !lat.equals(null) && !lon.equals(null)) {
                    L.append("['" + rset.getString(4) + "',");
                    L.append(rset.getString(1) + ",");
                    L.append(rset.getString(2) + ",");
                    L.append(SNo + "],");
                }
            }

            rset.close();
            stmt.close();
            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Location", L.toString());
            Parser.SetField("URL", URL);
            Parser.SetField("lat", lat);
            Parser.SetField("lon", lon);
            Parser.SetField("CustomerName", CustomerName);
            Parser.SetField("time", Time);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ViewComplainLocation.html");
        } catch (Exception var35) {
            out.println("Unable to process the request..." + var35.getMessage());
        }
    }

    private void GetTechLocation(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {
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
}
