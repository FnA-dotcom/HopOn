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
import java.sql.*;

@SuppressWarnings("Duplicates")
public class AddRadius extends HttpServlet {
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;
    private String ScreenNo = "15";

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
        ServletContext context = null;
        context = this.getServletContext();
        int CityIndex = 0;
        String UserId = "";
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
            } catch (Exception excp) {
                conn = null;
                out.println("Exception excp conn: " + excp.getMessage());
            }
            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
/*            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }*/

            Action = request.getParameter("ActionID");
            if (Action.equals("GetRadiusInput")) {
                this.GetRadiusInput(request, out, conn, context, UserId, CityIndex, isAdmin);
            } else if (Action.equals("EditRadiusRecords")) {
                this.EditRadiusRecords(request, out, conn, context, UserId, CityIndex);
            } else if (Action.equals("ShowEditRadius")) {
                this.ShowEditRadius(request, out, conn, context, UserId);
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

    private void GetRadiusInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int cityIndex, int isAdmin) {
        this.stmt = null;
        this.rset = null;
        int SrlNo = 1;
        Query = "";

        StringBuilder Radius = new StringBuilder();
        StringBuffer CityList = new StringBuffer();

        try {
            this.Query = "SELECT Id,CityName FROM City  WHERE Status=0  ORDER BY Id";
            this.stmt = conn.createStatement();
            this.rset = this.stmt.executeQuery(this.Query);
            CityList.append("<option value='' selected disabled>Select City</option>");

            while (this.rset.next()) {
                CityList.append("<option value=" + this.rset.getString(1) + ">" + this.rset.getString(2) + "</option>");
            }

            this.rset.close();
            this.stmt.close();
        } catch (Exception var15) {
            out.println(var15);
        }

        try {
            if (isAdmin == 1) {
                this.Query = "SELECT a.Radius,a.Id,b.CityName " +
                        "FROM RadiusCheck a inner join City b on a.CityIndex = b.Id ";
            } else {
                this.Query = "SELECT a.Radius,a.Id,b.CityName " +
                        "FROM RadiusCheck a inner join City b on a.CityIndex = b.Id " +
                        "where a.CityIndex = " + cityIndex + " ";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Radius.append("<tr>");
                Radius.append("<td>" + SrlNo + "</td>");
                Radius.append("<td>" + rset.getString(1) + "</td>"); //Radius
                Radius.append("<td>" + rset.getString(3) + "</td>");//CityName
                Radius.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(2) + ")\"></i></td>");

                Radius.append("</tr>");
                SrlNo++;
            }
            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Radius", Radius.toString());
            Parser.SetField("CityList", CityList.toString());
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

    private void EditRadiusRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int cityIndex) {
        this.Query = "";
        this.stmt = null;
        String chk = "";

        String Radius = request.getParameter("Radius").trim();
        this.Query = request.getParameter("RadiusIndex").trim();
        int RadiusIndex = Integer.parseInt(this.Query);
        this.Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(this.Query);
        this.Query = request.getParameter("City_Code").trim();
        int City_Code = Integer.parseInt(this.Query);
        int condValue = -1;

        Parsehtm Parser;
        try {
            if (count == 0) {
                this.Query = "SELECT count(*) from RadiusCheck where CityIndex = " + City_Code + "";

                try {
                    stmt = conn.createStatement();
                    rset = stmt.executeQuery(Query);
                    if (rset.next()) {
                        condValue = rset.getInt(1);
                    }

                    rset.close();
                    stmt.close();
                    if (condValue == 1) {
                        Query = "UPDATE RadiusCheck SET Radius=" + Radius + ",UpdatedBy='" + UserId + "'," +
                                "UpdatedDate=NOW(),CityIndex=" + City_Code + " WHERE CityIndex = " + City_Code;
                        stmt = conn.createStatement();
                        stmt.executeUpdate(this.Query);
                        stmt.close();
                        out.print("1");
                    } else {
                        try {
                            this.pStmt = conn.prepareStatement(
                                    "INSERT INTO RadiusCheck (Radius, Status, CreatedDate,CityIndex) " +
                                            "VALUES (?,?,now(),?)");
                            this.pStmt.setString(1, Radius);
                            this.pStmt.setString(2, "0");
                            this.pStmt.setInt(3, City_Code);
                            this.pStmt.executeUpdate();
                            this.pStmt.close();
                        } catch (Exception var21) {
                            out.println(var21);

                            try {
                                Parser = new Parsehtm(request);
                                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                            } catch (Exception var20) {
                                ;
                            }

                            Supportive.doLog(this.getServletContext(), "SaveCount-01", var21.getMessage(), var21);
                            out.flush();
                            out.close();
                        }
                    }
                } catch (SQLException var22) {
                    var22.printStackTrace();
                }
            } else {
                Query = "SELECT Radius,Status,CreatedDate,UpdatedBy,UpdatedDate,CityIndex FROM RadiusCheck ";
                stmt = conn.createStatement();
                rset = this.stmt.executeQuery(this.Query);
                if (this.rset.next()) {
                    pStmt = conn.prepareStatement(
                            "INSERT INTO RadiusCheckHistory (Radius, Status, CreatedDate, " +
                                    "UpdatedBy, UpdatedDate,CityIndex) VALUES (?,?,?,?,?,?)");
                    pStmt.setString(1, rset.getString(1));
                    pStmt.setString(2, rset.getString(2));
                    pStmt.setString(3, rset.getString(3));
                    pStmt.setString(4, rset.getString(4));
                    pStmt.setString(5, rset.getString(5));
                    pStmt.setString(6, rset.getString(6));
                    pStmt.executeUpdate();
                    pStmt.close();
                }

                rset.close();
                stmt.close();
                Query = "UPDATE RadiusCheck SET Radius='" + Radius + "', UpdatedBy = '" + UserId + "'," +
                        "UpdatedDate = NOW(),CityIndex=" + City_Code + " WHERE CityIndex = " + City_Code;
                stmt = conn.createStatement();
                stmt.executeUpdate(this.Query);
                stmt.close();
            }
        } catch (Exception e) {
            try {
                Parser = new Parsehtm(request);
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
        this.Query = request.getParameter("RadiusIndex").trim();
        int RadiusIndex = Integer.parseInt(this.Query);
        int CityIndex = 0;

        try {
            Query = "SELECT Id,Radius,CityIndex,Status FROM RadiusCheck where Id=" + RadiusIndex;

            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    Radius = rset.getString(2).trim();
                    CityIndex = rset.getInt(3);
                    Status = rset.getString(4);
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

        } catch (Exception var18) {
            chk = "13";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
            return;
        }
        out.println(Radius + "|" + RadiusIndex + "|" + CityIndex + "|" + Status);
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
