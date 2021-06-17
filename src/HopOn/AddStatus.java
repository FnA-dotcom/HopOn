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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class AddStatus extends HttpServlet {
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;
    String Query = "";
    private String ScreenNo = "13";

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
        ServletContext context = null;
        context = this.getServletContext();
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
                this.GetInput(request, out, conn, context);
            } else if (Action.equals("SaveRecords")) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if (Action.equals("GetStatusInfo")) {
                this.GetStatusInfo(request, out, conn, context);
            } else {
                out.println("Under Development ... " + Action);
            }

        } catch (Exception var15) {
            out.println("Exception in main... " + var15.getMessage());
            out.flush();
            out.close();
            return;
        }

        out.flush();
        out.close();
    }

    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuffer StatusList = new StringBuffer();
        int SerialNo = 1;
        try {
            Query = "SELECT Id,ComplaintStatus,(CASE WHEN Status=0 THEN \'Active\' ELSE \'InActive\' END) \n " +
                    "FROM ComplaintStatus  ORDER BY Id";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (rset.next()) {
                    StatusList.append("<tr>");
                    StatusList.append("<td width=02%>" + SerialNo + "</td>");
                    StatusList.append("<td width=10%> " + rset.getString(2) + "</td>");
                    StatusList.append("<td width=10%> " + rset.getString(3) + "</td>");
                    StatusList.append("<td width=10%><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");
                    StatusList.append("</tr>");
                    SerialNo++;
                }
            } catch (Exception var16) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "AddStatus-01", var16.getMessage(), var16);
                out.flush();
                out.close();
            }

            Parsehtm e = new Parsehtm(request);
            e.SetField("StatusList", StatusList.toString());
            e.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddStatus.html");
            out.flush();
            out.close();
        } catch (Exception var17) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }

            Supportive.doLog(this.getServletContext(), "AddStatus-02", var17.getMessage(), var17);
            out.flush();
            out.close();
        }

    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        String chk = "";
        String StatusName = request.getParameter("StatusName").trim();
        String Status = request.getParameter("Status").trim();
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);

        if (count == 0) {
            try {
                pStmt = conn.prepareStatement("INSERT INTO ComplaintStatus (ComplaintStatus, Status, CreateDate) " +
                        "VALUES (?,?,now())");
                pStmt.setString(1, StatusName);
                pStmt.setString(2, Status);
                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception var35) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "SaveStatus-01", var35.getMessage(), var35);
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("StatusIndex").trim();
            int StatusIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE ComplaintStatus SET ComplaintStatus='" + StatusName + "',Status=" + Status + " WHERE Id = " + StatusIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception var34) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "EditStatus-02", var34.getMessage(), var34);
                out.flush();
                out.close();
            }
        }
        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();
    }

    void GetStatusInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {

        String Query = "";
        stmt = null;
        rset = null;
        String Status = "";
        String chk = "";
        String StatusName = "";
        Query = request.getParameter("StatusIndex").trim();
        int StatusIndex = Integer.parseInt(Query);

        try {
            Query = "Select Id,ComplaintStatus,Status from ComplaintStatus where Id=" + StatusIndex;

            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (rset.next()) {

                    StatusName = rset.getString(2);
                    Status = rset.getString(3);
                }
            } catch (Exception var31) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "StatusInfo-01", var31.getMessage(), var31);
                out.flush();
                out.close();
            }
        } catch (Exception var32) {
            chk = "13";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
            return;
        }

        out.println(StatusName + "|" + StatusIndex + "|" + Status);
        out.flush();
        out.close();
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
