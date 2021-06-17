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

public class CreateDriver extends HttpServlet {
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
            UtilityHelper helper = new UtilityHelper();
            switch (Action) {
                case "GetDriver":
                    GetDriver(request, out, conn, context, UserId, CityIndex);
                    break;
                case "GetInitials":
                    GetInitials(request, out, conn, context, UserId, CityIndex, helper);
                    break;
                case "SaveRecords":
                    SaveDriver(request, out, conn, context, UserId, helper);
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

    private void GetDriver(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex) {
        stmt = null;
        rset = null;
        Query = "";

        StringBuilder SelectDriver = new StringBuilder();

        try {

            Query = "SELECT Id,UserName FROM SystemUsers WHERE Status=0 AND UserType='D' ORDER BY UserName ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SelectDriver.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("SelectDriver", SelectDriver.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/SelectDriver.html");
        } catch (Exception Ex) {
            Supportive.DumException("CreateDriver", "Zero Method -- GetDriver--MAIN", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "CreateDriver");
                Parser.SetField("ActionID", "GetDriver");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void GetInitials(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuilder Country = new StringBuilder();
        StringBuilder CarType = new StringBuilder();
        StringBuilder UserSelection = new StringBuilder();
        int SystemIndex = helper.UserIndex(request, UserId, conn, servletContext);
        String UserType = helper.UserType(request, SystemIndex, conn, servletContext);

//        out.println("SystemIndex " + SystemIndex + "<br>");
//        out.println("UserType " + UserType + "<br>");
        //int SelectedDriverIndex = Integer.parseInt(request.getParameter("DriverList").trim());

        try {
            if (UserType.equals("A")) {
                UserSelection.append("<div class=\"form-group\">");
                UserSelection.append("<label class=\"col-md-4 control-label\">Driver List</label>");
                UserSelection.append("<div class=\"col-md-8 col-sm-8 col-xs-12 \">");
                UserSelection.append("<select class=\"form-control m-b\" name=\"DriverList\" id=\"DriverList\"> ");
                UserSelection.append("<option value=\"\" selected disabled>Please select one..</option>");
                Query = "SELECT a.Id,a.UserName FROM SystemUsers a WHERE a.Status=0 AND a.UserType='D' AND a.Id " +
                        "NOT IN (SELECT x.SystemUserIndex FROM Drivers x WHERE x.SystemUserIndex = a.Id) ORDER BY a.UserName ";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    UserSelection.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
                }
                rset.close();
                stmt.close();
                UserSelection.append("</select>");
                UserSelection.append("</div>");
                UserSelection.append("</div>");
            } else {
                UserSelection.append("<div class=\"form-group\">");
                UserSelection.append("<label class=\"col-md-4 control-label\">Driver Name</label>");
                UserSelection.append("<div class=\"col-md-8 col-sm-8 col-xs-12 \">");
                String DriverName = helper.SystemUserName(request, SystemIndex, conn, servletContext);
                UserSelection.append("<input type=\"text\" id=\"DriverName\" name=\"DriverName\" readonly value='" + DriverName + "' class=\"form-control\">");

                UserSelection.append("</div>");
                UserSelection.append("</div>");
            }

            boolean isUserExist = false;
            try {
                isUserExist = helper.isDriverExist(request, SystemIndex, conn, servletContext);
//                out.println("isUserExist " + isUserExist + "<br>");
                if (isUserExist) {
/*                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "CreateDriver");
                    Parser.SetField("ActionID", "GetDriver");
                    Parser.SetField("Message", "User Has already been Registered!!");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
                    return;*/
                    CreateDriver createDriver = new CreateDriver();
                    createDriver.driverUserExist(request, out, conn, servletContext, SystemIndex, helper);
                } else {
                    try {
                        Query = "SELECT Id,CountryName FROM Country WHERE Status=0 ORDER BY CountryName ";
                        stmt = conn.createStatement();
                        rset = stmt.executeQuery(Query);
                        Country.append("<option value='' selected >Select Country</option>");
                        while (rset.next()) {
                            Country.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
                        }

                        rset.close();
                        stmt.close();
                    } catch (Exception var17) {
                        Supportive.DumException("CreateDriver", "Country Query -- GetInfo--001", request, var17, getServletContext());
                        try {
                            Parsehtm Parser = new Parsehtm(request);
                            Parser.SetField("FormName", "AddFamily");
                            Parser.SetField("ActionID", "GetInfo");
                            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                        } catch (Exception var171) {
                        }
                    }
                    try {
                        Query = "SELECT Id,CarType FROM CarType WHERE Status=0 ORDER BY CarType ";
                        stmt = conn.createStatement();
                        rset = stmt.executeQuery(Query);
                        CarType.append("<option value='' selected >Select Car Type</option>");
                        while (rset.next()) {
                            CarType.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
                        }

                        rset.close();
                        stmt.close();
                    } catch (Exception var17) {
                        Supportive.DumException("CreateDriver", "Car Type Query -- GetInfo--002", request, var17, getServletContext());
                        try {
                            Parsehtm Parser = new Parsehtm(request);
                            Parser.SetField("FormName", "AddFamily");
                            Parser.SetField("ActionID", "GetInfo");
                            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                        } catch (Exception var171) {
                        }
                    }
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("Country", Country.toString());
                    Parser.SetField("CarType", CarType.toString());
                    Parser.SetField("UserSelection", UserSelection.toString());
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/CreateDriver.html");
                }
            } catch (Exception ex) {
                Supportive.doLog(this.getServletContext(), "CreateDriver -- GetInitials", ex.getMessage(), ex);
                out.flush();
                out.close();
            }
        } catch (Exception Ex) {
            Supportive.DumException("CreateDriver", "First Method -- GetInitials--MAIN", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "CreateDriver");
                Parser.SetField("ActionID", "GetInitials");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void SaveDriver(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        Query = "";
        cStmt = null;
        rset = null;

//PersonalInfo
        int DriverList = Integer.parseInt(request.getParameter("DriverList").trim());
        String FName = request.getParameter("FName").trim();
        String MName = request.getParameter("MName").trim();
        String LName = request.getParameter("LName").trim();
        String FullName = FName + " " + LName + " " + MName;
        String DOB = request.getParameter("DOB").trim();
        String Month = DOB.substring(0, 2);
        String Day = DOB.substring(3, 5);
        String Year = DOB.substring(6, 10);
        String DateOfBirth = Year + "-" + Month + "-" + Day + " 00:00:00";
        String Gender = request.getParameter("Gender").trim();

        //Address Details
        String Address1 = request.getParameter("Address1").trim();
        String Address2 = request.getParameter("Address2").trim();
        String StreetAddress1 = request.getParameter("StreetAddress1").trim();
        String StreetAddress2 = request.getParameter("StreetAddress2").trim();
        int Country = Integer.parseInt(request.getParameter("Country").trim());
        int Province = Integer.parseInt(request.getParameter("Province").trim());
        int City = Integer.parseInt(request.getParameter("City").trim());
        String POBox = request.getParameter("POBox").trim();
        String TelePhone = request.getParameter("TelePhone").trim();
        String CellPhone = request.getParameter("CellPhone").trim();
        String Email = request.getParameter("Email").trim();
        String PostCode = request.getParameter("PostCode").trim();

        //Driving Card Details
        String DLicense = request.getParameter("DLicense").trim();
        String DCategory = request.getParameter("DCategory").trim();
        String DDate = request.getParameter("DDate").trim();
        String DMonth = DDate.substring(0, 2);
        String DDay = DDate.substring(3, 5);
        String DYear = DDate.substring(6, 10);
        String DrivingLicenseDate = DYear + "-" + DMonth + "-" + DDay + " 00:00:00";

        String EDate = request.getParameter("EDate").trim();
        String EMonth = EDate.substring(0, 2);
        String EDay = EDate.substring(3, 5);
        String EYear = EDate.substring(6, 10);
        String ExpiryDate = EYear + "-" + EMonth + "-" + EDay + " 00:00:00";

        String AIDate = request.getParameter("AIDate").trim();
        String AIMonth = AIDate.substring(0, 2);
        String AIDay = AIDate.substring(3, 5);
        String AIYear = AIDate.substring(6, 10);
        AIDate = AIYear + "-" + AIMonth + "-" + AIDay + " 00:00:00";

        String AEDate = request.getParameter("AEDate").trim();
        String AEMonth = AEDate.substring(0, 2);
        String AEDay = AEDate.substring(3, 5);
        String AEYear = AEDate.substring(6, 10);
        AEDate = AEYear + "-" + AEMonth + "-" + AEDay + " 00:00:00";

        String CPRIDate = request.getParameter("CPRIDate").trim();
        String CPRMonth = CPRIDate.substring(0, 2);
        String CPRDay = CPRIDate.substring(3, 5);
        String CPRYear = CPRIDate.substring(6, 10);
        CPRIDate = CPRYear + "-" + CPRMonth + "-" + CPRDay + " 00:00:00";

        String CPREDate = request.getParameter("CPREDate").trim();
        String CPREMonth = CPREDate.substring(0, 2);
        String CPREDay = CPREDate.substring(3, 5);
        String CPREYear = CPREDate.substring(6, 10);
        CPREDate = CPREYear + "-" + CPREMonth + "-" + CPREDay + " 00:00:00";

        //Car Details
        String CMake = request.getParameter("CMake").trim();
        String CModel = request.getParameter("CModel").trim();
        int CarType = Integer.parseInt(request.getParameter("CarType").trim());
        int Passenger = Integer.parseInt(request.getParameter("Passenger").trim());
        int CYear = Integer.parseInt(request.getParameter("CYear").trim());

        String IExpiry = request.getParameter("IXpiry").trim();
        String IXMonth = IExpiry.substring(0, 2);
        String IXDay = IExpiry.substring(3, 5);
        String IXYear = IExpiry.substring(6, 10);
        IExpiry = IXYear + "-" + IXMonth + "-" + IXDay + " 00:00:00";

        String VSCExpiry = request.getParameter("VSCXpiry").trim();
        String VSCMonth = VSCExpiry.substring(0, 2);
        String VSCDay = VSCExpiry.substring(3, 5);
        String VSCYear = VSCExpiry.substring(6, 10);
        VSCExpiry = VSCYear + "-" + VSCMonth + "-" + VSCDay + " 00:00:00";

        String CurrDate = helper.getCurrDate(request, conn);
        //int SystemUserIndex = helper.UserIndex(request, UserId, conn, servletContext);

        Query = "{CALL SP_SAVE_DriverInfo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try {
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, FullName);
            cStmt.setString(2, DateOfBirth);
            cStmt.setString(3, Gender);
            cStmt.setString(4, Address1);
            cStmt.setString(5, Address2);
            cStmt.setString(6, StreetAddress1);
            cStmt.setString(7, StreetAddress2);
            cStmt.setInt(8, Country);
            cStmt.setInt(9, Province);
            cStmt.setInt(10, City);
            cStmt.setString(11, POBox);
            cStmt.setString(12, TelePhone);
            cStmt.setString(13, CellPhone);
            cStmt.setString(14, Email);
            cStmt.setString(15, PostCode);
            cStmt.setString(16, DLicense);
            cStmt.setString(17, DCategory);
            cStmt.setString(18, DrivingLicenseDate);
            cStmt.setString(19, ExpiryDate);
            cStmt.setString(20, AIDate);
            cStmt.setString(21, AEDate);
            cStmt.setString(22, CPRIDate);
            cStmt.setString(23, CPREDate);
            cStmt.setString(24, CMake);
            cStmt.setString(25, CModel);
            cStmt.setInt(26, CarType);
            cStmt.setInt(27, Passenger);
            cStmt.setInt(28, CYear);
            cStmt.setString(29, IExpiry);
            cStmt.setString(30, VSCExpiry);
            cStmt.setInt(31, 0);
            cStmt.setString(32, CurrDate);
            cStmt.setString(33, UserId);
            cStmt.setInt(34, DriverList);
            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", "CreateDriver");
            Parser.SetField("ActionID", "GetDriver");
            Parser.SetField("Message", "Driver Information Has been entered Successfully!!");
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
        } catch (Exception ex) {
            Supportive.doLog(this.getServletContext(), "CreateDriver -- Record Insertion-02", ex.getMessage(), ex);
            out.flush();
            out.close();
        }
    }

    private void driverUserExist(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, int SystemIndex, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";
        String FullName = "";
        String DOB = "";
        String Gender = "";
        String Address1 = "";
        String Address2 = "";
        String Street1 = "";
        String Street2 = "";
        String CountryIndex = "";
        String ProvinceIndex = "";
        String CityIndex = "";
        String PostalCode = "";
        String Home = "";
        String CellPhone = "";
        String Email = "";
        String DrivingLicense = "";
        String Category = "";
        String LicenseDate = "";
        String ExpiryDate = "";
        String AbstractIssueDate = "";
        String AExpiryDate = "";
        String CPRIssueDate = "";
        String CPRExpiryDate = "";
        String CarMake = "";
        String CarModel = "";
        String CarType = "";
        String CarYear = "";
        String Passenger = "";
        String POBox = "";
        String InsuranceExpiryDate = "";
        String VCSExpiryDate = "";
        String SystemUserIndex = "";
        String FirstName = "";
        String LastName = "";
        String MiddleName = "";

        try {
            Query = "SELECT a.FullName ,a.DOB ,a.Gender ,a.Address1 ,a.Address2 ,a.Street1 ,a.Street2 ,b.CountryName ,c.ProvinceName ,d.CityName ," +
                    "a.PostalCode ,a.Home ,a.CellPhone ,a.Email ,a.DrivingLicense ,a.Category ,a.LicenseDate ,a.ExpiryDate ,a.AbstractIssueDate ," +
                    "a.AExpiryDate ,a.CPRIssueDate ,a.CPRExpiryDate ,a.CarMake ,a.CarModel ,a.CarType ,a.CarYear ,a.Passenger,a.POBox ,a.InsuranceExpiryDate," +
                    "a.VCSExpiryDate ,a.SystemUserIndex, " +
                    "SUBSTRING_INDEX(a.FullName, ' ', 1) AS FirstName , SUBSTRING_INDEX(SUBSTRING_INDEX(a.FullName, ' ', 2), ' ', -1) AS LastName, SUBSTRING_INDEX(SUBSTRING_INDEX(a.FullName, ' ', 3), ' ', -1) AS MiddleName " +
                    " FROM Drivers a " +
                    " STRAIGHT_JOIN Country b ON a.CountryIndex = b.Id " +
                    " STRAIGHT_JOIN Province c ON a.ProvinceIndex = c.Id " +
                    " STRAIGHT_JOIN City d ON a.CityIndex = d.Id " +
                    " WHERE a.SystemUserIndex = " + SystemIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                FullName = rset.getString(1);
                DOB = rset.getString(2);
                Gender = rset.getString(3);
                Address1 = rset.getString(4);
                Address2 = rset.getString(5);
                Street1 = rset.getString(6);
                Street2 = rset.getString(7);
                CountryIndex = rset.getString(8);
                ProvinceIndex = rset.getString(9);
                CityIndex = rset.getString(10);
                PostalCode = rset.getString(11);
                Home = rset.getString(12);
                CellPhone = rset.getString(13);
                Email = rset.getString(14);
                DrivingLicense = rset.getString(15);
                Category = rset.getString(16);
                LicenseDate = rset.getString(17);
                ExpiryDate = rset.getString(18);
                AbstractIssueDate = rset.getString(19);
                AExpiryDate = rset.getString(20);
                CPRIssueDate = rset.getString(21);
                CPRExpiryDate = rset.getString(22);
                CarMake = rset.getString(23);
                CarModel = rset.getString(24);
                CarType = rset.getString(25);
                CarYear = rset.getString(26);
                Passenger = rset.getString(27);
                POBox = rset.getString(28);
                InsuranceExpiryDate = rset.getString(29);
                VCSExpiryDate = rset.getString(30);
                SystemUserIndex = rset.getString(31);
                FirstName = rset.getString(32);
                LastName = rset.getString(33);
                MiddleName = rset.getString(34);
            }
            stmt.close();
            rset.close();

            Gender = Gender.equals("M") ? "Male" : Gender.equals("F") ? "Female" : "Other";

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FullName ", FullName);
            Parser.SetField("DOB ", DOB);
            Parser.SetField("Gender ", Gender);
            Parser.SetField("Address1 ", Address1);
            Parser.SetField("Address2 ", Address2);
            Parser.SetField("Street1 ", Street1);
            Parser.SetField("Street2 ", Street2);
            Parser.SetField("CountryIndex ", CountryIndex);
            Parser.SetField("ProvinceIndex ", ProvinceIndex);
            Parser.SetField("CityIndex ", CityIndex);
            Parser.SetField("PostalCode ", PostalCode);
            Parser.SetField("Home ", Home);
            Parser.SetField("CellPhone ", CellPhone);
            Parser.SetField("Email ", Email);
            Parser.SetField("DrivingLicense ", DrivingLicense);
            Parser.SetField("Category ", Category);
            Parser.SetField("LicenseDate ", LicenseDate);
            Parser.SetField("ExpiryDate ", ExpiryDate);
            Parser.SetField("AbstractIssueDate ", AbstractIssueDate);
            Parser.SetField("AExpiryDate ", AExpiryDate);
            Parser.SetField("CPRIssueDate ", CPRIssueDate);
            Parser.SetField("CPRExpiryDate ", CPRExpiryDate);
            Parser.SetField("CarMake ", CarMake);
            Parser.SetField("CarModel ", CarModel);
            Parser.SetField("CarType ", CarType);
            Parser.SetField("CarYear ", CarYear);
            Parser.SetField("Passenger 	", Passenger);
            Parser.SetField("POBox ", POBox);
            Parser.SetField("InsuranceExpiryDate ", InsuranceExpiryDate);
            Parser.SetField("VCSExpiryDate ", VCSExpiryDate);
            Parser.SetField("SystemUserIndex", SystemUserIndex);
            Parser.SetField("FirstName", FirstName);
            Parser.SetField("LastName", LastName);
            Parser.SetField("MiddleName", MiddleName);

            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/ShowDriver.html");
        } catch (Exception Ex) {
            out.println("Message" + Ex.getMessage());
            // Supportive.DumException("CreateDriver", "Method -- driverUserExist", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "CreateDriver");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

    }

}
