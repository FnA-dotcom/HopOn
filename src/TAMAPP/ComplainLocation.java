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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class ComplainLocation extends HttpServlet {
    private String ScreenNo = "25";
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.HandleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.HandleRequest(request, response);
    }

    private void HandleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        String UserId = "";
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        String connect_string = supp.GetConnectString();

        try {
            ServletContext context = null;
            context = this.getServletContext();

            try {
                boolean ValidSession = Supportive.checkSession(out, request);
                if (!ValidSession) {
                    out.flush();
                    out.close();
                    return;
                }

                UserId = Supportive.GetCookie("UserId", request);
                if (UserId == "") {
                    out.println("<font size=\"3\" face=\"Calibri\">Your session has been expired, please login again.</font>");
                    out.flush();
                    out.close();
                    return;
                }

                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var11) {
                conn = null;
                out.println("Exception excp conn: " + var11.getMessage());
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
            if (Action.compareTo("GETINPUT") == 0) {
                this.ComplainLocationInput(request, response, out, conn, context);
            } else if (Action.compareTo("ViewLocationReport") == 0) {
                this.ViewReport(request, response, out, conn, context);
            } else {
                out.println("Under Development ... " + Action);
            }

            conn.close();
        } catch (Exception var12) {
            out.println("Exception in main... " + var12.getMessage());
            out.flush();
            out.close();
            return;
        }

        out.flush();
        out.close();
    }

    private void ComplainLocationInput(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        String FormName = "ComplainLocation";
        String ActionID = "ViewLocationReport";
        String Form = "Complain Location Report";

        try {

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ComplainLocationInput.html");
        } catch (Exception var14) {
            out.println("Unable to process the request..." + var14.getMessage());
        }

    }

    private void ViewReport(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        String ComplainNumber = "";
        String CustomerName = "";
        String DatePicker = "";
        String SMonth = "";
        String SDay = "";
        String SYear = "";
        String EDay = "";
        String EMonth = "";
        String EYear = "";
        String StartDate = "";
        String EndDate = "";
        int SNo = 0;
        String lat = "";
        String lon = "";
        String URL = "";
        String Time = "";
        StringBuilder Location = new StringBuilder();
        Query = request.getParameter("Flag").trim();
        int Flag = Integer.parseInt(Query);
        if (Flag == 0) {
            DatePicker = request.getParameter("datefrom");
            SMonth = DatePicker.substring(0, 2);
            SDay = DatePicker.substring(3, 5);
            SYear = DatePicker.substring(6, 10);
            EDay = DatePicker.substring(16, 18);
            EMonth = DatePicker.substring(12, 15).trim();
            EYear = DatePicker.substring(19);
            StartDate = SYear + "-" + SDay + "-" + SMonth + " 00:00:00";
            EndDate = EYear + "-" + EDay + "-" + EMonth + " 23:59:59";
        }

        if (Flag == 1) {
            ComplainNumber = request.getParameter("ComplainNumber").trim();
            ComplainNumber = ComplainNumber.replace("^", "#");
        }
        try {
            if (Flag == 0) {
                Query = " SELECT ifnull(a.Latitude,'-'),ifnull(a.Longtitude,'-'), DATE_FORMAT(a.CreatedDate,'%d-%m-%Y %H:%i:%s'), a.CustName " +
                        "FROM CustomerData a WHERE a.CreatedDate >= date_format('" + StartDate + "','%Y-%m-%d %H:%i:%s') AND " +
                        " a.CreatedDate <= date_format('" + EndDate + "','%Y-%m-%d %H:%i:%s') ";
            } else {
                Query = " SELECT ifnull(a.Latitude,'-'),ifnull(a.Longtitude,'-'), DATE_FORMAT(a.CreatedDate,'%d-%m-%Y %H:%i:%s'), a.CustName " +
                        "FROM CustomerData a WHERE a.ComplainNumber = '" + ComplainNumber + "' ";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            while (rset.next()) {
                ++SNo;
                lat = rset.getString(1).trim();
                lon = rset.getString(2).trim();
                Time = rset.getString(3).trim();
                CustomerName = rset.getString(4);
                if (!rset.getString(1).equals("-") && !rset.getString(2).equals("-") &&
                        !rset.getString(1).equals("null") && !rset.getString(2).equals(null) &&
                        !lat.isEmpty() && !lon.isEmpty() && !lat.equals(null) && !lon.equals(null)) {
                    Location.append("['" + rset.getString(4) + "',");
                    Location.append(rset.getString(1) + ",");
                    Location.append(rset.getString(2) + ",");
                    Location.append(SNo + "],");
                }
            }

            rset.close();
            stmt.close();
            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Location", Location.toString());
            Parser.SetField("URL", URL);
            Parser.SetField("lat", lat);
            Parser.SetField("lon", lon);
            Parser.SetField("CustomerName", CustomerName);
            Parser.SetField("time", Time);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ViewComplainLocation.html");
        } catch (Exception var35) {
            out.println("Unable to process the request..." + var35.getMessage());
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
