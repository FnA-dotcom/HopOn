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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class CancelledJobs extends HttpServlet {
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        String UserId = "";
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();
        try {
            ServletContext context = null;
            context = getServletContext();
            try {
                boolean ValidSession = Supportive.checkSession(out, request);
                if (!ValidSession) {
                    out.flush();
                    out.close();
                    return;
                }
                UserId = Supportive.GetCookie("UserId", request);
                if (UserId == "") {
                    out.println("<font size=\"3\" face=\"Calibri\">Your session has been expired, please login again.</font>");
                    out.flush();
                    out.close();
                    return;
                }
//                Class.forName("com.mysql.jdbc.Driver");
//                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var11) {
                conn = null;
                out.println("Exception excp conn: " + var11.getMessage());
            }
            if (request.getParameter("ActionID") == null) {
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
            Action = request.getParameter("ActionID");
           /* if (Action.compareTo("ShowJobs") == 0) {
                ShowJobs(request, response, out, conn, context);
            } else if (Action.compareTo("CreateHistory") == 0) {
                CreateHistory(request, response, out, conn, context,UserId);
            } else {
                out.println("Under Development ... " + Action);
            }*/
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

/*    private void ShowJobs(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        String FormName = "CancelledJobs";
        String ActionID = "ShowJobs";
        String Form = "View Jobs";
        int SerialNo = 0;
        String ReplaceComplainNumber = "";
        StringBuilder CancelledJobs = new StringBuilder();
        try {
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,a.AssignDate,a.Make,a.Model,  "
                    + "b.UserId,b.ComplaintStatus,b.AssignmentStatus,b.Latitude,b.Longtitude,  "
                    + "c.UserName,c.UserId,IFNULL(d.ComplaintStatus,'No Status'),a.ComplainNumber,a.Id "
                    + "FROM CustomerData a " +
                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus=8 AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)" +
                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id  " +
                    " LEFT JOIN ComplaintStatus d ON b.ComplaintStatus = d.Id " +
                    " ORDER BY a.AssignDate DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(15).replace("#", "^");
                CancelledJobs.append("<tr>");
                CancelledJobs.append("<td align=left>" + SerialNo + "</td>\n");
                CancelledJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/HopOn/HopOn.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");
                CancelledJobs.append("<td align=left>" + rset.getString(15) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(1) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(2) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(3) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(4) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(5) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(6) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(14) + "</td>\n");
                CancelledJobs.append("<td align=left>" + rset.getString(12) + "</td>\n");
                CancelledJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/HopOn/HopOn.CancelledJobs?ActionID=CreateHistory&Id=" + rset.getString(16) + "&UserName=" + rset.getString(12) + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ ReAlive ] </a></td>");
                CancelledJobs.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("CancelledJobs", CancelledJobs.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/CancelJobs.html");
        } catch (Exception var14) {
            out.println("Unable to process the request..." + var14.getMessage());
        }
    }*/

    private void CreateHistory(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        String FormName = "CancelledJobs";
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
                pStmt.setString(2, rset.getString(2));//RegistrationNum
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
            out.println(e);
            out.close();
            out.flush();
            return;
        }

        ////updating in customerData
        try {
            Query = "UPDATE CustomerData SET AssignStatus=0,ReAssigned = 2 WHERE Id='" + Id + "'";

            try {
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
                out.println(String.valueOf(e.getMessage()));
                out.flush();
                out.close();
                return;
            }
        } catch (Exception e) {
            out.println(e);
            out.flush();
            out.close();
            return;
        }
    }

}
