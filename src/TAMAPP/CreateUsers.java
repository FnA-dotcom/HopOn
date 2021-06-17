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
public class CreateUsers extends HttpServlet {
    private String ScreenNo = "29";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.handleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.handleRequest(request, response);
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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


            if (Action.equals("GETINPUT")) {
                this.GetInput(request, out, conn, context, UserId);
            } else if (Action.compareTo("SaveUser") == 0) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else {
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


    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        String Query = "";
        PreparedStatement pStmt = null;
        Statement stmt = null;
        ResultSet rset = null;

        StringBuffer Designations = new StringBuffer();
        StringBuffer Departments = new StringBuffer();

        try {
            Query = "SELECT Id,Designation FROM Designation";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Designations.append("<option value=" + rset.getString(1) + ">" + rset.getString(2));
            }

            rset.close();
            stmt.close();


            Query = "SELECT Id,Department FROM Department";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Departments.append("<option value=" + rset.getString(1) + ">" + rset.getString(2));
            }

            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Designations", Designations.toString());
            Parser.SetField("Departments", Departments.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/CreateUser.html");

        } catch (Exception e) {
            out.println("Error1" + e.getMessage());
        }
    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        String Query = "";
        PreparedStatement pStmt = null;
        Statement stmt = null;
        ResultSet rset = null;

        String FormName = "CreateUsers";
        String ActionID = "SaveUser";

        String UserName = request.getParameter("UserName").trim();
        String Designation = request.getParameter("Designation");
        String Department = request.getParameter("Department").trim();
        String UserId1 = request.getParameter("UserId").trim();
        String Password = request.getParameter("Password").trim();
        String Status = "0";
        String ImageSource = "null";
        String Enabled = "1";

        try {

            StringBuffer Designations = new StringBuffer();
            StringBuffer Departments = new StringBuffer();

            try {
                Query = "SELECT Id,Designation FROM Designation";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    Designations.append("<option value=" + rset.getString(1) + ">" + rset.getString(2));
                }

                rset.close();
                stmt.close();


                Query = "SELECT Id,Department FROM Department";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    Departments.append("<option value=" + rset.getString(1) + ">" + rset.getString(2));
                }

                rset.close();
                stmt.close();
            } catch (Exception e) {
                out.println(e.getMessage());
            }

            pStmt = conn.prepareStatement(
                    "INSERT INTO SystemUsers (UserName,UserId,Password,Status,CreatedDate,ImageSource,Designation,Department,Enabled) " +
                            "VALUES (?,?,?,?,now(),?,?,?,?)");

            pStmt.setString(1, UserName);
            pStmt.setString(2, UserId1);
            pStmt.setString(3, Password);
            pStmt.setString(4, Status);
            pStmt.setString(5, ImageSource);
            pStmt.setString(6, Designation);
            pStmt.setString(7, Department);
            pStmt.setString(8, Enabled);

            pStmt.executeUpdate();
            pStmt.close();


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Designations", Designations.toString());
            Parser.SetField("Departments", Departments.toString());
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/CreateUser.html");

        } catch (Exception e) {
            out.println(e.getMessage());
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
