package TAMAPP;

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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class ViewAllJobs extends HttpServlet {
    private String ScreenNo = "20";
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
        String connect_string = supp.GetConnectString();

        try {
            ServletContext context = null;
            context = this.getServletContext();

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

                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var11) {
                conn = null;
                out.println("Exception excp conn: " + var11.getMessage());
            }

            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }

            if (request.getParameter("Action") == null) {
                Action = "Home";
                return;
            }

            Action = request.getParameter("Action");
            if (Action.compareTo("ViewAllJobs") == 0) {
                ViewJobs(request, response, out, conn, context);
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

    private void ViewJobs(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        String FormName = "ViewAllJobs";
        String ActionID = "ViewAllJobs";
        String Form = "View All Jobs";
        int SerialNo = 0;
        String ReplaceComplainNumber = "";
        StringBuilder ViewAllJobs = new StringBuilder();

        try {
            Query = "SELECT a.RegistrationNum,a.ChesisNo,a.CustName,DATE_FORMAT(a.AssignDate,'0000-00-00'),a.Make,a.Model, " +
                    " a.ComplainNumber,e.JobType,a.CellNo,a.PhoneNo,a.Color, a.Address,a.DeviceNo,a.Insurance,f.JobNature " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN JobType e ON a.JobTypeIndex = e.Id " +
                    " STRAIGHT_JOIN JobNature f ON a.JobNatureIndex = f.Id " +
                    " ORDER BY a.Id DESC ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SerialNo++;
                ReplaceComplainNumber = rset.getString(7).replace("#", "^");
                ViewAllJobs.append("<tr>");
                ViewAllJobs.append("<td align=left>" + SerialNo + "</td>\n");// SerialNo
                ViewAllJobs.append("<td align=left>" + rset.getString(7) + "</td>\n");// ComplainNumber
                ViewAllJobs.append("<td align=left>" + rset.getString(1) + "</td>\n");// RegistrationNum
                ViewAllJobs.append("<td align=left>" + rset.getString(2) + "</td>\n");// ChesisNo
                ViewAllJobs.append("<td align=left>" + rset.getString(3) + "</td>\n");// CustName
                ViewAllJobs.append("<td align=left>" + rset.getString(4) + "</td>\n");// AssignDate
                ViewAllJobs.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                ViewAllJobs.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                ViewAllJobs.append("<td align=left>" + rset.getString(8) + "</td>\n");// JobType
                ViewAllJobs.append("<td align=left>" + rset.getString(9) + "</td>\n");// CellNo
                ViewAllJobs.append("<td align=left>" + rset.getString(10) + "</td>\n");// PhoneNo
                ViewAllJobs.append("<td align=left>" + rset.getString(11) + "</td>\n");// Color
                ViewAllJobs.append("<td align=left>" + rset.getString(12) + "</td>\n");// Address
                ViewAllJobs.append("<td align=left>" + rset.getString(13) + "</td>\n");// DeviceNo
                ViewAllJobs.append("<td align=left>" + rset.getString(14) + "</td>\n");// Insurance
                ViewAllJobs.append("<td align=left>" + rset.getString(15) + "</td>\n");// JobNature
                ViewAllJobs.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.ComplainLocation?Action=ViewLocationReport&Flag=1&ComplainNumber=" + ReplaceComplainNumber + " target=NewFrame1><i class=\"fa fa-edit\"></i> [ View ] </a></td>");

                ViewAllJobs.append("</tr>");

            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.SetField("ViewAllJobs", ViewAllJobs.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ShowAllJobs.html");
        } catch (Exception var14) {
            out.println("Unable to process the request..." + var14.getMessage());
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
