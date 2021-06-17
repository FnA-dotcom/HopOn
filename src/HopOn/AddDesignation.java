package HopOn;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class AddDesignation extends HttpServlet {
    String ScreenNo = "8";
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;

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
        //String connect_string = supp.GetConnectString();
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
            } else if (Action.equals("SaveRecords")) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if (Action.equals("GetDesignationInfo")) {
                this.GetDesignationInfo(request, out, conn, context, UserId);
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

    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";
        StringBuffer DesignationList = new StringBuffer();

        try {
            Query = "SELECT Id,Designation,(CASE WHEN Status=0 THEN 'Active' ELSE 'Inactive' END) " +
                    "FROM Designation ORDER BY Id";

            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (true) {
                    if (!rset.next()) {
                        rset.close();
                        stmt.close();
                        break;
                    }

                    DesignationList.append("<tr>");
                    DesignationList.append("<td>" + rset.getInt(1) + "</td>");
                    DesignationList.append("<td>" + rset.getString(2) + "</td>");
                    DesignationList.append("<td>" + rset.getString(3) + "</td>");
                    DesignationList.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");
                    //DesignationList.append("<td width=10%><button class='btn-xs btn btn-info mylink' value=" + rset.getInt(1) + " target=NewFrame1><i class=\"fa fa-edit \"></i>[Edit]</button> </td>");
                    DesignationList.append("</tr>");
                }
            } catch (Exception var16) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddDesignation");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var17) {
                }

                Supportive.doLog(this.getServletContext(), "Add Designation-01", var16.getMessage(), var16);
                out.flush();
                out.close();
                return;
            }

            Parsehtm e = new Parsehtm(request);
            e.SetField("DesignationList", DesignationList.toString());
            e.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddDesignation.html");
            out.flush();
            out.close();
        } catch (Exception var172) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddDesignation");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var171) {
            }
            Supportive.doLog(this.getServletContext(), "Add Designation-02", var172.getMessage(), var172);
            out.flush();
            out.close();
            return;
        }

    }

    void GetDesignationInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String chk = "";
        String DesignationName = "";
        Query = request.getParameter("DesignationIndex").trim();
        int DesignationIndex = Integer.parseInt(Query);
        String Status = "";

        try {
            Query = "Select Id,Designation,Status from Designation where Id=" + DesignationIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            while (true) {
                if (!rset.next()) {
                    rset.close();
                    stmt.close();
                    break;
                }

                DesignationName = rset.getString(2);
                Status = rset.getString(3);
            }
        } catch (Exception var32) {
            chk = "13";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
            return;
        }

        out.println(DesignationName + "|" + DesignationIndex + "|" + Status);
        out.flush();
        out.close();
    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        String chk = "";

        String Designation = request.getParameter("Designation").trim();
        String Status = request.getParameter("Status").trim();
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);

        if (count == 0) {
            try {
                pStmt = conn.prepareStatement(
                        "INSERT INTO Designation (Designation, Status, CreatedDate) VALUES (?,?,now())");
                pStmt.setString(1, Designation);
                pStmt.setString(2, Status);
                pStmt.executeUpdate();
                pStmt.close();

            } catch (Exception var36) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddDesignation");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "Save Designation ", var36.getMessage(), var36);
                out.flush();
                out.close();
                return;
            }
        } else {
            Query = request.getParameter("DesignationIndex").trim();
            int DesignationIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE Designation SET Designation='" + Designation + "',Status=" + Status + " " +
                        " WHERE Id = " + DesignationIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddDesignation");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "Edit Designation-02", e.getMessage(), e);
                out.flush();
                out.close();
            }
        }

        chk = "1";
        out.println(String.valueOf(chk));
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
