package HopOn;

import Parsehtm.Parsehtm;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class CreateUsers extends HttpServlet {
    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{84, 35, 51, 66, 51, 36, 84, 36, 51, 67, 114, 51, 116, 75, 51, 81};
    private String ScreenNo = "29";

    public CreateUsers() {
    }

    private static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance("AES");
        c.init(1, key);
        byte[] encVal = c.doFinal(Data.trim().getBytes());
        String encryptedValue = (new BASE64Encoder()).encode(encVal);
        return encryptedValue;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, "AES");
        return key;
    }

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
        context = getServletContext();
        String UserId;
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
            } catch (Exception excp) {
                conn = null;
                out.println("Exception excp conn: " + excp.getMessage());
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

            if (Action.equals("GETINPUT")) {
                this.GetInput(request, out, conn, context, UserId, CityIndex, isAdmin);
            } else if (Action.compareTo("SaveUser") == 0) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if (Action.compareTo("Edit") == 0) {
                this.ShowEditInfo(request, out, conn, context, UserId);
            } else if (Action.compareTo("EditCreateUsers") == 0) {
                this.UpdateCreateUsers(request, out, conn, context, UserId);
            } else if (Action.compareTo("Delete") == 0) {
                this.DeleteInfo(request, out, conn, context, UserId);
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

    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int cityIndex, int isAdmin) {
        String Query = "";
        Statement stmt = null;
        ResultSet rset = null;

        StringBuilder City = new StringBuilder();
        StringBuilder UserType = new StringBuilder();
        StringBuilder DisplayTable = new StringBuilder();

        int ServerIndex = 0;
        //For Switching Image Directory Just Change below variable value
        //Live Server
        ServerIndex = 1;
        //Test Server
        //ServerIndex = 2;
        int srlno = 1;
        try {
            if (isAdmin == 1) {
                Query = "SELECT a.UserName,a.UserId,(CASE WHEN a.Status=0 THEN 'Active' ELSE 'Inactive' END),b.CityName,a.Id,a.UserType," +
                        " (CASE WHEN a.UserType='A' THEN 'Admin' " +
                        "WHEN a.UserType='D' THEN 'Driver' " +
                        "ELSE 'Parent' END) AS UserType " +
                        "FROM SystemUsers a " +
                        "STRAIGHT_JOIN City b on a.CityIndex=b.Id ";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    DisplayTable.append("<tr>");
                    DisplayTable.append("<td>" + (srlno++) + "</td>");
                    DisplayTable.append("<td>" + rset.getString(1) + "</td>");
                    DisplayTable.append("<td>" + rset.getString(2) + "</td>");
                    DisplayTable.append("<td>" + rset.getString(3) + "</td>");
                    DisplayTable.append("<td>" + rset.getString(4) + "</td>");
                    DisplayTable.append("<td>" + rset.getString(7) + "</td>");//UserType
                    DisplayTable.append("<td align=center><button class=\"btn-sm btn btn-info\"><a href=\"/" + (ServerIndex == 1 ? "HopOn" : "TAMAPPTesting") + "/HopOn.CreateUsers?ActionID=Edit&Indexptr=" + rset.getString(5) + "\" target=\"NewFrame1\"> <font color = \"FFFFFF\"> <i class=\"fa fa-edit\"></i> [Edit] </font></a></button></td>");

                    DisplayTable.append("<td align=center><button class=\"btn-sm btn btn-danger mylink\" value=" + rset.getString(5) + " target=NewFrame1> <font color = \"FFFFFF\"> <i class=\"fa fa-warning\"></i> [Delete] </font></button></td>");
                    DisplayTable.append("</tr>");
                }
            }

            rset.close();
            stmt.close();
        } catch (Exception var20) {
            var20.printStackTrace();
        }

        try {
            Query = "SELECT Id, CityName FROM City WHERE Status=0 ORDER BY Upper(CityName) ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            while (rset.next()) {
                City.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();

            Query = "SELECT ShortCode, UserType FROM UserType WHERE Status=0 ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            UserType.append("<option value='' selected disabled>Select User Type</option>");
            while (rset.next()) {
                if (rset.getString(1).equals("A"))
                    UserType.append("<option value=" + rset.getString(1) + " data-show=\".A\"> " + rset.getString(2) + "</option>");
                else
                    UserType.append("<option value=" + rset.getString(1) + " > " + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();

        } catch (Exception var19) {
            var19.printStackTrace();
        }
        try {

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("City", City.toString());
            Parser.SetField("UserTypes", UserType.toString());
            Parser.SetField("DisplayTable", DisplayTable.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/CreateUser.html");

        } catch (Exception e) {
            out.println("Error1" + e.getMessage());
        }
    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        String Query = "";
        PreparedStatement pStmt = null;
        Statement stmt = null;
        ResultSet rset = null;
        int isAdminCheck = 0;

        String FormName = "CreateUsers";
        String ActionID = "GETINPUT";

        String UserName = request.getParameter("UserName").trim();
        String UserType = request.getParameter("UserType").trim();
        String UserId1 = request.getParameter("UserId").trim();
        String Password = request.getParameter("Password").trim();
        int CityIndex = Integer.parseInt(request.getParameter("City"));
        if (UserType.equals("A"))
            isAdminCheck = Integer.parseInt(String.valueOf(request.getParameter("isadmincheck") == null ? 0 : 1));
        else
            isAdminCheck = 0;

        int Status = 0;
        String ImageSource = "null";
        int Enabled = 1;

        try {
            int Count = 0;
            Query = "SELECT COUNT(*) FROM SystemUsers WHERE UserId = '" + UserId1 + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                Count = rset.getInt(1);
            rset.close();
            stmt.close();

            if (Count > 0) {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", FormName);
                Parser.SetField("ActionID", ActionID);
                Parser.SetField("Message", "User ID Has already been Registered!!");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
                return;
            }

            String EncryptedPassword = this.GenerateEncryption(Password);
            pStmt = conn.prepareStatement(
                    "INSERT INTO SystemUsers (UserName,UserId,Password,Status,CreatedDate,ImageSource,Designation,Department,Enabled," +
                            "CityIndex,isAdmin,UserType) VALUES (?,?,?,?,now(),?,?,?,?,?,?,?)");
            pStmt.setString(1, UserName);
            pStmt.setString(2, UserId1);
            pStmt.setString(3, EncryptedPassword);
            pStmt.setInt(4, Status);
            pStmt.setString(5, ImageSource);
            pStmt.setInt(6, 0);
            pStmt.setInt(7, 0);
            pStmt.setInt(8, Enabled);
            pStmt.setInt(9, CityIndex);
            pStmt.setInt(10, isAdminCheck);
            pStmt.setString(11, UserType);

            pStmt.executeUpdate();
            pStmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Message", "User Has been created Successfully!!");
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");

/*            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("UserTypes", UserTypes.toString());
            Parser.SetField("City", City.toString());
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/CreateUser.html");*/
/*            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("UserTypes", UserTypes.toString());
            Parser.SetField("City", City.toString());
            Parser.SetField("FormName", FormName);
            Parser.SetField("ActionID", ActionID);
            Parser.SetField("Message","User Created Successfully!!");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");*/
            out.flush();
            out.close();
        } catch (Exception var27) {
            out.println(var27.getMessage());
        }

    }

    private String GenerateEncryption(String password) {
        String Enc = null;

        try {
            Enc = encrypt(password);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return Enc;
    }

    private void ShowEditInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        String Query = "";
        Statement stmt = null;
        ResultSet rset = null;
        String TechIndex = request.getParameter("Indexptr").trim();
        int _TIndex = Integer.parseInt(TechIndex);
        String Username = "";
        String TUserId = "";
        String Password = "";
        String Designation = "";
        String DepartmentId = "";
        String UserType = "";
        int CityIndex = 0;
        int _Status = 0;
        StringBuilder UserTypes = new StringBuilder();
        StringBuffer City = new StringBuffer();
        StringBuilder Status = new StringBuilder();

        try {
            Query = "Select Username,UserId,Password,Designation,Department,CityIndex,Status,UserType from SystemUsers where Id = " + _TIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                Username = rset.getString(1).trim();
                TUserId = rset.getString(2);
                Password = rset.getString(3);
//                Designation = rset.getString(4);
//                DepartmentId = rset.getString(5);
                CityIndex = rset.getInt(6);
                _Status = rset.getInt(7);
                UserType = rset.getString(8).trim();
            }

            rset.close();
            stmt.close();

            Password = Login.decrypt(Password);

            Query = "SELECT ShortCode,UserType FROM UserType";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            while (rset.next()) {
                if (rset.getString(1).equals(UserType)) {
                    UserTypes.append("<option value=" + rset.getString(1) + " Selected>" + rset.getString(2) + "</option>");
                } else {
                    UserTypes.append("<option value=" + rset.getString(1) + ">" + rset.getString(2));
                }
            }

            rset.close();
            stmt.close();
            Query = "SELECT Id, CityName FROM City WHERE Status = 0";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            while (rset.next()) {
                if (rset.getInt(1) == CityIndex) {
                    City.append("<option value=" + rset.getInt(1) + " Selected>" + rset.getString(2) + "</option>");
                } else {
                    City.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2));
                }
            }

            rset.close();
            stmt.close();


            Status.append("<div class=form-group>");
            Status.append("<label class='col-sm-3 control-label'>Status</label>");
            Status.append("<div class='col-sm-5'>");
            Status.append("<select class='form-control m-b' name=Status id=Status>");
            if (_Status == 1) {
                Status.append(" <option value=0> Active</option>");
                Status.append("<option value=1 Selected> Inactive</option>");
            } else {
                Status.append(" <option value=0 Selected> Active</option>");
                Status.append("<option value=1 > Inactive</option>");
            }

            Status.append(" </select>");
            Status.append("</div>");
            Status.append("</div>");


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("City", City.toString());
            Parser.SetField("UserTypes", UserTypes.toString());
            Parser.SetField("Username", Username);
            Parser.SetField("TUserId", String.valueOf(TUserId));
            Parser.SetField("Password", Password);
            Parser.SetField("_Index", String.valueOf(_TIndex));
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "EditForms/EditCreateUsers.html");
        } catch (Exception var21) {
            var21.printStackTrace();
        }

    }

    private void UpdateCreateUsers(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext context, String userId) {
        String Query = "";
        Statement stmt = null;
        ResultSet rset = null;
        String Indexptr = request.getParameter("Indexptr").trim();
        int _Index = Integer.parseInt(Indexptr);
        String UserName = request.getParameter("UserName").trim();
        String UserId1 = request.getParameter("UserId").trim();
        String Passwords = request.getParameter("Password").trim();
        String Designation = request.getParameter("Designation").trim();
        String Department = request.getParameter("Department").trim();
        String City = request.getParameter("City").trim();

        try {
            String EncryptedPassword = this.GenerateEncryption(Passwords);
            Query = "UPDATE SystemUsers SET UserName='" + UserName + "',UserId='" + UserId1 + "',Password='" + EncryptedPassword + "', " +
                    "Designation='" + Designation + "',Department='" + Department + "',CityIndex='" + City + "' WHERE Id=" + _Index;
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            Parsehtm parser = new Parsehtm(request);
            parser.SetField("FormName", "CreateUsers");
            parser.SetField("ActionID", "GETINPUT");
            parser.GenerateHtml(out, Supportive.GetHtmlPath(context) + "Exceptions/Success.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            var19.printStackTrace();
        }

    }

    private void DeleteInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        String Indexptr = request.getParameter("Indexptr").trim();
        int _Index = Integer.parseInt(Indexptr);
        String Query = "";
        Statement stmt = null;
        ResultSet rset = null;
        try {

            Query = "SELECT * FROM SystemUsers WHERE Id = " + _Index;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                PreparedStatement pStmt = conn.prepareStatement(
                        "INSERT INTO SystemUsersHistory (UserName, UserId, Password, Status, CreatedDate, ImageSource, " +
                                "Enabled, isAdmin, CityIndex, UserType) " +
                                "VALUES (?,?,?,?,?,?,?,?,?,?) ");
                pStmt.setString(1, rset.getString("UserName"));
                pStmt.setString(2, rset.getString("UserId"));
                pStmt.setString(3, rset.getString("Password"));
                pStmt.setInt(4, rset.getInt("Status"));
                pStmt.setString(5, rset.getString("CreatedDate"));
                pStmt.setString(6, rset.getString("ImageSource"));
                pStmt.setInt(7, rset.getInt("Enabled"));
                pStmt.setInt(8, rset.getInt("isAdmin"));
                pStmt.setInt(9, rset.getInt("CityIndex"));
                pStmt.setString(10, rset.getString("UserType"));

                pStmt.executeUpdate();
                pStmt.close();
            }
            rset.close();
            stmt.close();

            Query = "Delete from SystemUsers WHERE Id=" + _Index;
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            Parsehtm parser = new Parsehtm(request);
            parser.SetField("FormName", "CreateUsers");
            parser.SetField("ActionID", "GETINPUT");
            parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
            out.flush();
            out.close();
        } catch (Exception var11) {
            out.println("Error1" + var11.getMessage());
        }

    }

/*    private void UnAuthorize(AuthorizeUser authent, PrintWriter out, Connection conn) {
        out.println(AuthorizeUser.ReturnedErrMsg);
        out.flush();
        out.close();
        try {
            conn.close();
        } catch (Exception ex) {
        }
    }*/
}
