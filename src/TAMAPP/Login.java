package TAMAPP;

/**
 * Created by Siddiqui on 9/12/2017.
 */

import Parsehtm.Parsehtm;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.sql.*;
import java.util.Hashtable;

@SuppressWarnings("Duplicates")

public class Login extends HttpServlet {
    private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
    private PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

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
        String connect_string = supp.GetConnectString();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(connect_string);
        } catch (Exception excp) {
            conn = null;
            System.out.println("Exception excp conn: " + excp.getMessage());
        }

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

    private void SignIn(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext context) {
        Statement hstmt = null;
        ResultSet hrset = null;
        String Query = "";

        String UserId = request.getParameter("UserId");
        String Password = request.getParameter("Passwd");
        try {
            if ((UserId == null) || (UserId.trim().length() < 1)) {
                throw new Exception("Enter Valid UserId.");
            }
            if ((Password == null) || (Password.trim() == null) || (Password.trim().length() == 0)) {
                throw new Exception("Enter Valid Password.");
            }
            int Found = 0;
            //passwordEnc = Login.encrypt(Password);
            //passwordDec = Login.decrypt(passwordEnc);
//			out.println(passwordEnc);
            Query = " select count(*) from SystemUsers  where upper(trim(userid))='" + UserId.toUpperCase() + "'  " +
                    " and BINARY password='" + Password + "' ";
            hstmt = conn.createStatement();
            hrset = hstmt.executeQuery(Query);
            if (hrset.next()) {
                Found = hrset.getInt(1);
            }
            hrset.close();
            hstmt.close();
            if (Found < 1) {
                throw new Exception("Invalid UserId/Password !!!");
            }

            int Enabled = 0;
            int CityIndex = 0;
            int isAdmin = 0;
            String UserName = "";
            String Designation = "";
            String CityName = "";
            Query = " SELECT a.UserName,c.Designation,b.Department,a.Enabled,a.CityIndex,a.isAdmin,d.CityName " +
                    " FROM SystemUsers a " +
                    " STRAIGHT_JOIN Department b ON a.Department = b.Id " +
                    " STRAIGHT_JOIN Designation c ON a.Designation = c.Id " +
                    " STRAIGHT_JOIN City d ON a.CityIndex = d.Id" +
                    " WHERE upper(trim(a.UserId))='" + UserId.toUpperCase().trim() + "' " +
                    " AND a.Password='" + Password + "' ";
            hstmt = conn.createStatement();
            hrset = hstmt.executeQuery(Query);
            if (hrset.next()) {
                UserName = hrset.getString(1);
                Designation = hrset.getString(3);
                Enabled = hrset.getInt(4);
                CityIndex = hrset.getInt(5);
                isAdmin = hrset.getInt(6);
                CityName = hrset.getString(7);
            }
            hrset.close();
            hstmt.close();
            if (Enabled == 0) {
                throw new Exception("Your User ID has been blocked, kindly contact with System Administrator!!!");
            }
            logActivity(request, "LogIn", conn, UserId);

            Cookie uid = new Cookie("UserId", UserId.trim());
            response.addCookie(uid);

            try {
                conn.close();
            } catch (Exception exception) {
            }
            if (context.getAttribute("ActiveSessions") == null) {
                // create new Session
                javax.servlet.http.HttpSession session = request.getSession(true);

                //out.print("ActiveSessions is NULL.<br>");
                Hashtable ht = new Hashtable();
                ht.put(UserId, session);
                context.setAttribute("ActiveSessions", ht);
            } else if (context.getAttribute("ActiveSessions") != null) {
                //				out.print("Modified ActiveSessions is NOT NULL.<br>" +
                //				context.getAttribute("ActiveSessions").getClass().getName());

                Hashtable ht = (Hashtable) context.getAttribute("ActiveSessions");

                if (ht.get(UserId) != null)  // single login allowed
                {
                    javax.servlet.http.HttpSession session = (javax.servlet.http.HttpSession) ht.get(UserId);
                    //session.invalidate();

                    ht.remove(UserId);
                    context.setAttribute("ActiveSessions", ht);
                }

                // create new Session
                javax.servlet.http.HttpSession session = request.getSession(true);
                ht.put(UserId, session);
                context.setAttribute("ActiveSessions", ht);
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("UserId", UserId);
            session.setAttribute("CityIndex", String.valueOf(CityIndex));
            session.setAttribute("isAdmin", String.valueOf(isAdmin));

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("EmployeeName", UserName);
            Parser.SetField("Designation", Designation);
            Parser.SetField("CityName", CityName);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath("", context) + "Welcome.html");
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

    public void logActivity(HttpServletRequest request, String Action, Connection conn, String UserId) {
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

            pStmt = conn.prepareStatement(
                    "INSERT INTO LoginTrail(UserId, UserType, IP, Status, CreatedDate) VALUES(?,?,?,0,now())");

            pStmt.setString(1, UserId);
            pStmt.setString(2, "W");
            pStmt.setString(3, UserIP);

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

    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'T', 'h', 'e', 'B', 'e', 's', 't',
                    'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y'};
//    public static String encrypt(String Data) throws Exception {
//        Key key = generateKey();
//        Cipher c = Cipher.getInstance(ALGO);
//        c.init(Cipher.ENCRYPT_MODE, key);
//        byte[] encVal = c.doFinal(Data.getBytes());
//        String encryptedValue = new BASE64Encoder().encode(encVal);
//        return encryptedValue;
//    }

    //    public static String decrypt(String encryptedData) throws Exception {
//        Key key = generateKey();
//        Cipher c = Cipher.getInstance(ALGO);
//        c.init(Cipher.DECRYPT_MODE, key);
//        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
//        byte[] decValue = c.doFinal(decordedValue);
//        String decryptedValue = new String(decValue);
//        return decryptedValue;
//    }
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }
}
