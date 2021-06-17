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
public class PasswordChange extends HttpServlet {
    private String ScreenNo = "32";

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
//        String connect_string = supp.GetConnectString();
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
            } catch (Exception excp) {
                conn = null;
                out.println("Exception excp conn: " + excp.getMessage());
            }
/*            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }*/
            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            Action = request.getParameter("ActionID");


            if (Action.equals("GETINPUT")) {
                this.GetInput(request, out, conn, context, UserId);
            } else if (Action.equals("ChangePassword")) {
                this.ChangePassword(request, out, conn, context, UserId);
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
        try {
            Parsehtm Parser = new Parsehtm(request);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/PasswordChange.html");
        } catch (Exception e) {
            out.println(e.getMessage());
        }
    }

    void ChangePassword(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        String FormName = "PasswordChange";
        String ActionID = "ChangePassword";

        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";

        try {
            UserId = Supportive.GetCookie("UserId", request);

            String nPassword1 = request.getParameter("nPassword1").trim();
            String nPassword2 = request.getParameter("nPassword2").trim();

            if (!nPassword1.equals(nPassword2)) {
                out.println("<br>Error No.: 0002");
                out.println("<br>Error Is : Password Mismatch, Please Try Again ...!!! \n\n\n</b>");
                out.println("<input class=\"buttonERP\" type=button name=Back Value=\"  Back  \" onclick=history.back()>");
                out.println("</form></body></html>");
                out.flush();
                out.close();
                return;
            }

            Query = "UPDATE SystemUsers SET Password = '" + nPassword1 + "' WHERE UserId = '" + UserId + "'";

            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName.toString());
            Parser.SetField("ActionID", ActionID.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/PasswordChange.html");
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

