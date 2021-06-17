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

public class RouteAdd extends HttpServlet {

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
                case "SaveRecords":
                    this.SaveRecords(request, out, conn, context, UserId);
                    break;
                case "EditRouteInfo":
                    EditRouteInfo(request, out, conn, context, UserId);
                    break;
                case "DeleteRoute":
                    DeleteRoute(request, out, conn, context, UserId, helper);
                    break;
                case "ShowRunRouteWise":
                    ShowRunRouteWise(request, out, conn, context, UserId, CityIndex, helper);
                    break;
                case "ShowStudentRouteWise":
                    ShowStudentRouteWise(request, out, conn, context, UserId, CityIndex, helper);
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
        int SrlNo = 1;
        Query = "";
        Statement stmt1 = null;
        ResultSet rset1 = null;
        String Query1 = "";
        StringBuilder RouteList = new StringBuilder();
        StringBuilder DriverList;
        int statusMarked;
        int RunCount;
        int StudentCount = 0;
        String CityName;
        String RouteID = "";
        String _tkt = "";
        String CurrDate = "";
        String add8Days = "";

        try {
            double TotalRunDistance = 0;
            DriverList = helper.DriverList(request, conn, servletContext);
            CityName = helper.CityName(request, CityIndex, conn, servletContext);
            CurrDate = helper.getFormattedCurrDate(request, conn);
            _tkt = helper.getAutomateRouteID(request, conn);
            add8Days = helper.getCalculatedDate(request, conn);


            RouteID = "HO-RT#" + _tkt + "-" + CityName + "";

            Query = "SELECT a.Id,a.RouteID,0,0,0,b.FullName,(CASE WHEN a.Status=0 THEN 'Active' ELSE 'Inactive' END), a.Status," +
                    "DATE_FORMAT(a.CreatedDate,'%d-%b-%Y'), DATE_FORMAT(a.FromDate,'%d-%b-%Y'),DATE_FORMAT(a.EndDate,'%d-%b-%Y')" +
                    " FROM Routes a " +
                    " STRAIGHT_JOIN Drivers b ON a.DriverIndex = b.Id AND b.Status = 0 " +
                    " ORDER BY a.Id";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                statusMarked = rset.getInt(8);
                RunCount = 0;

                //out.println("Route Index " + rset.getInt(1) + "<br>");
/*                int[] RunCountInfo = helper.RunCountInfo(request, rset.getInt(1), conn, servletContext);
                RunCount = RunCountInfo[0];
                TotalRunDistance = RunCountInfo[1];

                out.println("Run Count " + RunCount + "<br>");
                out.println("Total Run Distance " + TotalRunDistance + "<br>");*/


                RunCount = helper.RunCount(request, rset.getInt(1), conn, servletContext);
                TotalRunDistance = helper.getRouteWiseDistance(request, rset.getInt(1), conn, servletContext);

/*                Query1 = "SELECT COUNT(*),SUM(Distance) FROM Runs WHERE RouteIndex = " + rset.getInt(1) + " AND Status = 0";
                stmt1 = conn.createStatement();
                rset1 = stmt1.executeQuery(Query1);
                if (rset1.next()) {
                    RunCount = rset1.getInt(1);
                    TotalRunDistance = rset1.getInt(2);
                }
                rset1.close();
                stmt1.close();*/

                Query1 = "SELECT COUNT(*) FROM StudentWiseRun WHERE RouteIndex = " + rset.getInt(1) + " AND Status = 0";
                stmt1 = conn.createStatement();
                rset1 = stmt1.executeQuery(Query1);
                if (rset1.next()) {
                    StudentCount = rset1.getInt(1);
                }
                rset1.close();
                stmt1.close();

                RouteList.append("<tr>");
                RouteList.append("<td width=02%>" + SrlNo + "</td>");
                RouteList.append("<td width=10%>" + rset.getString(2) + "</td>");//RouteID
                //RouteList.append("<td width=02%>" + RunCount + "</td>");//Run
                RouteList.append("<td width=02%><a class='label label-info tooltip-demo'  data-toggle=\"tooltip\" data-placement=\"top\" title=\"Click to Display Run Info\" href = \"/HopOn/HopOn.RouteAdd?ActionID=ShowRunRouteWise&RouteIndex=" + rset.getInt(1) + "&DriverName=" + rset.getString(6) + " \" target=\"_self\">" + RunCount + "</a></td>");

                //RouteList.append("<td width=02%>" + StudentCount + "</td>");//StudentCount
                RouteList.append("<td width=05%><a class='label label-primary tooltip-demo' data-toggle=\"tooltip\" data-placement=\"top\" title=\"Click for Student\" href = \"/HopOn/HopOn.RouteAdd?ActionID=ShowStudentRouteWise&RouteIndex=" + rset.getInt(1) + "&StudentCount= " + StudentCount + " \" target=\"_self\">" + StudentCount + "</a></td>");

                RouteList.append("<td width=02%>" + TotalRunDistance + " km</td>");//Total Distance
                RouteList.append("<td width=10%>" + rset.getString(6) + "</td>");//Driver
                if (rset.getInt(8) == 0)
                    RouteList.append("<td width=05%> <span class=\"label label-primary\">" + rset.getString(7) + "</span></td>");//Status
                else if (rset.getInt(8) == 1)
                    RouteList.append("<td width=05%> <span class=\"label label-danger\">" + rset.getString(7) + "</span></td>");//Status

                RouteList.append("<td width=05%>" + rset.getString(9) + "</td>");//CreatedDate
                RouteList.append("<td width=12%>" + rset.getString(10) + " - " + rset.getString(11) + "</td>");//ValidDate

                if (statusMarked == 0) {
                    RouteList.append("<td width=15%> ");
                    RouteList.append("<a href=\"#\" class=\"btn btn-primary btn-xs\"><i class=\"fa fa-folder\"></i> View </a>&nbsp;&nbsp;");
                    RouteList.append("<a href=\"#\" class=\"btn btn-success btn-xs\"><i class=\"fa fa-pencil\"></i> Edit </a>&nbsp;&nbsp;");
                    RouteList.append("<button class=\"btn btn-danger btn-xs mylink\" value=" + rset.getInt(1) + " target=NewFrame1> <font color = \"FFFFFF\"> <i class=\"fa fa-trash-o\"></i> [Delete] </font></button>");
                    RouteList.append("</td>");

                } else if (statusMarked == 1) {
                    RouteList.append("<td width=15%> ");
                    RouteList.append("<a href=\"#\" onclick=\"return false;\" class=\"btn btn-primary btn-xs\"><i class=\"fa fa-folder\"></i> View </a>&nbsp;&nbsp;");
                    RouteList.append("<a href=\"#\" onclick=\"return false;\" class=\"btn btn-success btn-xs\"><i class=\"fa fa-pencil\"></i> Edit </a>&nbsp;&nbsp;");
                    RouteList.append("<button class=\"btn btn-info btn-xs mylink1\" value=" + rset.getInt(1) + " target=NewFrame1> <font color = \"FFFFFF\"> <i class=\"fa fa-exchange\"></i> [Active] </font></button>");
                    RouteList.append("</td>");

                }
                RouteList.append("</tr>");
                ++SrlNo;
            }
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("DriverList", DriverList.toString());
            Parser.SetField("RouteList", RouteList.toString());
            Parser.SetField("RouteID", RouteID);
            Parser.SetField("CurrDate", CurrDate);
            Parser.SetField("add8Days", add8Days);

            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddRoute.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            Supportive.DumException("RouteAdd", "First Method -- GetInfo--MAIN", request, var19, getServletContext());
            try {
                Parsehtm Parser1 = new Parsehtm(request);
                Parser1.SetField("FormName", "RouteAdd");
                Parser1.SetField("ActionID", "GetInfo");
                Parser1.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String chk = "";

        String RouteID = request.getParameter("RouteID").trim();
        String Status = request.getParameter("Status").trim();
        Query = request.getParameter("DriverIndex").trim();
        int DriverIndex = Integer.parseInt(Query);
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);
        String StartDate = request.getParameter("StartDate");

        String SMonth = StartDate.substring(0, 2);
        String SDay = StartDate.substring(3, 5);
        String SYear = StartDate.substring(6, 10);

        String EndDate = request.getParameter("EndDate");

        String EMonth = EndDate.substring(0, 2);
        String EDay = EndDate.substring(3, 5);
        String EYear = EndDate.substring(6, 10);

        StartDate = SYear + "-" + SMonth + "-" + SDay;
        EndDate = EYear + "-" + EMonth + "-" + EDay;

        if (count == 0) {
            try {
                pStmt = conn.prepareStatement(
                        "INSERT INTO Routes (RouteID, DriverIndex, Status, CreatedDate, CreatedBy,FromDate,EndDate) " +
                                "VALUES (?,?,?,now(),?,?,?)");
                pStmt.setString(1, RouteID);
                pStmt.setInt(2, DriverIndex);
                pStmt.setString(3, Status);
                pStmt.setString(4, UserId);
                pStmt.setString(5, StartDate);
                pStmt.setString(6, EndDate);
                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "RouteAdd");
                    Parser.SetField("ActionID", "GetInfo");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception e1) {
                }

                Supportive.doLog(this.getServletContext(), "Add Route - 01", e.getMessage(), e);
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("RouteIndex").trim();
            int RouteIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE Routes SET RouteID = '" + RouteID + "',DriverIndex = " + DriverIndex + ", Status = " + Status + ", " +
                        " FromDate = '" + StartDate + "' , EndDate = '" + EndDate + "' , ModifiedBy = '" + UserId + "', ModifiedDate = NOW() " +
                        " WHERE Id = " + RouteIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception var34) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "RouteAdd");
                    Parser.SetField("ActionID", "GetInfo");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "Edit Route -02", var34.getMessage(), var34);
                out.flush();
                out.close();
            }
        }
        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();
    }

    private void EditRouteInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        Query = request.getParameter("RouteIndex").trim();
        int RouteIndex = Integer.parseInt(Query);
        String RouteID = "";
        int DriverIndex = 0;
        String Status = "";

        Query = "Select Id,RouteID,Status,DriverIndex from Routes where Id=" + RouteIndex;

        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                RouteID = rset.getString(2);
                Status = rset.getString(3);
                DriverIndex = rset.getInt(4);
            }
            rset.close();
            stmt.close();
        } catch (Exception var35) {
            out.flush();
            out.close();
            return;
        }

        out.println(RouteID + "|" + Status + "|" + DriverIndex + "|" + RouteIndex);
        out.flush();
        out.close();
    }

    private void DeleteRoute(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        Query = "";
        stmt = null;
        rset = null;
        Query = request.getParameter("RouteIndex").trim();
        int RouteIndex = Integer.parseInt(Query);
        String RouteID = "";
        int DriverIndex = 0;
        int Status = 0;
        int Result = 0;

        Query = "Select Id,RouteID,Status,DriverIndex from Routes where Id=" + RouteIndex;

        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                RouteID = rset.getString(2);
                Status = rset.getInt(3);
                DriverIndex = rset.getInt(4);
            }
            rset.close();
            stmt.close();
        } catch (Exception var35) {
            out.flush();
            out.close();
            return;
        }

        //Making it InActive
        if (Status == 0) {
            Result = helper.DeleteRouteInfo(request, RouteIndex, 1, conn, servletContext);
        } else if (Status == 1) {
            Result = helper.DeleteRouteInfo(request, RouteIndex, 0, conn, servletContext);
        }
        out.println(Result);
    }

    private void ShowRunRouteWise(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuilder RunTable = new StringBuilder();
        StringBuilder RunTableInfo;
        StringBuilder SchoolList;

        int RouteIndex = Integer.parseInt(request.getParameter("RouteIndex").trim());
        String DriverName = request.getParameter("DriverName").trim();

        int RunCount = helper.RunCount(request, RouteIndex, conn, servletContext);
        //int[] RunCountInfo = helper.RunCountInfo(request, RouteIndex, conn, servletContext);
        //int RunCount = RunCountInfo[0];
        int CountRecord = helper.RunCountInStudentWiseRunTable(request, RouteIndex, conn, servletContext);
        String RouteID = helper.RouteID(request, RouteIndex, conn, servletContext);
        SchoolList = helper.SchoolList(request, conn, servletContext);

/*        out.println("RouteIndex " + RouteIndex + "<br>");
        out.println("DriverName " + DriverName + "<br>");
        out.println("RouteID " + RouteID + "<br>");
        out.println("RunCount " + RunCount + "<br>");
        out.println("CountRecord " + CountRecord + "<br>");*/

        if (RunCount == 0) {
            RunTable.append(" <div class=\"row\">");
            RunTable.append("<div class=\"col-lg-12\">");
            RunTable.append("<div class=\"ibox float-e-margins\">");
            RunTable.append("<div class=\"ibox-title\" style=\"color:#fff; background-color: #1ab394; \">");
            RunTable.append("<h5>Selected Route Details</h5>");
            RunTable.append("</div>");
            RunTable.append("<div class=\"ibox-content\">");
            RunTable.append("<div class=\"alert alert-warning\">");
            RunTable.append("No Run Data for the Route: " + RouteID + " <a class=\"alert-link\" href=\"#\">Click for Details!</a>.");
            RunTable.append("</div>");
            RunTable.append("</div>");
            RunTable.append("</div>");
            RunTable.append("</div>");
            RunTable.append("</div>");
        } else {
            RunTableInfo = helper.RunInfo(request, RouteIndex, conn, servletContext);

            RunTable.append("<div class=\"row\">");
            RunTable.append("<div class=\"col-lg-12\">");
            RunTable.append("<div class=\"ibox float-e-margins\">");
            RunTable.append("<div class=\"ibox-title\" style=\"color:#fff; background-color: #1ab394; \">");
            RunTable.append("<h5>Run Information </h5>");
            RunTable.append("<div class=\"ibox-tools\">");
            RunTable.append("<a class=\"collapse-link\">");
            RunTable.append("<i class=\"fa fa-chevron-up\"></i>");
            RunTable.append("</a>");
            RunTable.append("<a class=\"dropdown-toggle\" data-toggle=\"dropdown\" href=\"#\">");
            RunTable.append("<i class=\"fa fa-wrench\"></i>");
            RunTable.append("</a>");
            RunTable.append("<ul class=\"dropdown-menu dropdown-user\">");
            RunTable.append("<li><a href=\"#\">Config option 1</a>");
            RunTable.append("</li>");
            RunTable.append("<li><a href=\"#\">Config option 2</a>");
            RunTable.append("</li>");
            RunTable.append("</ul>");
            RunTable.append("<a class=\"close-link\">");
            RunTable.append("<i class=\"fa fa-times\"></i>");
            RunTable.append("</a>");
            RunTable.append("</div>");
            RunTable.append("</div>");
            RunTable.append("<div class=\"ibox-content\">");

            if (CountRecord > 0) {
                RunTable.append("<div class=\"alert alert-danger\">");
                RunTable.append("This Route have some Runs which do not have Student(s) <a class=\"alert-link\" href=\"#\">Click here to Refresh!</a>.");
                RunTable.append("</div>");
            }

            RunTable.append("<h4 style=\"text-align:left;\" class=\"ibox-title\"><b>Route # : " + RouteID + " </b> </h4> ");
            RunTable.append("<h4 style=\"text-align:left;\" ><b>Driver : " + DriverName + " </b> </h4> ");
            RunTable.append("<table class=\"table table-bordered\">");
            RunTable.append("<thead>");
            RunTable.append("<tr>");
            RunTable.append("<th>RunID</th>");
            RunTable.append("<th>Rate</th>");
            RunTable.append("<th>Start Date</th>");
            RunTable.append("<th>Distance</th>");
            RunTable.append("<th>RunType</th>");
            RunTable.append("<th>Student</th>");
            RunTable.append("<th>Action</th>");
            RunTable.append("</tr>");
            RunTable.append("</thead>");
            RunTable.append("<tbody>");

            RunTable.append(RunTableInfo);

            RunTable.append("</tbody>");
            RunTable.append("</table>");
            RunTable.append("</div>");
            RunTable.append("</div>");
            RunTable.append("</div>");
            RunTable.append(" </div>");
        }

        //out.println(RouteID + "|" + DriverName + "|" + RunTable + "|" + RunTableInfo);
        try {
            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("myDATA", RunTable.toString());
            Parser.SetField("SchoolList", SchoolList.toString());
            Parser.SetField("RouteIndex", String.valueOf(RouteIndex));
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/showRunInfo.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            Supportive.DumException("RouteAdd", "ShowRunRouteWise ", request, var19, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "RouteAdd");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

    }

    private void ShowStudentRouteWise(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";

        int RouteIndex = Integer.parseInt(request.getParameter("RouteIndex").trim());
        int StudentCount = Integer.parseInt(request.getParameter("StudentCount").trim());


        StringBuilder StudentInfo = new StringBuilder();

        try {
            if (StudentCount <= 0) {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddRun");
                Parser.SetField("ActionID", "GetInfo");
                Parser.SetField("Message", "Student Has Not Been Defined For This Run!");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
                return;
            }

            Query = "SELECT a.RouteIndex,a.RunIndex,a.StudentIndex,a.SchoolIndex," +
                    "b.ChildName,b.Age, DATE_FORMAT(b.DOB,'%d-%b-%Y') AS DOB,c.Grade," +
                    "b.Description,d.FullName, b.PhoneNumber " +
                    "FROM StudentWiseRun a " +
                    "STRAIGHT_JOIN Childrens b ON a.StudentIndex = b.Id " +
                    "STRAIGHT_JOIN Grades c ON b.Grade = c.Id " +
                    "STRAIGHT_JOIN Parents d ON b.ParentIndex = d.Id " +
                    "WHERE a.RouteIndex = " + RouteIndex + " AND a.`Status` = 0";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                StudentInfo.append("<div class=\"col-lg-4\" >");
                StudentInfo.append("<div class=\"widget-head-color-box navy-bg p-lg text-center\" >");
                StudentInfo.append("<div class=\"m-b-md\" >");
                StudentInfo.append("<h2 class=\"font-bold no-margins\" >");
                StudentInfo.append(rset.getString(5));
                StudentInfo.append("</h2>");
                StudentInfo.append("</div >");
                StudentInfo.append("<img src = \"/HopOn/library/img/a4.jpg\" class=\"img-circle circle-border m-b-md\" alt = \"profile\">");
                StudentInfo.append(" <div >");
                StudentInfo.append("<span > " + rset.getInt(6) + " Yrs</span > |");
                StudentInfo.append("<span > " + rset.getString(7) + " </span > |");
                StudentInfo.append("<span > " + rset.getString(8) + " Grade </span >");
                StudentInfo.append("</div >");
                StudentInfo.append("</div >");
                StudentInfo.append("<div class=\"widget-text-box\" >");
                StudentInfo.append("<h4 class=\"media-heading\" > Parent Name : " + rset.getString(10) + "</h4>");
                StudentInfo.append("<h4 class=\"media-heading\" > Name : " + rset.getString(5) + "</h4>");
                StudentInfo.append("<h4 class=\"media-heading\" > Age : " + rset.getString(6) + " Yrs </h4>");
                StudentInfo.append("<h4 class=\"media-heading\" > DOB : " + rset.getString(7) + "</h4>");
                StudentInfo.append("<h4 class=\"media-heading\" > Grade : " + rset.getString(8) + "</h4>");
                StudentInfo.append("<h4 class=\"media-heading\" > Phone : " + rset.getString(11) + "</h4>");

                //StudentInfo.append("<p>" + rset.getString(9) + "</p>");
                StudentInfo.append("<div class=\"text-right\" >");
                StudentInfo.append("<a class=\"btn btn-xs btn-white\" ><i class=\"fa fa-thumbs-up\" ></i > Like </a>");
                StudentInfo.append("<a class=\"btn btn-xs btn-primary\" ><i class=\"fa fa-heart\" ></i > Love </a>");
                StudentInfo.append("</div>");
                StudentInfo.append("</div>");
                StudentInfo.append("</div>");

            }
            rset.close();
            stmt.close();


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("StudentInfo", StudentInfo.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/showStudentDetails.html");
        } catch (Exception Ex) {
            Supportive.DumException("RouteAdd", "showStudent -- 009", request, Ex, getServletContext());
            try {
                Parsehtm Parser1 = new Parsehtm(request);
                Parser1.SetField("FormName", "RouteAdd");
                Parser1.SetField("ActionID", "GetInfo");
                Parser1.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
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
