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
public class AddFamily extends HttpServlet {
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
        String Action;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();
        ServletContext context;
        context = this.getServletContext();
        String UserId;
        int CityIndex;
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
            Connection conn;
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
            //String SystemUserType = request.getParameter("SystemUserType");

/*            if (Action.equals("GetParent")) {
                GetParent(request, out, conn, context, UserId, CityIndex);
            } else */
/*            if (Action.equals("GetInfo")) {
                if (SystemUserType.equals("A"))
                    GetParent(request, out, conn, context, SystemUserType);
                else
                    GetInfo(request, out, conn, context, UserId, CityIndex, helper, SystemUserType);
            } else if (Action.trim().equals("SaveFamily")) {
                SaveRecords(request, out, conn, context, UserId, helper);
            } else {
                out.println("Under Development ... " + Action);
            }*/

            switch (Action) {
                case "GetParent":
                    GetParent(request, out, conn, context);
                    break;
                case "GetInfo":
                    GetInfo(request, out, conn, context, UserId, CityIndex, helper);
                    break;
                case "SaveFamily":
                    assert conn != null;
                    SaveRecords(request, out, conn, context, UserId, helper);
                    break;
                default:
                    out.println("Under Development ... " + Action);
                    break;
            }

            assert conn != null;
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

    private void GetParent(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";

        StringBuilder SelectParent = new StringBuilder();

        try {

            Query = "SELECT Id,UserName FROM SystemUsers WHERE Status=0 AND UserType='P' ORDER BY UserName ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                SelectParent.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("SelectParent", SelectParent.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/SelectParent.html");
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

    private void GetInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuilder Country = new StringBuilder();
        StringBuilder UserSelection = new StringBuilder();
/*        String SystemUserType = request.getParameter("SystemUserType");
        int SelectedParentIndex = 0;

        if (SystemUserType.equals("A")) {
            SelectedParentIndex = Integer.parseInt(request.getParameter("ParentList").trim());
            out.println("Admin " + SelectedParentIndex);
        } else {
            SelectedParentIndex = helper.UserIndex(request, UserId, conn, servletContext);
            out.println("Parent" + SelectedParentIndex);
        }*/
        int SystemIndex = helper.UserIndex(request, UserId, conn, servletContext);
        String UserType = helper.UserType(request, SystemIndex, conn, servletContext);

        try {
            if (UserType.equals("A")) {
                UserSelection.append("<div class=\"form-group\">");
                UserSelection.append("<label class=\"col-md-4 control-label\">Parent List</label>");
                UserSelection.append("<div class=\"col-md-8 col-sm-8 col-xs-12 \">");
                UserSelection.append("<select class=\"form-control m-b\" name=\"ParentList\" id=\"ParentList\"> ");
                UserSelection.append("<option value=\"\" selected disabled>Please select one..</option>");
                Query = "SELECT a.Id,a.UserName FROM SystemUsers a WHERE a.Status=0 AND a.UserType='P' AND a.Id " +
                        "NOT IN (SELECT x.SystemUserIndex FROM Parents x WHERE x.SystemUserIndex = a.Id) ORDER BY a.UserName ";
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
                UserSelection.append("<label class=\"col-md-4 control-label\">Parent Name</label>");
                UserSelection.append("<div class=\"col-md-8 col-sm-8 col-xs-12 \">");
                String ParentName = helper.SystemUserName(request, SystemIndex, conn, servletContext);
                UserSelection.append("<input type=\"text\" id=\"ParentName\" name=\"ParentName\" readonly value='" + ParentName + "' class=\"form-control\">");

                UserSelection.append("</div>");
                UserSelection.append("</div>");
            }
            boolean isUserExist = false;
            try {
                isUserExist = helper.isParentExist(request, SystemIndex, conn, servletContext);
                if (isUserExist) {
/*                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddFamily");
                    Parser.SetField("ActionID", "GetParent");
                    Parser.SetField("Message", "User Has already been Registered!!");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
                    return;*/
                    AddFamily add = new AddFamily();
                    add.parentUserExist(request, out, conn, servletContext, SystemIndex, helper);
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
                        Supportive.DumException("AddFamily", "Country Query -- GetInfo--001", request, var17, getServletContext());
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
                    Parser.SetField("UserSelection", UserSelection.toString());
                    Parser.SetField("ParentList", String.valueOf(SystemIndex));
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddFamily.html");
                }
            } catch (Exception ex) {
                Supportive.doLog(this.getServletContext(), "CreateDriver -- GetInitials", ex.getMessage(), ex);
                out.flush();
                out.close();
            }


        } catch (Exception Ex) {
            Supportive.DumException("AddFamily", "Second Method -- GetInfo--MAIN", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddFamily");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, UtilityHelper helper) {
        Query = "";
        cStmt = null;
        rset = null;

        int SelectedParentIndex = Integer.parseInt(request.getParameter("ParentList").trim());
        //PersonalInfo
        String FName = request.getParameter("FName").trim();
        String MName = request.getParameter("MName").trim();
        String LName = request.getParameter("LName").trim();
        String FullName = FName + " " + LName + " " + MName;
        String DOB = request.getParameter("DOB").trim();
        String Month = DOB.substring(0, 2);
        String Day = DOB.substring(3, 5);
        String Year = DOB.substring(6, 10);
        String DateOfBirth = Year + "-" + Month + "-" + Day + " 00:00:00";
        String MStatus = request.getParameter("MStatus").trim();
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

        //Emergency Details
        String EFName = request.getParameter("EFName").trim();
        String EMName = request.getParameter("EMName").trim();
        String ELName = request.getParameter("ELName").trim();
        String EFullName = EFName + " " + ELName + " " + EMName;
        String EDOB = request.getParameter("EDOB").trim();
        String EMonth = EDOB.substring(0, 2);
        String EDay = EDOB.substring(3, 5);
        String EYear = EDOB.substring(6, 10);
        String EDateOfBirth = EYear + "-" + EMonth + "-" + EDay + " 00:00:00";
        String Rship = request.getParameter("Rship").trim();
        String EContact = request.getParameter("EContact").trim();


        String CurrDate = helper.getCurrDate(request, conn);
        //int SystemUserIndex  = helper.UserIndex(request, UserId, conn, servletContext);

        Query = "{CALL SP_SAVE_ParentsInfo(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
//        Query = "{CALL SP_SAVE_ParentsInfo(?,?,?,?,?,?,?)}";
        try {
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, FullName);
            cStmt.setString(2, DateOfBirth);
            cStmt.setString(3, MStatus);
            cStmt.setString(4, Gender);
            cStmt.setString(5, Address1);
            cStmt.setString(6, Address2);
            cStmt.setString(7, StreetAddress1);
            cStmt.setString(8, StreetAddress2);
            cStmt.setInt(9, Country);
            cStmt.setInt(10, Province);
            cStmt.setInt(11, City);
            cStmt.setString(12, POBox);
            cStmt.setString(13, TelePhone);
            cStmt.setString(14, CellPhone);
            cStmt.setString(15, Email);
            cStmt.setString(16, EFullName);
            cStmt.setString(17, PostCode);
            cStmt.setString(18, EDateOfBirth);
            cStmt.setString(19, Rship);
            cStmt.setInt(20, 0);
            cStmt.setString(21, CurrDate);
            cStmt.setInt(22, SelectedParentIndex);
            cStmt.setString(23, EContact);
            cStmt.setString(24, UserId);
            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", "AddFamily");
            Parser.SetField("ActionID", "GetInfo");
            Parser.SetField("Message", "Parent Information Has been entered Successfully!!");
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
        } catch (Exception ex) {
            Supportive.doLog(this.getServletContext(), "AddFamily -- Record Insertion-02", ex.getMessage(), ex);
            out.flush();
            out.close();
        }

    }

    private void parentUserExist(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, int SystemIndex, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";
        String FullName = "";
        String DOB = "";
        String MaritialStatus = "";
        String Gender = "";
        String Address1 = "";
        String Address2 = "";
        String Street1 = "";
        String Street2 = "";
        String CountryName = "";
        String Province = "";
        String City = "";
        String POBox = "";
        String HomeNum = "";
        String CellPhone = "";
        String Email = "";
        String EmergencyName = "";
        String PostalCode = "";
        String EmergencyDOB = "";
        String EmergencyRelationShip = "";
        String EmergencyContact = "";
        String FirstName = "";
        String LastName = "";
        String MiddleName = "";
        String EFirstName = "";
        String ELastName = "";
        String EMiddleName = "";

        try {
            Query = "SELECT a.FullName, DATE_FORMAT(a.DOB,'%d-%b-%Y'), a.MaritialStatus, a.Gender, a.Address1, a.Address2, a.Street1, a.Street2, b.CountryName , c.ProvinceName , " +
                    "d.CityName, a.POBox, a.HomeNum, a.CellPhone, a.Email, a.EmergencyName, a.PostalCode, DATE_FORMAT(a.EmergencyDOB,'%d-%b-%Y') , a.EmergencyRelationShip, a.EmergencyContact," +
                    "SUBSTRING_INDEX(a.FullName, ' ', 1) AS FirstName , SUBSTRING_INDEX(SUBSTRING_INDEX(a.FullName, ' ', 2), ' ', -1) AS LastName, SUBSTRING_INDEX(SUBSTRING_INDEX(a.FullName, ' ', 3), ' ', -1) AS MiddleName," +
                    "SUBSTRING_INDEX(a.EmergencyName, ' ', 1) AS EFirstName , SUBSTRING_INDEX(SUBSTRING_INDEX(a.EmergencyName, ' ', 2), ' ', -1) AS ELastName, SUBSTRING_INDEX(SUBSTRING_INDEX(a.EmergencyName, ' ', 3), ' ', -1) AS EMiddleName" +
                    " FROM Parents a " +
                    " STRAIGHT_JOIN Country b ON a.CountryIndex = b.Id " +
                    " STRAIGHT_JOIN Province c ON a.ProvinceIndex = c.Id " +
                    " STRAIGHT_JOIN City d ON a.CityIndex = d.Id " +
                    "WHERE a.SystemUserIndex = " + SystemIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                FullName = rset.getString(1);
                DOB = rset.getString(2);
                MaritialStatus = rset.getString(3);
                Gender = rset.getString(4);
                Address1 = rset.getString(5);
                Address2 = rset.getString(6);
                Street1 = rset.getString(7);
                Street2 = rset.getString(8);
                CountryName = rset.getString(9);
                Province = rset.getString(10);
                City = rset.getString(11);
                POBox = rset.getString(12);
                HomeNum = rset.getString(13);
                CellPhone = rset.getString(14);
                Email = rset.getString(15);
                EmergencyName = rset.getString(16);
                PostalCode = rset.getString(17);
                EmergencyDOB = rset.getString(18);
                EmergencyRelationShip = rset.getString(19);
                EmergencyContact = rset.getString(20);
                FirstName = rset.getString(21);
                LastName = rset.getString(22);
                MiddleName = rset.getString(23);
                EFirstName = rset.getString(24);
                ELastName = rset.getString(25);
                EMiddleName = rset.getString(26);
            }
            stmt.close();
            rset.close();

            MaritialStatus = MaritialStatus.equals("M") ? "Married" : MaritialStatus.equals("S") ? "Single" : MaritialStatus.equals("W") ? "Widowed" : "Divorced";
            Gender = Gender.equals("M") ? "Male" : Gender.equals("F") ? "Female" : "Other";

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FullName", FullName);
            Parser.SetField("DOB", DOB);
            Parser.SetField("MaritialStatus", MaritialStatus);
            Parser.SetField("Gender", Gender);
            Parser.SetField("Address1", Address1);
            Parser.SetField("Address2", Address2);
            Parser.SetField("Street1", Street1);
            Parser.SetField("Street2", Street2);
            Parser.SetField("CountryName", CountryName);
            Parser.SetField("Province", Province);
            Parser.SetField("City", City);
            Parser.SetField("POBox", POBox);
            Parser.SetField("HomeNum", HomeNum);
            Parser.SetField("CellPhone", CellPhone);
            Parser.SetField("Email", Email);
            Parser.SetField("EmergencyName", EmergencyName);
            Parser.SetField("PostalCode", PostalCode);
            Parser.SetField("EmergencyDOB", EmergencyDOB);
            Parser.SetField("EmergencyRelationShip", EmergencyRelationShip);
            Parser.SetField("EmergencyContact", EmergencyContact);

            Parser.SetField("FirstName", FirstName);
            Parser.SetField("LastName", LastName);
            Parser.SetField("MiddleName", MiddleName);
            Parser.SetField("EFirstName", EFirstName);
            Parser.SetField("ELastName", ELastName);
            Parser.SetField("EMiddleName", EMiddleName);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/ShowFamily.html");
        } catch (Exception Ex) {
            Supportive.DumException("AddFamily", "First Method -- GetInfo--MAIN", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddFamily");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

    }
}
