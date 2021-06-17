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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class TechnicianStatusWiseReport extends HttpServlet {
    private String ScreenNo = "26";
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";
    private CallableStatement cStmt = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        String UserId = "";
        int CityIndex = 0;
        int isAdmin = 0;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();

        try {
            ServletContext context = null;
            context = this.getServletContext();

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
                boolean ValidSession = Supportive.checkSession(out, request);
                if (!ValidSession) {
                    out.flush();
                    out.close();
                    return;
                }

                UserId = session.getAttribute("UserId").toString();
                CityIndex = Integer.parseInt(session.getAttribute("CityIndex").toString());
                isAdmin = Integer.parseInt(session.getAttribute("isAdmin").toString());
                if (UserId == "") {
                    out.println("<font size=\"3\" face=\"Calibri\">Your session has been expired, please login again.</font>");
                    out.flush();
                    out.close();
                    return;
                }

                //Class.forName("com.mysql.jdbc.Driver");
//                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var11) {
                conn = null;
                out.println("Exception excp conn: " + var11.getMessage());
            }

            try {
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
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }

            if (request.getParameter("Action") == null) {
                Action = "Home";
                return;
            }

            Action = request.getParameter("Action");
            if (Action.compareTo("TakeInput") == 0) {
                TakeInput(request, response, out, conn, context);
            } else if (Action.compareTo("ShowReport") == 0) {
                ShowRecords(request, out, conn, context, isAdmin, CityIndex);
            } else {
                out.println("Under Development ... " + Action);
            }

            conn.close();

            out.close();
            out.flush();
        } catch (Exception Ex) {
            out.println("Exception in main... " + Ex.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private void TakeInput(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {

        try {

            Parsehtm Parser = new Parsehtm(request);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/TechStatusInput.html");
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "Error Record 001", e.getMessage(), e);
        }
    }

    private void ShowRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, int isAdmin, int CityIndex) {
        stmt = null;
        rset = null;
        Query = "";
        String Query1 = "";
        Statement stmt1 = null;
        ResultSet rset1 = null;

        int ParamCheck = -1;
        ParamCheck = (isAdmin == 1 ? ParamCheck : CityIndex);
        int ServerIndex = 0;
        //For Switching Image Directory Just Change below variable value
        //Live Server
        ServerIndex = 1;
        //Test Server
        //ServerIndex = 2;

        String DatePicker = request.getParameter("daterange");
        String SMonth = DatePicker.substring(0, 2);
        String SDay = DatePicker.substring(3, 5);
        String SYear = DatePicker.substring(6, 10);

        String EDay = DatePicker.substring(16, 18);
        String EMonth = DatePicker.substring(12, 15).trim();
        String EYear = DatePicker.substring(19);

        String StartDate = SYear + "-" + SDay + "-" + SMonth + " 00:00:00";
        String EndDate = EYear + "-" + EDay + "-" + EMonth + " 23:59:59";

        String TechName = "";
        int TAssigned = 0;
        int TPicked = 0;
        int TCompleted = 0;
        int TPostpond = 0;
        int TCanceled = 0;
        int LoginCount = 0;

        StringBuilder ShowRecords = new StringBuilder();

        try {

            Query = "{CALL TotalTechStatus_DateWise(?,?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, StartDate);
            cStmt.setString(2, EndDate);
            cStmt.setInt(3, ParamCheck);
            rset = cStmt.executeQuery();
            while (rset.next()) {
                TechName = rset.getString(2).trim();
                TAssigned = rset.getInt(3);
                TPicked = rset.getInt(4);
                TCompleted = rset.getInt(5);
                TPostpond = rset.getInt(6);
                TCanceled = rset.getInt(7);

                Query1 = " SELECT count(*)  FROM LoginTrail " +
                        " WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + rset.getString(8) + "')) AND " +
                        " UserType = 'M' ";
                stmt1 = conn.createStatement();
                rset1 = stmt1.executeQuery(Query1);
                if (rset1.next())
                    LoginCount = rset1.getInt(1);
                rset1.close();
                stmt1.close();

                ShowRecords.append("<tr>");
                ShowRecords.append("<td class=\"text-left medium\" >" + TechName + "</td>");
                ShowRecords.append("<td class=\"text-center medium\" ><span class=\"label label-info\">" + TAssigned + "</span></td>");
                ShowRecords.append("<td class=\"text-center medium\" ><span class=\"label label-success\">" + TPicked + "</span></td>");
                ShowRecords.append("<td class=\"text-center medium\" ><span class=\"label label-primary\">" + TCompleted + "</span></td>");
                ShowRecords.append("<td class=\"text-center medium\" ><span class=\"label label-warning\">" + TPostpond + "</span></td>");
                ShowRecords.append("<td class=\"text-center medium\" ><span class=\"label label-danger\">" + TCanceled + "</span></td>");
                if (LoginCount == 0)
                    ShowRecords.append("<td class=\"text-center \"><img src=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/images/OfflineImage.png width=20 height=10 ><br></td>");// Status
                else
                    ShowRecords.append("<td class=\"text-center \" ><img src=/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/images/OnlineImage.png width=20 height=10 ><br></td>");// Status
                ShowRecords.append("</tr>");
            }
            rset.close();
            cStmt.close();


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("ShowRecords", ShowRecords.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "/Reports/ShowReport.html");
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "Technician Wise Report - Show Report 01", e.getMessage(), e);
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
