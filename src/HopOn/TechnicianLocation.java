package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class TechnicianLocation extends HttpServlet {
    private String ScreenNo = "27";
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
//        String connect_string = supp.GetConnectString();

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

//                Class.forName("com.mysql.jdbc.Driver");
//                conn = DriverManager.getConnection(connect_string);
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
            if (Action.compareTo("TechInput") == 0) {
                TechnicianLocationInput(request, response, out, conn, context);
            } else if (Action.compareTo("ViewLocationReport") == 0) {
                ViewReport(request, response, out, conn, context);
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

    private void TechnicianLocationInput(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        String FormName = "TechnicianLocation";
        String ActionID = "ViewLocationReport";
        String Form = "Technician Location Report";

        try {

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Form", Form);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/TechnicianLocationInput.html");
        } catch (Exception var14) {
            out.println("Unable to process the request..." + var14.getMessage());
        }

    }

    private void ViewReport(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";

        Statement statement = null;
        ResultSet resultSet = null;
        String Query1 = "";

        String TechnicianName = "";

        int SNo = 0;
        String lat = "";
        String lon = "";
        String URL = "";
        String Time = "";
        StringBuilder TechnicianLocation = new StringBuilder();
/*        DatePicker = request.getParameter("datefrom");
        SMonth = DatePicker.substring(0, 2);
        SDay = DatePicker.substring(3, 5);
        SYear = DatePicker.substring(6, 10);
        EDay = DatePicker.substring(16, 18);
        EMonth = DatePicker.substring(12, 15).trim();
        EYear = DatePicker.substring(19);
        StartDate = SYear + "-" + SDay + "-" + SMonth + " 00:00:00";
        EndDate = EYear + "-" + EDay + "-" + EMonth + " 23:59:59";*/
        try {

            Query = "SELECT Id,UserId,Status,UserName FROM MobileUsers WHERE Status = 0";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Query1 = "SELECT Latitude,Longtitude,UserId, DATE_FORMAT(CreatedDate,'%d-%m-%Y %H:%i:%s') " +
                        " FROM TechnicianLocation WHERE UserId='" + rset.getString(2) + "' AND " +
                        " CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId='" + rset.getString(2) + "') AND " +
                        " DATE_FORMAT(CreatedDate,'%d-%m-%Y') = DATE_FORMAT(NOW(),'%d-%m-%Y') ";

                statement = conn.createStatement();
                resultSet = statement.executeQuery(Query1);

                while (resultSet.next()) {
                    ++SNo;
                    lat = resultSet.getString(1).trim();
                    lon = resultSet.getString(2).trim();
                    TechnicianName = rset.getString(4);
                    if (!resultSet.getString(1).equals("-") && !resultSet.getString(2).equals("-") &&
                            !resultSet.getString(1).equals("null") && !resultSet.getString(2).equals(null) &&
                            !lat.isEmpty() && !lon.isEmpty() && !lat.equals(null) && !lon.equals(null)) {
                        TechnicianLocation.append("['" + resultSet.getString(4) + "' ,");
                        TechnicianLocation.append(resultSet.getString(1) + ",");
                        TechnicianLocation.append(resultSet.getString(2) + ",");
                        //TechnicianLocation.append(SNo + "],");
                        TechnicianLocation.append(" '" + rset.getString(4) + "'],");
                    }
                }
                resultSet.close();
                statement.close();
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Location", TechnicianLocation.toString());
            Parser.SetField("URL", URL);
            Parser.SetField("lat", lat);
            Parser.SetField("lon", lon);
            Parser.SetField("TechnicianName", TechnicianName);
            Parser.SetField("time", Time);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ViewTechLocation.html");
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
