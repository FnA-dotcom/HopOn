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
public class AddSchool extends HttpServlet {
    String ScreenNo = "7";
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
//        String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = this.getServletContext();
        String UserId;
        int CityIndex = 0;
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
            if (Action.equals("GetInfo")) {
                GetInput(request, out, conn, context, UserId, CityIndex);
            } else if (Action.equals("SaveRecords")) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if (Action.equals("GetSchoolInfo")) {
                this.GetSchoolInfo(request, out, conn, context, UserId);
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

    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex) {
        stmt = null;
        rset = null;
        int SrlNo = 1;
        Query = "";
        StringBuilder SchoolList = new StringBuilder();
        StringBuilder CityList = new StringBuilder();

        try {
            Query = "SELECT Id, CityName FROM City WHERE Status=0 ORDER BY CityName";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            SchoolList.append("<option value=selected disabled>Select City</option>");
            while (rset.next()) {
                CityList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();

            Query = "SELECT a.Id,a.SchoolName,b.CityName ,(CASE WHEN a.Status=0 THEN 'Active' ELSE 'Inactive' END),Latitude,Longitude" +
                    " FROM School a " +
                    " STRAIGHT_JOIN City b ON a.CityIndex = b.Id " +
                    " WHERE a.Status='0'";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (rset.next()) {
                    SchoolList.append("<tr>");
                    SchoolList.append("<td>" + SrlNo + "</td>");
                    SchoolList.append("<td>" + rset.getString(3) + "</td>");
                    SchoolList.append("<td>" + rset.getString(2) + "</td>");
                    SchoolList.append("<td>" + rset.getString(4) + "</td>");
                    SchoolList.append("<td>" + rset.getString(5) + "</td>");
                    SchoolList.append("<td>" + rset.getString(6) + "</td>");
                    SchoolList.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");

                    SchoolList.append("</tr>");
                    ++SrlNo;
                }
            } catch (Exception var18) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddSchool");
                    Parser.SetField("ActionID", "GetInfo");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var17) {
                }
            }

            Parsehtm var20 = new Parsehtm(request);
            var20.SetField("CityList", CityList.toString());
            var20.SetField("SchoolList", SchoolList.toString());
            var20.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddSchool.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddSchool");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

        out.flush();
        out.close();
    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String chk = "";

        String SchoolName = request.getParameter("SchoolName").trim();
        String Status = request.getParameter("Status").trim();
        String Latitude = request.getParameter("Latitude").trim();
        String Longitude = request.getParameter("Longitude").trim();
        Query = request.getParameter("CityIndex").trim();
        int CityIndex = Integer.parseInt(Query);
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);

        if (count == 0) {
            try {
                pStmt = conn.prepareStatement(
                        "INSERT INTO School (SchoolName, Status, CreatedDate, CreatedBy, CityIndex,Latitude,Longitude) " +
                                "VALUES (?,?,NOW(),?,?,?,?)");
                pStmt.setString(1, SchoolName);
                pStmt.setString(2, Status);
                pStmt.setString(3, UserId);
                pStmt.setInt(4, CityIndex);
                pStmt.setString(5, Latitude);
                pStmt.setString(6, Longitude);
                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception e) {
                Supportive.DumException("AddSchool", "School Save -- Saving Info--001", request, e, getServletContext());
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddSchool");
                    Parser.SetField("ActionID", "GetInfo");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception e1) {
                }
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("SchoolIndex").trim();
            int SchoolIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE School SET SchoolName='" + SchoolName + "',Status=" + Status + ",CityIndex=" + CityIndex + "," +
                        "Latitude='" + Latitude + "',Longitude='" + Longitude + "' " +
                        " WHERE Id = " + SchoolIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception var34) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddSchool");
                    Parser.SetField("ActionID", "GetInfo");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "Edit School-02", var34.getMessage(), var34);
                out.flush();
                out.close();
            }
        }

        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();
    }

    void GetSchoolInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String chk = "";
        Query = request.getParameter("SchoolIndex").trim();
        int SchoolIndex = Integer.parseInt(Query);
        String SchoolName = "";
        int CityIndex = 0;
        String Status = "";
        String Latitude = "";
        String Longitude = "";

        Query = "Select Id,SchoolName,Status,CityIndex,Latitude,Longitude from School where Id=" + SchoolIndex;

        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            while (rset.next()) {
                SchoolName = rset.getString(2);
                Status = rset.getString(3);
                CityIndex = rset.getInt(4);
                Latitude = rset.getString(5);
                Longitude = rset.getString(6);
            }
            rset.close();
            stmt.close();
        } catch (Exception var35) {
            out.flush();
            out.close();
            return;
        }

        out.println(SchoolIndex + "|" + SchoolName + "|" + CityIndex + "|" + Status + "|" + Latitude + "|" + Longitude);
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


