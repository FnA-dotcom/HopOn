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
public class AddJobNature extends HttpServlet {
    private String ScreenNo = "11";
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
            else if (Action.equals("GetJobNatureInfo"))
                GetJobNature(request, out, conn, context, UserId);

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
        StringBuffer JobNature = new StringBuffer();

        try {
            Query = "SELECT Id,JobNature,(CASE WHEN Status=0 THEN 'Active' ELSE 'Inactive' END) " +
                    "FROM JobNature ORDER BY Id";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    JobNature.append("<tr>");
                    JobNature.append("<td>" + SrlNo + "</td>");
                    JobNature.append("<td>" + rset.getString(2) + "</td>");
                    JobNature.append("<td>" + rset.getString(3) + "</td>");
                    //JobNature.append("<td width=10%><button class='btn-xs btn btn-info mylink' value="+rset.getInt(1)+" target=NewFrame1><i class=\"fa fa-edit \"></i>[Edit]</button> </td>");
                    JobNature.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");

                    JobNature.append("</tr>");
                    SrlNo++;
                }
                rset.close();
                stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddJobNature");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "Add Job Type -01", e.getMessage(), e);
                out.flush();
                out.close();
            }


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("JobNature", JobNature.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddJobNature.html");
            out.flush();
            out.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddJobNature");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "Department 0000-01", e.getMessage(), e);
            out.flush();
            out.close();
        }
    }

    void GetJobNature(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        Query = "";
        stmt = null;
        rset = null;
        String Status = "";
        String chk = "", CountryName = "";
        String ID = request.getParameter("Indexptr").trim();
        int indexptr = Integer.parseInt(ID);

        try {
            Query = "Select Id,Department,Status from Department where Id=" + indexptr;
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {

                    CountryName = rset.getString(2);
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
                Supportive.doLog(this.getServletContext(), "Add Department 0000-01", e.getMessage(), e);
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
        out.println(String.valueOf(CountryName) + "|" + String.valueOf(ID) + "|" + String.valueOf(Status));
        out.flush();
        out.close();

    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        int count = 0;
        String chk = "";

        String Department = request.getParameter("Department").trim();
        String Status = request.getParameter("Status").trim();
        int index = request.getParameter("Indexptr") != null ? Integer.parseInt(request.getParameter("Indexptr").trim()) : 0;

        if (count == 0)

        {
            try {
                PreparedStatement MainReceipt = conn.prepareStatement(
                        "INSERT INTO Department (Department, Status, CreatedDate) " +
                                "VALUES (?,?,now())");
                MainReceipt.setString(1, Department);
                MainReceipt.setString(2, Status);
                MainReceipt.executeUpdate();
                MainReceipt.close();

                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/SuccessEmployee.html");

            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "AddDesignation-01", e.getMessage(), e);
                out.flush();
                out.close();
            }
        } else {
            try {
                PreparedStatement MainReceipt = conn.prepareStatement(
                        "Update Department SET Department = ?, Status = ?, CreatedDate=now()  " +
                                "WHERE Id = " + index);
                MainReceipt.setString(1, Department);
                MainReceipt.setString(2, Status);
                MainReceipt.executeUpdate();
                MainReceipt.close();

            } catch (Exception e) {
                chk = "13";
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "AddDesignation-01", e.getMessage(), e);
                out.flush();
                out.close();
            }
        }
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
