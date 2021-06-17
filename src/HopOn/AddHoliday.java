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

public class AddHoliday extends HttpServlet {
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

            UtilityHelper helper = new UtilityHelper();
            Action = request.getParameter("ActionID");
            switch (Action) {
                case "GetInfo":
                    GetInformation(request, out, conn, context, UserId, CityIndex, helper);
                    break;
                case "SaveHoliday":
                    SaveHoliday(request, out, conn, context, UserId, CityIndex, helper);
                    break;
                default:
                    out.println("Under Development ... " + Action);
                    break;
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

    private void GetInformation(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuilder DriverList;
        try {
            DriverList = helper.DriverList(request, conn, servletContext);

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("DriverList", DriverList.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddHoliday.html");
        } catch (Exception var19) {
            Supportive.DumException("AddHoliday", "First Method -- GetInfo--MAIN", request, var19, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddHoliday");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void SaveHoliday(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, UtilityHelper helper) {
        Query = "";
        stmt = null;
        rset = null;

        String NatDate = "";
        String NatHolidayEnd = "";
        String PerDate = "";
        int PerDriver = 0;
        String PerHolidayEnd = "";

        String SMonth = "";
        String SDay = "";
        String SYear = "";

        String PMonth = "";
        String PDay = "";
        String PYear = "";


        //Fix Variables
        String StartDate = "";
        String EndDate = "-";
        int CheckBoxVal = 0;
        String FinalComments = "";


        //For CounterNat Value
        //If Its value is 1 then it will be as National Holiday
        //If Its value is 3 then it will be as Personal Holiday
        int HolidayType = Integer.parseInt(request.getParameter("CounterNat").trim());

        //For CheckBox Value
        //1 - Means in It is not selected
        //0 - Means in It is selected
        if (HolidayType == 1) {
            NatDate = request.getParameter("SDate");
            SMonth = NatDate.substring(0, 2);
            SDay = NatDate.substring(3, 5);
            SYear = NatDate.substring(6, 10);

            StartDate = SYear + "-" + SMonth + "-" + SDay;

            if (request.getParameter("my-checkbox") == null)
                CheckBoxVal = 1;
            else
                CheckBoxVal = 0;

            if (CheckBoxVal == 1) {
                NatHolidayEnd = request.getParameter("HEnd");
                SMonth = NatHolidayEnd.substring(0, 2);
                SDay = NatHolidayEnd.substring(3, 5);
                SYear = NatHolidayEnd.substring(6, 10);
                EndDate = SYear + "-" + SMonth + "-" + SDay;
            }
            FinalComments = request.getParameter("txtComments");

        }
        if (HolidayType == 3) {
            PerDate = request.getParameter("SDatePer");
            PMonth = PerDate.substring(0, 2);
            PDay = PerDate.substring(3, 5);
            PYear = PerDate.substring(6, 10);

            StartDate = PYear + "-" + PMonth + "-" + PDay;

            if (request.getParameter("my-checkboxPer") == null)
                CheckBoxVal = 1;
            else
                CheckBoxVal = 0;

            PerDriver = Integer.parseInt(request.getParameter("DriverList"));
            if (CheckBoxVal == 1) {
                PerHolidayEnd = request.getParameter("HEndPer");
                PMonth = PerHolidayEnd.substring(0, 2);
                PDay = PerHolidayEnd.substring(3, 5);
                PYear = PerHolidayEnd.substring(6, 10);
                EndDate = PYear + "-" + PMonth + "-" + PDay;
            }
            FinalComments = request.getParameter("txtCommentsPer");
        }

        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO Holidays (HolidayType, StartDate, CheckBoxVal, EndHoliday, Comments, Status, CreatedDate, CreatedBy, DriverIndex) " +
                            "VALUES (?,?,?,?,?,0,NOW(),?,?)");
            pStmt.setInt(1, HolidayType);
            pStmt.setString(2, StartDate);
            pStmt.setInt(3, CheckBoxVal);
            pStmt.setString(4, (EndDate.equals("-") ? "0000:00:00" : EndDate));
            pStmt.setString(5, FinalComments);
            pStmt.setString(6, UserId);
            pStmt.setInt(7, PerDriver);
            pStmt.executeUpdate();
            pStmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", "AddHoliday");
            Parser.SetField("ActionID", "GetInfo");
            Parser.SetField("Message", "Holiday Information has been Entered Successfully!");
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddHoliday");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception e1) {
            }

            Supportive.doLog(this.getServletContext(), "Add Holiday - 01", e.getMessage(), e);
            out.flush();
            out.close();
        }
    }
}
