package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@SuppressWarnings("Duplicates")

public class TestDashboard extends HttpServlet {
    private String ScreenNo = "2";
    private String Query = "";
    private PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;
    private CallableStatement cStmt = null;
    private ResultSet rset1 = null;
    private String Query1 = "";

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
                conn = Supportive.getMysqlConn(this.getServletContext());
                if (conn == null) {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("Error", "Unable to connect. Our team is looking into it!");
                    Parser.GenerateHtml(out, String.valueOf(Supportive.GetHtmlPath(this.getServletContext())) + "index.html");
                    return;
                }
            } catch (Exception var14) {
                conn = null;
                out.println("Exception excp conn: " + var14.getMessage());
            }

            if (request.getParameter("ActionID") == null) {
                ActionID = "Home";
                return;
            }

            ActionID = request.getParameter("ActionID");
            if (ActionID.equals("GetValue")) {
                this.GetValue(request, out, conn, context, UserId, CityIndex, isAdmin);
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

    private void GetValue(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int cityIndex, int isAdmin) {
        Parsehtm Parser = new Parsehtm();
        try {
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Dashboards/dashboard_5.html");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
