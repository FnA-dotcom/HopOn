package HopOn;

/**
 * Created by Siddiqui on 9/12/2017.
 */

import Parsehtm.Parsehtm;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

@SuppressWarnings("Duplicates")

public class Login extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
    private static final String POST_PARAMS = "userName=Pankaj";
    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'T', '#', '3', 'B', '3', '$', 'T',
                    '$', '3', 'C', 'r', '3', 't', 'K', '3', 'Q'};
    private PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }

    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HandleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HandleRequest(request, response);
    }

    public void HandleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Connection conn = null;
        String Action = null;
        Supportive supp = new Supportive();
        String connect_string = "";

        ServletContext context = null;
        context = getServletContext();

        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Expires", "0"); // Proxies.
        try {
            if (request.getParameter("Action") == null) {
                Action = "Home";
                return;
            }
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
                System.out.println("Exception excp conn: " + excp.getMessage());
            }

            Action = request.getParameter("Action");

            if (Action.compareTo("Login") != 0) {
                boolean ValidSession = Supportive.checkSession(out, request);
                if (!ValidSession) {
                    out.flush();
                    out.close();
                    conn.close();
                    return;
                }
            }
            if (Action.compareTo("Login") == 0) {
                SignIn(request, response, out, conn, context);
            } else if (Action.compareTo("Logout") == 0) {
                Logout(request, response, out, conn, context);
            } else if (Action.compareTo("PasswordFirst") == 0) {
                PasswordFirst(request, response, out, conn, context);
            } else {
                out.println("<font size=\"3\" face=\"Calibri\">Under Development  " + Action + "</font>");
            }
            out.flush();
            out.close();

            conn.close();
        } catch (Exception e) {
            Supportive.DumException("Login", "HandleRequest", request, e, getServletContext());
            try {
                Cookie cookie = new Cookie("UserId", "");
                response.addCookie(cookie);

                String Message = "You have been logged out of the System.";

                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("Error", Message);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "index.html");
            } catch (Exception localException1) {
            }
        }
        out.flush();
        out.close();
    }

    private void PasswordFirst(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext context) {

        stmt = null;
        Query = "";
        String PasswordFirst = request.getParameter("xQar2f0").trim();

        try {
            HttpSession session = request.getSession(true);
            if (session.getAttribute("UserId") == null || session.getAttribute("UserId").equals("")) {
                out.println(" <center><table cellpadding=3 cellspacing=2><tr><td bgcolor=\"#FFFFFF\"> " +
                        " <font color=green face=arial><b>Your Session Has Been Expired</b></font> " +
                        " </td></tr></table> " +
                        " <p> " +
                        " <font face=arial size=+1><b><a href=/HopOn/index.html target=_top> Return to Login Page 1234" +
                        " </a></b></font>" +
                        " <br><font face=arial size=-2>(You will need to sign in again.)</font><br> " +
                        " </center> ");
                out.flush();
                out.close();
                session.removeAttribute("UserId");
                return;
            }
            String UserId = session.getAttribute("UserId").toString();
            String EncPass = Login.encrypt(PasswordFirst);

            Query = "UPDATE SystemUsers SET isPwdChanged = 1, Password = '" + EncPass + "' WHERE UserId = '" + UserId + "' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            UserId = Supportive.GetCookie("UserId", request);
            try {
                //logActivity(request,"LogOut",conn,UserId);
                WebLogOut(request, out, conn, UserId);
                session = request.getSession(false);
                session.removeAttribute("UserId");
                session.removeAttribute("CityIndex");
                session.removeAttribute("isAdmin");
                session.invalidate();

                if (context.getAttribute("ActiveSessions") != null) {
                    Hashtable ht = (Hashtable) context.getAttribute("ActiveSessions");

                    ht.remove(UserId);
                    context.setAttribute("ActiveSessions", ht);
                }

                String Message = "Your Password has been changed successfully!!. Please Re-Login.";

                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("Error", Message);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "index.html");
            } catch (Exception e) {
                try {
                    Supportive.DumException("Login", "Logout", request, e, this.getServletContext());
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("Error", e.getMessage());
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "index.html");
                } catch (Exception exception) {
                }
            }
        } catch (Exception Ex) {
            Supportive.DumException("Login", "First Pwd Change", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("Error", Ex.getMessage());
                Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "index.html");
            } catch (Exception localException1) {
            }
        }
    }

/*    private static final byte[] keyValue =
            new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't',
                    'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};*/

    private void SignIn(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext context) {
        Statement hstmt = null;
        ResultSet hrset = null;
        String Query = "", UserType = "";

        String UserId = request.getParameter("UserId");
        String Password = request.getParameter("Passwd");

        String passwordEnc = "";
        String passwordDec = "";
        try {
            if ((UserId == null) || (UserId.trim().length() < 1)) {
                throw new Exception("Enter Valid UserId.");
            }
            if ((Password == null) || (Password.trim() == null) || (Password.trim().length() == 0)) {
                throw new Exception("Enter Valid Password.");
            }

            passwordEnc = Login.encrypt(Password);

            int Found = 0;
            int Enabled = 0;
            int CityIndex = 0;
            int isAdmin = 0;
            int isPwdChanged = 0;
            String UserName = "";
            try {
                Query = " SELECT IFNULL(count(*),0),IFNULL(UserType,'-'),IFNULL(Enabled,0),IFNULL(CityIndex,0), " +
                        " IFNULL(UserName,'-'),IFNULL(isAdmin,0),IFNULL(isPwdChanged,0) " +
                        " FROM SystemUsers  WHERE upper(trim(userid))='" + UserId.toUpperCase() + "'  " +
                        " AND password='" + passwordEnc + "' ";
                System.out.println("Query " + Query);
                hstmt = conn.createStatement();
                hrset = hstmt.executeQuery(Query);
                if (hrset.next()) {
                    Found = hrset.getInt(1);
                    UserType = hrset.getString(2).trim();
                    Enabled = hrset.getInt(3);
                    CityIndex = hrset.getInt(4);
                    UserName = hrset.getString(5);
                    isAdmin = hrset.getInt(6);
                    isPwdChanged = hrset.getInt(7);
                }
                hrset.close();
                hstmt.close();
            } catch (Exception Ex) {
                Supportive.DumException("Login", "Credentials Error", request, Ex, getServletContext());
                throw new Exception("Invalid UserId/Password. \nPlease enter correct credentials!!");
            }

            if (Found < 1) {
                throw new Exception("Invalid UserId/Password !!!");
            }
            if (Enabled == 0) {
                throw new Exception("Your User ID has been blocked, kindly contact with System Administrator!!!");
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("UserId", UserId);
            session.setAttribute("CityIndex", String.valueOf(CityIndex));
            session.setAttribute("isAdmin", String.valueOf(isAdmin));

            Parsehtm Parser = new Parsehtm(request);
            if (isPwdChanged == 0) {
                Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "PasswordScreen.html");
            } else {
                Parser.SetField("EmployeeName", UserName);
                if (UserType.equals("A")) {
                    logActivity(request, "LogIn", conn, UserId, CityIndex);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "AdminMain.html");
                } else if (UserType.equals("P"))
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "ParentMain.html");
                else if (UserType.equals("D"))
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "DriverMain.html");
                else
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "index.html");
            }

        } catch (Exception e) {
            Supportive.DumException("Login", "Login", request, e, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("Error", e.getMessage());
                Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "index.html");
            } catch (Exception localException1) {
            }
        }
    }

    private void Logout(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext context) {

        String UserId = Supportive.GetCookie("UserId", request);
        try {
            //logActivity(request,"LogOut",conn,UserId);
            WebLogOut(request, out, conn, UserId);
            HttpSession session = request.getSession(false);
            session.removeAttribute("UserId");
            session.removeAttribute("CityIndex");
            session.removeAttribute("isAdmin");
            session.invalidate();

            if (context.getAttribute("ActiveSessions") != null) {
                Hashtable ht = (Hashtable) context.getAttribute("ActiveSessions");

                ht.remove(UserId);
                context.setAttribute("ActiveSessions", ht);
            }

            String Message = "You have been successfully logged out from the System.";

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("Error", Message);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "index.html");
        } catch (Exception e) {
            try {
                Supportive.DumException("Login", "Logout", request, e, this.getServletContext());
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("Error", e.getMessage());
                Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "index.html");
            } catch (Exception exception) {
            }
        }
    }

    public void logActivity(HttpServletRequest request, String Action, Connection conn, String UserId, int CityIndex) {
        String Query = null;
        try {
            String UserID = Supportive.GetCookie("UserId", request);
            String UserIP = request.getRemoteAddr();

            Query = " INSERT INTO admin_activity_log  (userid,action,entrydate,ip) VALUES (?,?,now(),?) ";

            PreparedStatement ps = conn.prepareStatement(Query);
            ps.setString(1, UserID);
            ps.setString(2, Action);
            ps.setString(3, UserIP);
            ps.execute();
            ps.close();

            //int CityIndex = CityIndex(conn, UserId);

            pStmt = conn.prepareStatement(
                    "INSERT INTO LoginTrail(UserId, UserType, IP, Status, CreatedDate,CityIndex) VALUES(?,?,?,0,now(),?)");

            pStmt.setString(1, UserId);
            pStmt.setString(2, "W");
            pStmt.setString(3, UserIP);
            pStmt.setInt(4, CityIndex);

            pStmt.executeUpdate();
            pStmt.close();
        } catch (Exception e) {
            Supportive.DumException("Login", "logActivity", request, e, getServletContext());
        }
    }

    private void WebLogOut(HttpServletRequest request, PrintWriter out, Connection conn, String UserId) {
        stmt = null;
        rset = null;
        Query = " ";

        try {
            Query = "DELETE FROM LoginTrail WHERE UserId='" + UserId + "' AND UserType='W' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

        } catch (Exception Ex) {
            out.println("Exception" + Ex.getMessage());
        }
    }
}











            /*String Url = "http://96.73.123.69:83/HopOn/HopOn.Login?Action=Logout";
            URL obj = new URL(Url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "");

            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(POST_PARAMS.getBytes());
            os.flush();
            os.close();
            // For POST only - END

            int responseCode = con.getResponseCode();
            System.out.println("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer res = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    res.append(inputLine);
                }
                in.close();

                // print result
                System.out.println("RESULT : " + res.toString());
            } else {
                System.out.println("POST request not worked");
            }*/