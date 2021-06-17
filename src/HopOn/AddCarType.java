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

public class AddCarType extends HttpServlet {
    String ScreenNo = "0";
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
                GetInformation(request, out, conn, context, UserId, CityIndex);
            } else if (Action.equals("SaveRecords")) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if (Action.equals("GetCarTypeInfo")) {
                this.GetCarTypeInfo(request, out, conn, context, UserId);
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

    private void GetInformation(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex) {
        stmt = null;
        rset = null;
        int SrlNo = 1;
        Query = "";
        StringBuilder CTList = new StringBuilder();

        try {

            Query = "SELECT Id,CarType,(CASE WHEN Status=0 THEN 'Active' ELSE 'Inactive' END) " +
                    " FROM CarType WHERE Status = 0 ORDER BY Id ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                CTList.append("<tr>");
                CTList.append("<td>" + SrlNo + "</td>");
                CTList.append("<td>" + rset.getString(2) + "</td>");
                CTList.append("<td>" + rset.getString(3) + "</td>");
                CTList.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");
                CTList.append("</tr>");
                ++SrlNo;
            }
            rset.close();
            stmt.close();


            Parsehtm var20 = new Parsehtm(request);
            var20.SetField("CTList", CTList.toString());
            var20.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddCarType.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            Supportive.DumException("AddCarType", "First Method -- GetInfo--MAIN", request, var19, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddCarType");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String chk = "";

        String CarType = request.getParameter("CarType").trim();
        String Status = request.getParameter("Status").trim();
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);

        if (count == 0) {
            try {
                pStmt = conn.prepareStatement(
                        "INSERT INTO CarType (CarType, Status, CreatedDate, CreatedBy) " +
                                "VALUES (?,?,now(),?)");
                pStmt.setString(1, CarType);
                pStmt.setString(2, Status);
                pStmt.setString(3, UserId);
                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception e) {
                Supportive.DumException("AddCarType", "Second Method -- Saving Information -- 001", request, e, getServletContext());
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddCarType");
                    Parser.SetField("ActionID", "GetInfo");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception e1) {
                }

                Supportive.doLog(this.getServletContext(), "Edit City-02", e.getMessage(), e);
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("CarTypeIndex").trim();
            int CarTypeIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE CarType SET CarType='" + CarType + "',Status=" + Status + ", " +
                        " WHERE Id = " + CarTypeIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception e) {
                Supportive.DumException("AddCarType", "Second Method -- Saving Information -- 002", request, e, getServletContext());
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddCarType");
                    Parser.SetField("ActionID", "GetInfo");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var15) {
                }
                out.flush();
                out.close();
            }
        }

        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();


    }

    void GetCarTypeInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String chk = "";
        Query = request.getParameter("CarTypeIndex").trim();
        int CarTypeIndex = Integer.parseInt(Query);
        String CarType = "";
        String Status = "";

        try {
            Query = "Select Id,CarType,Status from CarType where Id=" + CarTypeIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                CarType = rset.getString(2);
                Status = rset.getString(3);
            }
            rset.close();
            stmt.close();

            out.println(CarType + "|" + CarTypeIndex + "|" + Status);
            out.flush();
            out.close();

        } catch (Exception var19) {
            Supportive.DumException("AddCarType", "Third Method -- GetCarTypeInfo--001", request, var19, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddCarType");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

    }
}
