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

public class AddRun extends HttpServlet {
    String ScreenNo = "0";
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;
    CallableStatement cStmt;

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
                case "FetchRouteData":
                    this.FetchRouteData(request, out, conn, context, UserId, helper);
                    break;
                case "SaveRun":
                    SaveRecords(request, out, conn, context, UserId, helper);
                    break;
                case "DeleteRun":
                    DeleteRun(request, out, conn, context, UserId, helper);
                    break;
                case "addStudent":
                    addStudent(request, out, conn, context, UserId, helper);
                    break;
                case "ShowStudent":
                    showStudent(request, out, conn, context, UserId, helper);
                    break;
                case "showLocation":
                    showLocation(request, out, conn, context, UserId, helper);
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
        StringBuilder RouteList;
        StringBuilder DriverList;
        StringBuilder SchoolList;
        String userIP = "", _tkt = "";
        String RunID = "", CityName = "";
        try {

            DriverList = helper.DriverList(request, conn, servletContext);
            RouteList = helper.RouteList(request, conn, servletContext);
            userIP = helper.getClientIp(request);
            SchoolList = helper.SchoolList(request, conn, servletContext);
            CityName = helper.CityName(request, CityIndex, conn, servletContext);

            Query = "SELECT SUBSTRING(IFNULL(MAX(Convert(Substring(RunID,9,4) ,UNSIGNED INTEGER)),0)+10001,2,4) " +
                    " FROM Runs WHERE Id = (SELECT MAX(ID) FROM Runs) ";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next())
                    _tkt = rset.getString(1);
                rset.close();
                stmt.close();
            } catch (SQLException ex) {
                out.println("EXCEPTION 3 <BR>");
                Supportive.doLog(this.getServletContext(), "Record Insertion-03", ex.getMessage(), ex);
                out.flush();
                out.close();
            }

            RunID = "HO-Run#" + _tkt + "-" + CityName + "";

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("DriverList", DriverList.toString());
            Parser.SetField("RouteList", RouteList.toString());
            Parser.SetField("SchoolList", SchoolList.toString());
            Parser.SetField("RunID", RunID);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddRun.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            Supportive.DumException("AddRun", "First Method -- GetInfo--MAIN", request, var19, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddRun");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        Query = "";
        stmt = null;
        rset = null;

        int RouteIndex = Integer.parseInt(request.getParameter("RouteIndex").trim());
        String RunID = request.getParameter("RunID").trim();
//        double Distance = Double.parseDouble(request.getParameter("Distance").trim());
        String Type = request.getParameter("Type").trim();
        String Rate = request.getParameter("Rate").trim();
        String SDate = request.getParameter("SDate").trim();
        String SMonth = SDate.substring(0, 2);
        String SDay = SDate.substring(3, 5);
        String SYear = SDate.substring(6, 10);
        String StartDate = SYear + "-" + SMonth + "-" + SDay + " 00:00:00";

        String ddlTimeH = request.getParameter("ddlTimeH").trim();
        String ddlTimeM = request.getParameter("ddlTimeM").trim();
        String Comments = request.getParameter("Comments").trim();
        String DeliveryTime = ddlTimeH + ":" + ddlTimeM;

        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO Runs (RunID, Rate, StartDate, Type, DeliveryTime, Comments, RouteIndex, Status, CreatedDate,CreatedBy) " +
                            "VALUES (?,?,?,?,?,?,?,0,now(),?)");
            pStmt.setString(1, RunID);
            pStmt.setString(2, Rate);
//            pStmt.setDouble(3, Distance);
            pStmt.setString(3, StartDate);
            pStmt.setString(4, Type);
            pStmt.setString(5, DeliveryTime);
            pStmt.setString(6, Comments);
            pStmt.setInt(7, RouteIndex);
            pStmt.setString(8, UserId);
            pStmt.executeUpdate();
            pStmt.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddRun");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception e1) {
            }

            Supportive.doLog(this.getServletContext(), "Add Run - 02", e.getMessage(), e);
            out.flush();
            out.close();
        }
        String RouteID;
        try {
            RouteID = helper.RouteID(request, RouteIndex, conn, servletContext);

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", "AddRun");
            Parser.SetField("ActionID", "GetInfo");
            Parser.SetField("Message", "Run has been successfully added for the Route of " + RouteID + ". Please add students in latest run.");
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
        } catch (Exception Ex) {
            out.println("Error3" + Ex.getMessage());
            Supportive.doLog(servletContext, "Add Run -- Second Function ** 03 ", Ex.getMessage(), Ex);
            out.close();
            out.flush();
            return;
        }

    }

    private void FetchRouteData(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";

        StringBuilder RunTable = new StringBuilder();
        StringBuilder RunTableInfo = new StringBuilder();
        Query = request.getParameter("RouteIndex");
        int RouteIndex = Integer.parseInt(Query);

        String RouteNum;
        String DriverName;
        try {
            if (RouteIndex > 0) {
                String[] routeInfo = helper.getRouteInfo(request, RouteIndex, conn, servletContext);

                RouteNum = routeInfo[0];
                DriverName = routeInfo[1];
            } else {
                RouteNum = "";
                DriverName = "";
            }

//            out.println("RouteIndex " + RouteIndex + "<br>");
//            int[] RunCountInfo = helper.RunCountInfo(request, RouteIndex, conn, servletContext);
            int RunCount = 0, TotalDistance = 0;
//            RunCount = RunCountInfo[0];
//            TotalDistance = RunCountInfo[1];

            RunCount = helper.RunCount(request, RouteIndex, conn, servletContext);

            int CountRecord = helper.RunCountInStudentWiseRunTable(request, RouteIndex, conn, servletContext);

            if (RunCount == 0) {
                RunTable.append(" <div class=\"row\">");
                RunTable.append("<div class=\"col-lg-12\">");
                RunTable.append("<div class=\"ibox float-e-margins\">");
                RunTable.append("<div class=\"ibox-title\" style=\"color:#fff; background-color: #1ab394; \">");
                RunTable.append("<h5>Selected Route Details</h5>");
                RunTable.append("</div>");
                RunTable.append("<div class=\"ibox-content\">");
                RunTable.append("<div class=\"alert alert-warning\">");
                RunTable.append("No Run Data for the Selected Route <a class=\"alert-link\" href=\"#\">Click for Details!</a>.");
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

                RunTable.append("<h4 style=\"text-align:left;\" class=\"ibox-title\"><b>Route # : " + RouteNum + " </b> </h4> ");
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


            out.println(RouteNum + "|" + DriverName + "|" + RunTable + "|" + RunTableInfo);

        } catch (Exception Ex) {
            Supportive.DumException("AddRun", "Second Method -- FetchRouteData-- 002 ", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddRun");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

    }

    private void DeleteRun(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        Query = "";
        stmt = null;
        rset = null;
        Query = request.getParameter("RunIndex").trim();
        int RunIndex = Integer.parseInt(Query);

        int _RunIndex = 0;
        int Status = 0;
        int Result = 0;

        Query = "Select Id,Status from Runs where Id=" + RunIndex;

        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                _RunIndex = rset.getInt(1);
                Status = rset.getInt(2);

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
            Result = helper.DeleteRunInfo(request, RunIndex, 1, conn, servletContext);
        } else if (Status == 1) {
            Result = helper.DeleteRunInfo(request, RunIndex, 0, conn, servletContext);
        }
        out.println(Result);
    }

    private void addStudent(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        Query = "";
        stmt = null;
        rset = null;
        Query = request.getParameter("RunIndex").trim();
        int RunIndex = Integer.parseInt(Query);
        int SchoolIndex = Integer.parseInt(request.getParameter("SchoolIndex").trim());
        int StudentIndex = Integer.parseInt(request.getParameter("StudentIndex").trim());
        int RouteIndex = Integer.parseInt(request.getParameter("RouteIndex").trim());

        String CurrDate = "";
        Query = "{CALL CurrentDate()}";
        try {
            cStmt = conn.prepareCall(Query);
            rset = cStmt.executeQuery();
            if (rset.next())
                CurrDate = rset.getString(1).trim();
            rset.close();
            cStmt.close();
        } catch (SQLException ex) {
            Supportive.doLog(this.getServletContext(), "AddRun--Record Insertion-01", ex.getMessage(), ex);
            out.flush();
            out.close();
        }

        try {
            Query = "{CALL SP_SAVE_StudentWiseRun(?,?,?,?,?,?,?)}";
            cStmt = conn.prepareCall(Query);

            cStmt.setInt(1, RunIndex);
            cStmt.setInt(2, StudentIndex);
            cStmt.setInt(3, SchoolIndex);
            cStmt.setInt(4, 0);//Status
            cStmt.setString(5, CurrDate);//CurrDate
            cStmt.setString(6, UserId);//UserId
            cStmt.setInt(7, RouteIndex);//RouteIndex

            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();

            out.println("1");

/*            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", "AddRun");
            Parser.SetField("ActionID", "GetInfo");
            Parser.SetField("Message", "Student Has Been Entered Successfully!");
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");*/

        } catch (Exception Ex) {
            Supportive.DumException("AddRun", "addStudent -- 008", request, Ex, getServletContext());
            try {
                Parsehtm Parser1 = new Parsehtm(request);
                Parser1.SetField("FormName", "AddRun");
                Parser1.SetField("ActionID", "GetInfo");
                Parser1.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

    }

    private void showStudent(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";

        int RouteIndex = Integer.parseInt(request.getParameter("RouteIndex").trim());
        int RunIndex = Integer.parseInt(request.getParameter("RunIndex").trim());
        int CountRecord = Integer.parseInt(request.getParameter("CountRecord").trim());

        StringBuilder StudentInfo = new StringBuilder();

        try {
            if (CountRecord <= 0) {
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
                    "WHERE a.RouteIndex = " + RouteIndex + " AND a.RunIndex = " + RunIndex + " AND a.`Status` = 0";
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
            Supportive.DumException("AddRun", "showStudent -- 009", request, Ex, getServletContext());
            try {
                Parsehtm Parser1 = new Parsehtm(request);
                Parser1.SetField("FormName", "AddRun");
                Parser1.SetField("ActionID", "GetInfo");
                Parser1.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void showLocation(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";
        int SNo = 0;
        String PickupLat = "";
        String PickupLon = "";
        String DropOffLat = "";
        String DropOffLon = "";
        String ChildName = "";
        String SchoolName = "";
        int RouteIndex = Integer.parseInt(request.getParameter("RouteIndex").trim());
        int RunIndex = Integer.parseInt(request.getParameter("RunIndex").trim());

//        out.println("RouteIndex" + RouteIndex +"<br>");
//        out.println("RunIndex" + RunIndex +"<br>");
        StringBuilder LocationBtn = new StringBuilder();

        try {
            Query = "SELECT a.ChildName, b.SchoolName, a.PickupLat, a.PickupLon,  a.DropOffLat, a.DropOffLon, d.RunID, a.Id," +
                    "REPLACE(a.ChildName, ' ', '-' ), REPLACE(b.SchoolName, ' ', '-' ) " +
                    "FROM Childrens a " +
                    "INNER JOIN School b ON a.SchoolIndex = b.Id " +
                    "INNER JOIN StudentWiseRun c ON a.Id = c.StudentIndex " +
                    "INNER JOIN Runs d ON c.RunIndex = d.Id AND d.Id = " + RunIndex + " ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            LocationBtn.append("<select class=\"form-control m-b\" name=\"StdList\" id=\"StdList\">");
            LocationBtn.append("<option value='' readonly selected>Select Student</option>");
            while (rset.next()) {
                if (SNo == 0) {
                    PickupLat = rset.getString(3);
                    PickupLon = rset.getString(4);
                    DropOffLat = rset.getString(5);
                    DropOffLon = rset.getString(6);
                    ChildName = rset.getString(1);
                    SchoolName = rset.getString(2);
                }
/*                out.println("ChildName " + rset.getString(1) + "<br>");
                out.println("SchoolName " + rset.getString(2) + "<br>");
                out.println("PickupLat " + rset.getString(3) + "<br>");
                out.println("PickupLon " + rset.getString(4) + "<br>");
                out.println("DropOffLat " + rset.getString(5) + "<br>");
                out.println("DropOffLon " + rset.getString(6) + "<br>");
                out.println("****************************************************** <br> ");*/
//                out.println("<a href=\"/HopOn/HopOn.AddRun?ActionID=showLocation&RunIndex="+rset.getString(1)+"&RouteIndex="+RouteIndex+" \" target=_\"self\" class=\"btn btn-success btn-sm\"><i class=\"fa fa-map-marker\"></i> "+rset.getString(1)+" </a>&nbsp;&nbsp;");
                //LocationBtn.append("<button type=\"button\" value="+rset.getString(3)+","+rset.getString(4)+","+rset.getString(5)+","+rset.getString(6)+" class=\"btn btn-w-m btn-primary\">"+rset.getString(1)+"</button>&nbsp;&nbsp;");

                LocationBtn.append("<option value=" + rset.getString(3) + "," + rset.getString(4) + "," + rset.getString(5) + "," + rset.getString(6) + "," + rset.getString(9) + "," + rset.getString(10) + " >" + rset.getString(1) + "</option>");
                //LocationBtn.append("<option value=" + rset.getString(1) + ">" + rset.getString(1) + "</option>");

                SNo++;
            }
            LocationBtn.append("</select>");
            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("LocationBtn", LocationBtn.toString());
            Parser.SetField("PickupLat", PickupLat);
            Parser.SetField("PickupLon", PickupLon);
            Parser.SetField("DropOffLat", DropOffLat);
            Parser.SetField("DropOffLon", DropOffLon);
            Parser.SetField("ChildName", ChildName);
            Parser.SetField("SchoolName", SchoolName);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/ViewRLNEW.html");
        } catch (Exception Ex) {
            Supportive.DumException("AddRun", "showLocation -- showLocation ", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddRun");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

}
