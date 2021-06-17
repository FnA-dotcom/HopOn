package TAMAPP;

/**
 * Created by Siddiqui on 9/13/2017.
 */

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
public class AddJobType extends HttpServlet {
    private String ScreenNo = "10";
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        handleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        handleRequest(request, response);
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        String Action = null;

        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = getServletContext();
        String UserId;
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
            } catch (Exception excp) {
                conn = null;
                out.println("Exception excp conn: " + excp.getMessage());
            }
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }

            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            Action = request.getParameter("ActionID");

            if (Action.equals("GETINPUT"))
                GETINPUT(request, out, conn, context, UserId);
            else if (Action.equals("SaveRecords"))
                SaveRecords(request, out, conn, context, UserId);
            else if (Action.equals("GetJobTypeInfo"))
                GetJobTypeInfo(request, out, conn, context, UserId);

            else {
                out.println("Under Development ... " + Action);
            }
            conn.close();
        } catch (Exception e) {
            out.println("Exception in main... " + e.getMessage());
            out.flush();
            out.close();
            return;
        }
        out.flush();
        out.close();
    }

    void GETINPUT(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        int SrlNo = 1;
        Query = "";
        StringBuffer JobType = new StringBuffer();

        try {
            Query = "SELECT Id,JobType,(CASE WHEN Status=0 THEN 'Active' ELSE 'Inactive' END) " +
                    "FROM JobType ORDER BY Id";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    JobType.append("<tr>");
                    JobType.append("<td>" + SrlNo + "</td>");
                    JobType.append("<td>" + rset.getString(2) + "</td>");
                    JobType.append("<td>" + rset.getString(3) + "</td>");
                    //JobType.append("<td width=10%><button class='btn-xs btn btn-info mylink' value=" + rset.getInt(1) + " target=NewFrame1><i class=\"fa fa-edit \"></i>[Edit]</button> </td>");
                    JobType.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");

                    JobType.append("</tr>");
                    SrlNo++;
                }
                rset.close();
                stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddJobType");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "Add Job Type -01", e.getMessage(), e);
                out.flush();
                out.close();
            }


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("JobType", JobType.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddJobType.html");
            out.flush();
            out.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddJobType");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "Job Type 0000-01", e.getMessage(), e);
            out.flush();
            out.close();
        }
    }

    void GetJobTypeInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        Query = "";
        stmt = null;
        rset = null;
        String Status = "";
        String chk = "", JobType = "";
        Query = request.getParameter("JobTypeIndex").trim();
        int JobTypeIndex = Integer.parseInt(Query);

        try {
            Query = "Select Id,JobType,Status from JobType where Id=" + JobTypeIndex;
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    JobType = rset.getString(2).trim();
                    Status = rset.getString(3);
                }
                rset.close();
                stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "Fetch Job Type", e.getMessage(), e);
                out.flush();
                out.close();
            }

        } catch (Exception e) {
            chk = "13";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
            return;
        }
        out.println(JobType + "|" + String.valueOf(JobTypeIndex) + "|" + String.valueOf(Status));
        out.flush();
        out.close();

    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        String chk = "";

        String JobType = request.getParameter("JobType").trim();
        String Status = request.getParameter("Status").trim();
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);

        if (count == 0) {
            try {
                PreparedStatement MainReceipt = conn.prepareStatement(
                        "INSERT INTO JobType (JobType, Status, CreatedDate) " +
                                "VALUES (?,?,now())");
                MainReceipt.setString(1, JobType);
                MainReceipt.setString(2, Status);
                MainReceipt.executeUpdate();
                MainReceipt.close();

            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddJobType");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "AddJobType-01", e.getMessage(), e);
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("JobTypeIndex").trim();
            int JobTypeIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE JobType SET JobType='" + JobType + "',Status=" + Status + " " +
                        " WHERE Id = " + JobTypeIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddJobType");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "Edit Job Type-02", e.getMessage(), e);
                out.flush();
                out.close();
            }
        }
        chk = "1";
        out.println(chk);
        out.flush();
        out.close();
        return;
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
