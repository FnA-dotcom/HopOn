package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class InspectionReport extends HttpServlet {
    private String ScreenNo = "23";
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HandleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HandleRequest(request, response);
    }

    public void HandleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        String UserId = "";

        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        int CityIndex = 0;
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
            ServletContext context = null;
            context = getServletContext();

            try {
                boolean ValidSession = Supportive.checkSession(out, request);
                if (!ValidSession) {
                    out.flush();
                    out.close();

                    return;
                }
                // UserId = Supportive.GetCookie("UserId", request);
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
            if (!authent.AuthorizeScreen(UserId, ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }*/
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }
            Action = request.getParameter("ActionID");

            if (Action.equals("GETINPUT")) {
                this.InputReport(request, response, out, conn, context, CityIndex, isAdmin);
            } else if (Action.equals("ShowReport")) {
                this.ShowRecords(request, out, conn, response, context, CityIndex, isAdmin);
            } else if (Action.trim().equals("ViewImage")) {
                this.ViewImage(request, response, out);
            } else {
                out.println("Under Development ... " + Action);
            }
            conn.close();
        } catch (Exception e) {
            out.println("Exception in main... " + e.getMessage());
            out.flush();
            out.close();
            return;
        }
        out.flush();
        out.close();
    }

    private void InputReport(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext, int cityIndex, int isAdmin) {
        this.stmt = null;
        this.rset = null;
        this.Query = "";
        StringBuilder ComplainNumber = new StringBuilder();

        try {

            //old logic
//            Query = " SELECT Id,ComplainNumber FROM CustomerData WHERE " +
//                    " Id IN (SELECT ComplaintId FROM Assignment)" +
//                    " ORDER BY Id";
            //new logic
            Query = "SELECT Id,CONCAT(ComplainNumber , ' - ' , IFNULL(Insurance,'-') , ' - ' , IFNULL(ChesisNo,'-'))  FROM CustomerData WHERE \n" +
                    "Id IN (SELECT ComplaintId FROM Assignment WHERE ComplaintStatus=6) AND CityIndex = " + cityIndex + "  \n" +
                    "ORDER BY AssignDate DESC";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            ComplainNumber.append("<option value=0 Selected Disabled> Select Complain Number </option>");
            while (rset.next()) {
                ComplainNumber.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            stmt.close();
            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("ComplainNumber", ComplainNumber.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/InspectionForm.html");
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "Error Record 001", e.getMessage(), e);
        }
    }

    private void ShowRecords(HttpServletRequest request, PrintWriter out, Connection conn, HttpServletResponse response, ServletContext servletContext, int cityIndex, int isAdmin) {
        this.stmt = null;
        this.rset = null;
        this.Query = "";
        String TechName = "";

        Query = request.getParameter("Complain").trim();
        int Complain = Integer.parseInt(Query);

/*        String DatePicker = request.getParameter("daterange");
        String SMonth = DatePicker.substring(0, 2);
        String SDay = DatePicker.substring(3, 5);
        String SYear = DatePicker.substring(6, 10);

        String EDay = DatePicker.substring(16, 18);
        String EMonth = DatePicker.substring(12, 15).trim();
        String EYear = DatePicker.substring(19);

        String StartDate = SYear + "-" + SDay + "-" + SMonth + " 00:00:00";
        String EndDate = EYear + "-" + EDay + "-" + EMonth + " 23:59:59";*/
        String IFComplainNumber = "";

        StringBuilder ComplainDetail1 = new StringBuilder();
        StringBuilder ComplainDetail2 = new StringBuilder();
        StringBuilder Table1 = new StringBuilder();
        StringBuilder Table2 = new StringBuilder();
        StringBuilder Table3 = new StringBuilder();
        StringBuilder Table4 = new StringBuilder();
        StringBuilder Table5 = new StringBuilder();
        StringBuilder ImageList = new StringBuilder();
        try {
            if (isAdmin == 1) {
                this.Query = "SELECT ComplainNumber FROM CustomerData WHERE Id=" + Complain;
            } else {
                this.Query = "SELECT ComplainNumber FROM CustomerData WHERE Id=" + Complain + " AND CityIndex = " + cityIndex + " ";
            }

            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next())
                    IFComplainNumber = rset.getString(1).trim();
                rset.close();
                stmt.close();
            } catch (Exception Ex) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "Inspection Form - Show Report 00", Ex.getMessage(), Ex);
            }
/*            Query = "SELECT Horn, InteriorLights, SideMirrors, PowerWindows" +
                    " FROM FaultInspection WHERE " +
                    " CreatedDate BETWEEN '" + StartDate + "' AND '" + EndDate + "' AND ComplainNumber='" + ComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + ComplainNumber + "' ) " +
                    " Order by CreatedDate DESC limit 1";*/
            //Date Filter Removed
/*            Query = "SELECT Horn, InteriorLights, SideMirrors, PowerWindows" +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate DESC LIMIT 1";*/
/*            Query = "SELECT Horn, InteriorLights, SideMirrors, PowerWindows" +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate LIMIT 1";*/
            if (isAdmin == 1) {
                this.Query = "SELECT Horn, InteriorLights, SideMirrors, PowerWindows FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "' " + " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            } else {
                this.Query = "SELECT Horn, InteriorLights, SideMirrors, PowerWindows FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "' " + " AND CityIndex = " + cityIndex + " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Table1.append("<tr>");
                Table1.append("<td align=left>" + rset.getString(1) + "</td>\n");// Horn
                Table1.append("<td align=left>" + rset.getString(2) + "</td>\n");// InteriorLights
                Table1.append("<td align=left>" + rset.getString(3) + "</td>\n");// SideMirrors
                Table1.append("<td align=left>" + rset.getString(4) + "</td>\n");// PowerWindows
                Table1.append("</tr>");
            }
            rset.close();
            stmt.close();

/*            Query = "SELECT CentralLockingSystem, AC,Radio, TempGauge" +
                    " FROM FaultInspection WHERE " +
                    " CreatedDate BETWEEN '" + StartDate + "' AND '" + EndDate + "' AND ComplainNumber='" + ComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + ComplainNumber + "' ) " +
                    " Order by CreatedDate DESC limit 1";*/
/*            Query = "SELECT CentralLockingSystem, AC,Radio, TempGauge" +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate DESC LIMIT 1";*/
/*            Query = "SELECT CentralLockingSystem, AC,Radio, TempGauge" +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate LIMIT 1";*/
            if (isAdmin == 1) {
                this.Query = "SELECT CentralLockingSystem, AC,Radio, TempGauge FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "' " + " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            } else {
                this.Query = "SELECT CentralLockingSystem, AC,Radio, TempGauge FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "' " + " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE CityIndex = " + cityIndex + " AND ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Table2.append("<tr>");
                Table2.append("<td align=left>" + rset.getString(1) + "</td>\n");// CentralLockingSystem
                Table2.append("<td align=left>" + rset.getString(2) + "</td>\n");// AC
                Table2.append("<td align=left>" + rset.getString(3) + "</td>\n");// Radio
                Table2.append("<td align=left>" + rset.getString(4) + "</td>\n");// TempGauge
                Table2.append("</tr>");
            }
            rset.close();
            stmt.close();

//            Query = "SELECT FuelGauge, ABSLight, EngineCheckLight, HandBrake,ToolKit " +
//                    " FROM FaultInspection WHERE " +
//                    " CreatedDate BETWEEN '" + StartDate + "' AND '" + EndDate + "' AND ComplainNumber='" + ComplainNumber + "' " +
//                    "AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection  WHERE ComplainNumber='" + ComplainNumber + "' ) " +
//                    " Order by CreatedDate DESC limit 1";

/*            Query = "SELECT FuelGauge, ABSLight, EngineCheckLight, HandBrake,ToolKit " +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    "AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection  WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate DESC LIMIT 1";*/
/*            Query = "SELECT FuelGauge, ABSLight, EngineCheckLight, HandBrake,ToolKit " +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    "AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection  WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate LIMIT 1";*/
            if (isAdmin == 1) {
                this.Query = "SELECT FuelGauge, ABSLight, EngineCheckLight, HandBrake,ToolKit  FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "' " + "AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection  WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            } else {
                this.Query = "SELECT FuelGauge, ABSLight, EngineCheckLight, HandBrake,ToolKit  FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "'  AND CityIndex = " + cityIndex + " " + "AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection  WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Table3.append("<tr>");
                Table3.append("<td align=left>" + rset.getString(1) + "</td>\n");// FuelGauge
                Table3.append("<td align=left>" + rset.getString(2) + "</td>\n");// ABSLight
                Table3.append("<td align=left>" + rset.getString(3) + "</td>\n");// EngineCheckLight
                Table3.append("<td align=left>" + rset.getString(4) + "</td>\n");// HandBrake
                Table3.append("<td align=left>" + rset.getString(5) + "</td>\n");// ToolKit
                Table3.append("</tr>");
            }
            rset.close();
            stmt.close();

/*            Query = "SELECT  CleanInterior,DashBoard, GearShift, CustBelongings,Carjack " +
                    " FROM FaultInspection WHERE " +
                    " CreatedDate BETWEEN '" + StartDate + "' AND '" + EndDate + "' AND ComplainNumber='" + ComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(x.CreatedDate) FROM FaultInspection x WHERE ComplainNumber='" + ComplainNumber + "' ) " +
                    " Order by CreatedDate DESC limit 1";*/

/*            Query = "SELECT  CleanInterior,DashBoard, GearShift, CustBelongings,Carjack " +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(x.CreatedDate) FROM FaultInspection x WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate DESC LIMIT 1";*/
/*            Query = "SELECT  CleanInterior,DashBoard, GearShift, CustBelongings,Carjack " +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(x.CreatedDate) FROM FaultInspection x WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate LIMIT 1";*/
            if (isAdmin == 1) {
                this.Query = "SELECT  CleanInterior,DashBoard, GearShift, CustBelongings,Carjack  FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "' " + " AND CreatedDate=(SELECT max(x.CreatedDate) FROM FaultInspection x WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            } else {
                this.Query = "SELECT  CleanInterior,DashBoard, GearShift, CustBelongings,Carjack  FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "'  AND CityIndex = " + cityIndex + " " + " AND CreatedDate=(SELECT max(x.CreatedDate) FROM FaultInspection x WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Table4.append("<tr>");
                Table4.append("<td align=left>" + rset.getString(1) + "</td>\n");// CleanInterior
                Table4.append("<td align=left>" + rset.getString(2) + "</td>\n");// DashBoard
                Table4.append("<td align=left>" + rset.getString(3) + "</td>\n");// GearShift
                Table4.append("<td align=left>" + rset.getString(4) + "</td>\n");// CustBelongings
                Table4.append("<td align=left>" + rset.getString(5) + "</td>\n");// Carjack
                Table4.append("</tr>");
            }
            rset.close();
            stmt.close();

/*            Query = "SELECT ExteriorLights, BodyDamage, IntExtScratches, Windscreen " +
                    " FROM FaultInspection WHERE " +
                    " CreatedDate BETWEEN '" + StartDate + "' AND '" + EndDate + "' AND ComplainNumber='" + ComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + ComplainNumber + "' ) " +
                    " Order by CreatedDate DESC limit 1";*/
/*            Query = "SELECT ExteriorLights, BodyDamage, IntExtScratches, Windscreen " +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate DESC LIMIT 1";*/
/*            Query = "SELECT ExteriorLights, BodyDamage, IntExtScratches, Windscreen " +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate LIMIT 1";*/
            if (isAdmin == 1) {
                this.Query = "SELECT ExteriorLights, BodyDamage, IntExtScratches, Windscreen  FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "' " + " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            } else {
                this.Query = "SELECT ExteriorLights, BodyDamage, IntExtScratches, Windscreen  FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "'  AND CityIndex = " + cityIndex + " " + " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                Table5.append("<tr>");
                Table5.append("<td align=left>" + rset.getString(1) + "</td>\n");// ExteriorLights
                Table5.append("<td align=left>" + rset.getString(2) + "</td>\n");// BodyDamage
                Table5.append("<td align=left>" + rset.getString(3) + "</td>\n");// IntExtScratches
                Table5.append("<td align=left>" + rset.getString(4) + "</td>\n");// Windscreen
                Table5.append("</tr>");

            }
            rset.close();
            stmt.close();

/*            Query = "SELECT Pic1,Pic2,Pic3,Pic4 " +
                    " FROM FaultInspection WHERE " +
                    " CreatedDate BETWEEN '" + StartDate + "' AND '" + EndDate + "' AND ComplainNumber='" + ComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + ComplainNumber + "' ) " +
                    " Order by CreatedDate DESC limit 1";*/
/*            Query = "SELECT Pic1,Pic2,Pic3,Pic4 " +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate DESC LIMIT 1";*/
/*            Query = "SELECT Pic1,Pic2,Pic3,Pic4 " +
                    " FROM FaultInspection WHERE " +
                    " ComplainNumber='" + IFComplainNumber + "' " +
                    " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " +
                    " ORDER BY CreatedDate LIMIT 1";*/
            if (isAdmin == 1) {
                this.Query = "SELECT Pic1,Pic2,Pic3,Pic4  FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "' " + " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            } else {
                this.Query = "SELECT Pic1,Pic2,Pic3,Pic4  FROM FaultInspection WHERE  ComplainNumber='" + IFComplainNumber + "'  AND CityIndex = " + cityIndex + " " + " AND CreatedDate=(SELECT max(CreatedDate) FROM FaultInspection WHERE ComplainNumber='" + IFComplainNumber + "' ) " + " ORDER BY CreatedDate LIMIT 1";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                ImageList.append("<tr>");
                if (!rset.getString(1).equals("-"))
                    ImageList.append("<td align=left> <a href=\"JavaScript:newPopup('/HopOn/HopOn.InspectionReport?ActionID=ViewImage&FileName=" + rset.getString(1) + "');\"><i class=\"fa fa-share\">[View]</i></a></td>\n");// Shop Pic
                else
                    ImageList.append("<td align=left>No Image Taken</td>\n");
                if (!rset.getString(2).equals("-"))
                    ImageList.append("<td align=left> <a href=\"JavaScript:newPopup('/HopOn/HopOn.InspectionReport?ActionID=ViewImage&FileName=" + rset.getString(2) + "');\"><i class=\"fa fa-share\">[View]</i></a></td>\n");// Shop Pic
                else
                    ImageList.append("<td align=left>No Image Taken</td>\n");
                if (!rset.getString(3).equals("-"))
                    ImageList.append("<td align=left> <a href=\"JavaScript:newPopup('/HopOn/HopOn.InspectionReport?ActionID=ViewImage&FileName=" + rset.getString(3) + "');\"><i class=\"fa fa-share\">[View]</i></a></td>\n");// Shop Pic
                else
                    ImageList.append("<td align=left>No Image Taken</td>\n");
                if (!rset.getString(4).equals("-"))
                    ImageList.append("<td align=left> <a href=\"JavaScript:newPopup('/HopOn/HopOn.InspectionReport?ActionID=ViewImage&FileName=" + rset.getString(4) + "');\"><i class=\"fa fa-share\">[View]</i></a></td>\n");// Shop Pic
                else
                    ImageList.append("<td align=left>No Image Taken</td>\n");
                ImageList.append("</tr>");
            }
            rset.close();
            stmt.close();

/*            Query = "SELECT  a.RegistrationNum,a.CustName,a.CellNo,ifnull(a.PhoneNo,'-'),a.Make,a.Model,a.Color,a.ChesisNo," +
                    "b.JobType,a.Address,IFNULL(a.DeviceNo,'-'),IFNULL(a.Insurance,'-'),a.ComplainNumber,d.UserName " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id " +
                    " STRAIGHT_JOIN Assignment c ON a.Id=c.ComplaintId " +
                    " AND c.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = c.ComplaintId) " +
                    " STRAIGHT_JOIN  MobileUsers d ON c.UserId=d.Id " +
                    " WHERE a.ComplainNumber='" + IFComplainNumber + "' ";*/
            if (isAdmin == 1) {
                this.Query = "SELECT  a.RegistrationNum,a.CustName,a.CellNo,ifnull(a.PhoneNo,'-'),a.Make,a.Model,a.Color,a.ChesisNo,b.JobType,a.Address,IFNULL(a.DeviceNo,'-'),IFNULL(a.Insurance,'-'),a.ComplainNumber,d.UserName  FROM CustomerData a  STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id  STRAIGHT_JOIN Assignment c ON a.Id=c.ComplaintId  AND c.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = c.ComplaintId)  STRAIGHT_JOIN  MobileUsers d ON c.UserId=d.Id  WHERE a.ComplainNumber='" + IFComplainNumber + "' ";
            } else {
                this.Query = "SELECT  a.RegistrationNum,a.CustName,a.CellNo,ifnull(a.PhoneNo,'-'),a.Make,a.Model,a.Color,a.ChesisNo,b.JobType,a.Address,IFNULL(a.DeviceNo,'-'),IFNULL(a.Insurance,'-'),a.ComplainNumber,d.UserName  FROM CustomerData a  STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id  STRAIGHT_JOIN Assignment c ON a.Id=c.ComplaintId  AND c.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = c.ComplaintId)  STRAIGHT_JOIN  MobileUsers d ON c.UserId=d.Id  WHERE a.ComplainNumber='" + IFComplainNumber + "'  AND CityIndex = " + cityIndex + " ";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                ComplainDetail1.append("<tr>");
                ComplainDetail1.append("<td align=left>" + rset.getString(1).trim() + "</td>\n");// RegistrationNum
                ComplainDetail1.append("<td align=left>" + rset.getString(2) + "</td>\n");// CustName
                ComplainDetail1.append("<td align=left>" + rset.getString(3) + "</td>\n");// CellNo
                ComplainDetail1.append("<td align=left>" + rset.getString(4) + "</td>\n");// PhoneNo
                ComplainDetail1.append("<td align=left>" + rset.getString(5) + "</td>\n");// Make
                ComplainDetail1.append("<td align=left>" + rset.getString(6) + "</td>\n");// Model
                ComplainDetail1.append("<td align=left>" + rset.getString(7) + "</td>\n");// Color
                ComplainDetail1.append("<td align=left>" + rset.getString(8) + "</td>\n");// ChesisNo
                ComplainDetail1.append("</tr>");
                TechName = rset.getString(14).trim();
            }
            rset.close();
            stmt.close();

/*            Query = "SELECT  b.JobType,a.Address,IFNULL(a.DeviceNo,'-'),IFNULL(a.Insurance,'-') " +
                    " FROM CustomerData a " +
                    " STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id " +
                    " STRAIGHT_JOIN Assignment c ON a.Id=c.ComplaintId " +
                    " AND c.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = c.ComplaintId) " +
                    " STRAIGHT_JOIN  MobileUsers d ON c.UserId=d.Id " +
                    " WHERE a.ComplainNumber='" + IFComplainNumber + "' ";*/
            if (isAdmin == 1) {
                this.Query = "SELECT  b.JobType,a.Address,IFNULL(a.DeviceNo,'-'),IFNULL(a.Insurance,'-')  FROM CustomerData a  STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id  STRAIGHT_JOIN Assignment c ON a.Id=c.ComplaintId  AND c.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = c.ComplaintId)  STRAIGHT_JOIN  MobileUsers d ON c.UserId=d.Id  WHERE a.ComplainNumber='" + IFComplainNumber + "' ";
            } else {
                this.Query = "SELECT  b.JobType,a.Address,IFNULL(a.DeviceNo,'-'),IFNULL(a.Insurance,'-')  FROM CustomerData a  STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id  STRAIGHT_JOIN Assignment c ON a.Id=c.ComplaintId  AND c.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = c.ComplaintId)  STRAIGHT_JOIN  MobileUsers d ON c.UserId=d.Id  WHERE a.ComplainNumber='" + IFComplainNumber + "'  AND CityIndex = " + cityIndex + " ";
            }
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                ComplainDetail2.append("<tr>");
                ComplainDetail2.append("<td align=left>" + rset.getString(1).trim() + "</td>\n");// JobType
                ComplainDetail2.append("<td align=left>" + rset.getString(2) + "</td>\n");// Address
                ComplainDetail2.append("<td align=left>" + rset.getString(3) + "</td>\n");// DeviceNo
                ComplainDetail2.append("<td align=left>" + rset.getString(4) + "</td>\n");// Insurance
                ComplainDetail2.append("</tr>");
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Table1", Table1.toString());
            Parser.SetField("Table2", Table2.toString());
            Parser.SetField("Table3", Table3.toString());
            Parser.SetField("Table4", Table4.toString());
            Parser.SetField("Table5", Table5.toString());
            Parser.SetField("ImageList", ImageList.toString());
            Parser.SetField("ComplainDetail1", ComplainDetail1.toString());
            Parser.SetField("ComplainDetail2", ComplainDetail2.toString());
            Parser.SetField("ComplainNumber", IFComplainNumber);
            Parser.SetField("TechName", TechName);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "/Reports/ShowRecords.html");
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "Inspection Form - Show Report 01", e.getMessage(), e);
        }

    }

    private void ViewImage(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String FileName = request.getParameter("FileName");

        try {
            //String path = "d:\\HTMLS\\EngroTSM\\pics\\" + FileName;
            String path = "/opt/Htmls/HopOn/pics/" + FileName;
            out.println(path);
            FileInputStream fin = new FileInputStream(new File(path));
            byte content[] = new byte[fin.available()];
            fin.read(content);
            fin.close();

            OutputStream os = response.getOutputStream();
            response.setContentType("image/jpeg");
            os.write(content);
            os.flush();
            os.close();

        } catch (Exception e) {
            out.println("Image not found..." + e.getMessage());
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
