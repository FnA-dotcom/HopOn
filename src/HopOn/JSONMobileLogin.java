package HopOn;

import Parsehtm.Parsehtm;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")

public class JSONMobileLogin extends HttpServlet {
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";
    private PreparedStatement pStmt = null;
    private String LogString = null;
    private Connection conn = null;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Supportive supp = new Supportive();
//        String connect_string = supp.GetConnectString();

        response.setContentType("application/json;charset=UTF-8");
        //For Printing
        PrintWriter out = new PrintWriter(response.getOutputStream());

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

        try {
            String RequestName = request.getParameter("RequestName").trim();
            switch (RequestName) {
                case "Test":
                    BFunction(response, request);
                    break;
                case "LoginHelper":
                    LoginHelper(response, request, conn);
                    break;
                case "MobileLogOut":
                    MobileLogOut(response, request, conn);
                    break;
                case "MobileOffline":
                    MobileOffline(response, request, conn);
                    break;
                case "MobileOnline":
                    MobileOnline(response, request, conn);
                    break;
                default:
                    out.println("Under Development");
                    break;
            }
        } catch (Exception Ex) {
            out.println("Error in Main " + Ex.getMessage());
        }
        out.close();
        out.flush();
    }

    private void BFunction(HttpServletResponse response, HttpServletRequest req) throws IOException {

        String myVar = req.getParameter("AA");
        ServletOutputStream out = response.getOutputStream();

        JSONArray jsonArray = new JSONArray();
        for (int i = 1; i < 2; i++) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("srcOfPhoto", "Element 1");
            jsonObj.put("username", "name" + i);
            jsonObj.put("userid", "userid" + i);

            jsonArray.add(jsonObj);
        }
        out.print("ELEMENTS ARE " + jsonArray.toJSONString());
        out.print("myVar " + myVar);
    }

    private String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec("encryptionIntVec".getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec("aesEncryptionKey".getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(2, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private void LoginHelper(HttpServletResponse response, HttpServletRequest request, Connection conn) throws IOException {
        stmt = null;
        rset = null;
        Query = "";
        int found = 0;
        int LoginCount = 0;
        String appVersion = "";

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();
        //For Printing JSON Objects
        ServletOutputStream out = response.getOutputStream();

        String value = request.getParameter("value");
        String[] splitValues = decrypt(value).split("'");

        String UserIP = request.getRemoteAddr();
/*        String UserId = request.getParameter("UserId").trim();
        UserId = UserId.replace("-", "");
        String Password = request.getParameter("Password");
        String MobileAppVersion = request.getParameter("version");*/

        String UserName = "";
        String passwordEnc = "";
        String passwordDec = "";
        //Status 0 means active
        //Status 1 measns inactive
        try {
            passwordEnc = Login.encrypt(splitValues[1]);
            passwordDec = Login.decrypt(passwordEnc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Query = " Select count(*),UserName from MobileUsers where upper(trim(UserId))='" + splitValues[0].trim() + "' " +
                " and Password='" + passwordEnc + "' and Status=0 ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                found = rset.getInt(1);
                UserName = rset.getString(2);
            }
            rset.close();
            stmt.close();

            if (found > 0) {
                Query = "SELECT AppVersion FROM AppVersion";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next())
                    appVersion = rset.getString(1).trim();
                rset.close();
                stmt.close();
                if (splitValues[2].compareToIgnoreCase(appVersion) != 0) {
                    Supportive.doLogMethodMessage(this.getServletContext(), "Login Method ", "Version Exception");
                    jsonObj.put("Result", "false");
                    jsonObj.put("Message", "You are using an outdated Mobile App. Current Version is  " + appVersion + " ");
                    jsonArray.add(jsonObj);
                    out.print(jsonArray.toJSONString());
                    return;
                } else {
                    jsonObj.put("Result", "true");
                    jsonObj.put("Message", UserName);

                    TechnicianActivity(conn, splitValues[0], "LogIn");
                    try {
                        Query = "SELECT  count(*) FROM LoginTrail WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + splitValues[0] + "')) AND UserType='M'";
                        stmt = conn.createStatement();
                        rset = stmt.executeQuery(Query);
                        if (rset.next())
                            LoginCount = rset.getInt(1);
                        rset.close();
                        stmt.close();

                        if (LoginCount == 0) {
                            pStmt = conn.prepareStatement(
                                    "INSERT INTO LoginTrail(UserId, UserType, IP, Status, CreatedDate) VALUES(?,?,?,0,now())");

                            pStmt.setString(1, splitValues[0]);
                            pStmt.setString(2, "M");
                            pStmt.setString(3, UserIP);

                            pStmt.executeUpdate();
                            pStmt.close();
                        } else {
                            Query = "UPDATE  LoginTrail SET IP = '" + UserIP + "',CreatedDate=NOW() " +
                                    " WHERE ltrim(rtrim(UserId)) = '" + splitValues[0] + "' ";
                            stmt = conn.createStatement();
                            stmt.executeUpdate(Query);
                            stmt.close();
                        }
                    } catch (Exception e) {
                        Supportive.doLog(this.getServletContext(), "Login Trail First Exception", e.getMessage(), e);
                        jsonObj.put("Message", "Login Trail First Exception!! ");
                        jsonArray.add(jsonObj);
                        out.print(jsonArray.toJSONString());
                        return;
                    }
                    jsonArray.add(jsonObj);
                    out.print(jsonArray.toJSONString());
                }
            } else {
                Supportive.doLogMethodMessage(this.getServletContext(), "Login Method - Credential Error", "InCorrect Credentials. Please Enter Correct Credentials!!");
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "InCorrect Credentials. Please Enter Correct Credentials!! ");
                jsonArray.add(jsonObj);
                out.print(jsonArray.toJSONString());
                return;
            }
        } catch (Exception e) {
            Supportive.doLogMethodMessage(this.getServletContext(), "Login Method - User Not Exist", "Exception :  Invalid UserId or password");
            jsonObj.put("Result", "false");
            jsonObj.put("Message", "Invalid UserId or password " + e.getMessage() + " ");
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());
            return;
        }
    }

    private void MobileLogOut(HttpServletResponse response, HttpServletRequest request, Connection conn) throws IOException {
        stmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();
        //For Printing JSON Objects
        ServletOutputStream out = response.getOutputStream();
        try {
            Query = "DELETE FROM LoginTrail WHERE UserId='" + UserId + "' AND UserType='M' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());

            //Trail Activity
            TechnicianActivity(conn, UserId, "LogOut");
            return;

        } catch (Exception Ex) {
            Supportive.doLog(this.getServletContext(), "Mobile Log Out ", Ex.getMessage(), Ex);
            jsonObj.put("Result", "false");
            jsonObj.put("Message", Ex.getMessage());
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());
            return;
        }
    }

    private void MobileOffline(HttpServletResponse response, HttpServletRequest request, Connection conn) throws IOException {
        stmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();
        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();
        //For Printing JSON Objects
        ServletOutputStream out = response.getOutputStream();
        try {
            Query = "DELETE FROM LoginTrail WHERE UserId='" + UserId + "' AND UserType='M' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());

            //Trail Activity
            TechnicianActivity(conn, UserId, "Offline");
            out.close();
            out.flush();
            return;

        } catch (Exception Ex) {
            Supportive.doLog(this.getServletContext(), "Mobile Offline ", Ex.getMessage(), Ex);
            jsonObj.put("Result", "false");
            jsonObj.put("Message", Ex.getMessage());
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());
            return;
        }
    }

    private void MobileOnline(HttpServletResponse response, HttpServletRequest request, Connection conn) throws IOException {
        pStmt = null;
        String UserIP = request.getRemoteAddr();
        String UserId = request.getParameter("UserId").trim();
        int LoginCount = 0;
        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();
        //For Printing JSON Objects
        ServletOutputStream out = response.getOutputStream();
        try {
            TechnicianActivity(conn, UserId, "OnlineAgain");

            Query = "SELECT  count(*) FROM LoginTrail WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + UserId + "')) AND UserType='M'";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                LoginCount = rset.getInt(1);
            rset.close();
            stmt.close();

            if (LoginCount == 0) {
                pStmt = conn.prepareStatement(
                        "INSERT INTO LoginTrail(UserId, UserType, IP, Status, CreatedDate) VALUES(?,?,?,0,now())");

                pStmt.setString(1, UserId.trim());
                pStmt.setString(2, "M");
                pStmt.setString(3, UserIP);

                pStmt.executeUpdate();
                pStmt.close();
            } else {
                Query = "UPDATE  LoginTrail SET IP = '" + UserIP + "',CreatedDate=NOW() " +
                        " WHERE ltrim(rtrim(UserId)) = '" + UserId + "' ";
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            }
            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());
            return;
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Mobile Online ", e.getMessage(), e);
            jsonObj.put("Result", "false");
            jsonObj.put("Message", e.getMessage());
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());
            return;
        }
    }


    private String TechnicianActivity(Connection conn, String UserId, String LoggedFlag) {
        pStmt = null;
        Query = "";
        String Message = "";
        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO DriverLoginTrail(DriverId,LoggedTime, Status, CreatedDate,LoginFlag) VALUES(?,NOW(),0,now(),?)");

            pStmt.setString(1, UserId.trim());
            pStmt.setString(2, LoggedFlag.trim());
            pStmt.executeUpdate();
            pStmt.close();

            Message = "Success";
        } catch (Exception Ex) {
            Message = Ex.getMessage();
        }
        return Message;
    }
}