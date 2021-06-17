package HopOn;

import Parsehtm.Parsehtm;

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
public class TechnicianStatus extends HttpServlet {
    private String ScreenNo = "22";
    private String Query = "";
    private PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;
    private CallableStatement cStmt = null;

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
//                Class.forName("com.mysql.jdbc.Driver");
//                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var14) {
                conn = null;
                out.println("Exception excp conn: " + var14.getMessage());
            }
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }
            if (request.getParameter("ActionID") == null) {
                ActionID = "Home";
                return;
            }
            ActionID = request.getParameter("ActionID");
            if (ActionID.equals("GetTechStatus")) {
                GETSTATES(request, out, conn, context, UserId);
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

    private void GETSTATES(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        String Query1 = "";
        Statement stmt1 = null;
        ResultSet rset1 = null;
        StringBuilder TechnicianList = new StringBuilder();
        int LoginCount = 0;
        int SrlNo = 0;
        int ServerIndex = 0;
        //For Switching Image Directory Just Change below variable value
        //Live Server
        ServerIndex = 1;
        //Test Server
        //ServerIndex = 2;
        try {
            Query = "SELECT Id,UserName,UserId FROM MobileUsers WHERE Status=0 ORDER BY Id";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SrlNo++;
                Query1 = "SELECT count(*),IFNULL(DATE_FORMAT(CreatedDate,'%Y-%m-%d %H:%i:%s'),'0000-00-00'), " +
                        " IFNULL(TIMEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),DATE_FORMAT(CreatedDate,'%Y-%m-%d %H:%i:%s')),'00:00:00') " +
                        " FROM LoginTrail WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + rset.getString(3) + "')) AND UserType = 'M' ";
                stmt1 = conn.createStatement();
                rset1 = stmt1.executeQuery(Query1);
                if (rset1.next())
                    LoginCount = rset1.getInt(1);

                TechnicianList.append("<tr>");
                TechnicianList.append("<td align=left>" + SrlNo + "</td>\n");// Horn
                TechnicianList.append("<td align=left>" + rset.getString(2) + "</td>\n");// UserName
                TechnicianList.append("<td align=left>" + rset.getString(3) + "</td>\n");// UserId
                if (LoginCount == 0)
                    TechnicianList.append("<td><img src=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/images/OfflineImage.png width=30 height=20 ><br></td>");// Status
                else
                    TechnicianList.append("<td><img src=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/images/OnlineImage.png width=30 height=20 ><br></td>");// Status
                TechnicianList.append("<td align=left>" + rset1.getString(2) + "</td>\n");// LoginDate
                TechnicianList.append("<td align=left>" + rset1.getString(3) + "</td>\n");// Duration

                TechnicianList.append("</tr>");
                rset1.close();
                stmt1.close();
            }
            rset.close();
            stmt.close();


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("TechnicianList", TechnicianList.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/TechStatus.html");
            out.flush();
            out.close();
        } catch (Exception Ex) {
            try {
                Supportive.doLog(servletContext, "First Function", Ex.getMessage(), Ex);
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "TechnicianStatus");
                Parser.SetField("ActionID", "GetTechStatus");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
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
