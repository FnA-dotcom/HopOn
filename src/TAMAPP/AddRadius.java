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
public class AddRadius extends HttpServlet {
    private String ScreenNo = "15";
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
            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }

            Action = request.getParameter("ActionID");

            if (Action.equals("GetRadiusInput"))
                GetRadiusInput(request, out, conn, context, UserId);
            else if (Action.equals("EditRadiusRecords"))
                EditRadiusRecords(request, out, conn, context, UserId);
            else if (Action.equals("ShowEditRadius"))
                ShowEditRadius(request, out, conn, context, UserId);
        } catch (Exception e) {
            out.println("Exception in main... " + e.getMessage());
            out.flush();
            out.close();
            return;
        }
        out.flush();
        out.close();
    }

    private void GetRadiusInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        int SrlNo = 1;
        Query = "";

        StringBuilder Radius = new StringBuilder();

        try {

            Query = "SELECT Radius,FalconRadius,FalconLat,FalconLon,Id FROM RadiusCheck";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Radius.append("<tr>");
                Radius.append("<td>" + SrlNo + "</td>");
                Radius.append("<td>" + rset.getString(1) + "</td>"); //Radius
                Radius.append("<td>" + rset.getString(2) + "</td>"); //FalconRadius
                Radius.append("<td>" + rset.getString(3) + "</td>"); //FalconLat
                Radius.append("<td>" + rset.getString(4) + "</td>"); //FalconLon
                Radius.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(5) + ")\"></i></td>");

                Radius.append("</tr>");
                SrlNo++;
            }
            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Radius", Radius.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddRadius.html");
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddRadius");
                Parser.SetField("ActionID", "GetRadiusInput");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "Add Radius 0000-01", e.getMessage(), e);
            out.flush();
            out.close();
        }
    }

    private void EditRadiusRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        String chk = "";

        String Radius = request.getParameter("Radius").trim();
        String FalconRadius = request.getParameter("FalconRadius").trim();
        Query = request.getParameter("RadiusIndex").trim();
        int RadiusIndex = Integer.parseInt(Query);

        try {
            Query = "SELECT Radius,Status,CreatedDate,FalconRadius,FalconLat,FalconLon,UpdatedBy,UpdatedDate FROM RadiusCheck ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                pStmt = conn.prepareStatement("INSERT INTO RadiusCheckHistory (Radius, Status, CreatedDate, FalconRadius, FalconLat, " +
                        "FalconLon, UpdatedBy, UpdatedDate) VALUES (?,?,?,?,?,?,?,?)");
                pStmt.setString(1, rset.getString(1));
                pStmt.setString(2, rset.getString(2));
                pStmt.setString(3, rset.getString(3));
                pStmt.setString(4, rset.getString(4));
                pStmt.setString(5, rset.getString(5));
                pStmt.setString(6, rset.getString(6));
                pStmt.setString(7, rset.getString(7));
                pStmt.setString(8, rset.getString(8));

                pStmt.executeUpdate();
                pStmt.close();
            }
            rset.close();
            stmt.close();

            Query = "UPDATE RadiusCheck SET Radius='" + Radius + "',FalconRadius='" + FalconRadius + "',UpdatedBy = '" + UserId + "',UpdatedDate = NOW() " +
                    " WHERE Id = " + RadiusIndex;
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddRadius");
                Parser.SetField("ActionID", "GetRadiusInput");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var15) {
            }

            Supportive.doLog(this.getServletContext(), "Edit Radius-02", e.getMessage(), e);
            out.flush();
            out.close();
        }
        chk = "1";
        out.println(chk);
        out.flush();
        out.close();
        return;
    }

    private void ShowEditRadius(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String Status = "";
        String chk = "", Radius = "";
        String FalconRadius = "";
        Query = request.getParameter("RadiusIndex").trim();
        int RadiusIndex = Integer.parseInt(Query);

        try {
            Query = "SELECT Id,Radius,FalconRadius FROM RadiusCheck where Id=" + RadiusIndex;
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    Radius = rset.getString(2).trim();
                    FalconRadius = rset.getString(3);
                }
                rset.close();
                stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "Fetch Radius", e.getMessage(), e);
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
        out.println(Radius + "|" + String.valueOf(FalconRadius) + "|" + String.valueOf(RadiusIndex));
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
