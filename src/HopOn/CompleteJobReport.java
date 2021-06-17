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
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class CompleteJobReport extends HttpServlet {
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
        //String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = getServletContext();
        int CityIndex = 0;
        int isAdmin = 0;
        try {
            HttpSession session = request.getSession(true);
            if ((session.getAttribute("UserId") == null) || (session.getAttribute("UserId").equals(""))) {
                out.println(" <center><table cellpadding=3 cellspacing=2><tr><td bgcolor=\"#FFFFFF\">  <font color=green face=arial><b>Your Session Has Been Expired</b></font>  </td></tr></table>  <p>  <font face=arial size=+1><b><a href=/HopOn/index.html target=_top> Return to Login Page  </a></b></font> <br><font face=arial size=-2>(You will need to sign in again.)</font><br>  </center> ");
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
//                Class.forName("com.mysql.jdbc.Driver");
//                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var14) {
                conn = null;
                out.println("Exception excp conn: " + var14.getMessage());
            }
            if (request.getParameter("Action") == null) {
                Action = "Home";
                return;
            }
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

            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }

            Action = request.getParameter("Action");

            if (Action.compareTo("ShowCompleteReport") == 0) {
                ShowCompleteReport(request, out, conn, context, CityIndex, isAdmin);
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

    private void ShowCompleteReport(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, int CityIndex, int isAdmin) {
        String FormName = "CompleteJobReports";
        String ActionID = "ShowCompleteReport";
        String Form = "Complete Jobs";
        int SerialNo = 0;
        String ReplaceComplainNumber = "";
        StringBuilder CompleteJobs = new StringBuilder();
        int ServerIndex = 0;
        //For Switching Image Directory Just Change below variable value
        //Live Server
        ServerIndex = 1;
        //Test Server
        //ServerIndex = 2;

        try {
            if (isAdmin == 0) {
                Query = " SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s') ,a.Make,a.Model,\n" +
                        "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,\n" +
                        "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType,\n" +
                        "DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address,\n" +
                        "CASE  WHEN b.Distance IS NULL AND b.ManualStatus = 1 THEN 'Manual' \n" +
                        "\t\t\tWHEN b.Distance IS NOT NULL AND b.TransferFlag = 1 THEN 'Transfer'\n" +
                        "ELSE b.Distance \n" +
                        "END\n" +
                        "FROM CustomerData a\n" +
                        "STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus = 6 \n" +
                        "AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)\n" +
                        "STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id\n" +
                        "LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id\n" +
                        "STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                        "WHERE a.CityIndex = " + CityIndex + " \n" +
                        "ORDER BY a.AssignDate DESC ";
            } else {
                Query = " SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'%Y-%m-%d %H:%i:%s') ,a.Make,a.Model,\n" +
                        "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,\n" +
                        "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,e.JobType,\n" +
                        "DATE_FORMAT(b.CreatedDate,'%Y-%m-%d %H:%i:%s'),a.Address,\n" +
                        "CASE  WHEN b.Distance IS NULL AND b.ManualStatus = 1 THEN 'Manual' \n" +
                        "\t\t\tWHEN b.Distance IS NOT NULL AND b.TransferFlag = 1 THEN 'Transfer'\n" +
                        "ELSE b.Distance \n" +
                        "END\n" +
                        "FROM CustomerData a\n" +
                        "STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus = 6 \n" +
                        "AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)\n" +
                        "STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id\n" +
                        "LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id\n" +
                        "STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                        "ORDER BY a.AssignDate DESC ";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                CompleteJobs.append("<tr>");
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + (SerialNo++) + "</td>\n");// SerialNo
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(15) + "</td>\n");// ComplainNumber
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(3) + "</td>\n");// CustName
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(5) + "</td>\n");// Make
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(6) + "</td>\n");// Model
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(14) + "</td>\n");// ComplaintStatus
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(16) + "</td>\n");// JobType
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(17) + "</td>\n");// StatusDate
                CompleteJobs.append("<td bgcolor=#A2DA62 align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/HopOn/HopOn.ManualAssignment?ActionID=GetTechLocation&TechId=" + rset.getString(13) + " \" target=\"_self\"> <small>" + rset.getString(12) + "</small> </a></td>");//Technician
                CompleteJobs.append("<td bgcolor=#A2DA62 align=center><a class=\"btn-sm btn btn-info\" href=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");//Location
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(18) + "</td>\n");// Address
                CompleteJobs.append("<td bgcolor=#A2DA62 align=left>" + rset.getString(19) + "</td>\n");// Distance
                CompleteJobs.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("CompleteJobs", CompleteJobs.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowCompleteJobs.html");
        } catch (Exception Ex) {
            out.println("Error While Fetching Records " + Ex.getMessage());
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
