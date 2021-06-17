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
import java.sql.*;

@SuppressWarnings("Duplicates")
public class MobileUsers extends HttpServlet {
    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[]{84, 35, 51, 66, 51, 36, 84, 36, 51, 67, 114, 51, 116, 75, 51, 81};
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;
    CallableStatement cStmt;

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
//        String connect_string = supp.GetConnectString();
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

            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            Action = request.getParameter("ActionID");
            UtilityHelper helper = new UtilityHelper();

            if (Action.equals("GetInfo")) {
                GetInput(request, out, conn, context);
            } else if (Action.equals("getDetails")) {
                getDetails(request, out, conn, context, helper);
            } else if (Action.compareTo("SaveUser") == 0) {
                SaveRecords(request, out, conn, context, helper, UserId);
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

    private void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";

        StringBuilder SelectDriver = new StringBuilder();

        try {

            Query = "SELECT Id,UserName,UserType FROM SystemUsers WHERE Status=0 AND UserType IN ('D','P') ORDER BY UserName ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                if (rset.getString(3).equals("D"))
                    SelectDriver.append("<option value=" + rset.getInt(1) + "_" + rset.getString(3) + ">[Driver] - " + rset.getString(2) + "</option>");
                else
                    SelectDriver.append("<option value=" + rset.getInt(1) + "_" + rset.getString(3) + ">[Parent] - " + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("SelectDriver", SelectDriver.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/MobileUserInput.html");
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

    private void getDetails(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, UtilityHelper helper) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuilder CityList;
        String DriverName = "";
        String SelectedDriverIndex = request.getParameter("DriverList").trim();
        String[] SelectedIndex = SelectedDriverIndex.split("_");
        boolean isUserExist = false;
        try {

            isUserExist = helper.isMobileUserExist(request, Integer.parseInt(SelectedIndex[0]), conn, servletContext);

            if (isUserExist) {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "MobileUsers");
                Parser.SetField("ActionID", "GetInfo");
                Parser.SetField("Message", "User Has already been Registered!!");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Message.html");
                return;
            }

            CityList = helper.CityList(request, conn, servletContext);
            int DriverIndex = helper.DriverIndex(request, Integer.parseInt(SelectedIndex[0]), conn, servletContext);
            DriverName = helper.DriverName(request, DriverIndex, conn, servletContext);

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("CityList", CityList.toString());
            Parser.SetField("DriverName", DriverName);
            Parser.SetField("SelectedDriverIndex", String.valueOf(SelectedDriverIndex));
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/CreateMobileUser.html");
        } catch (Exception Ex) {
            Supportive.DumException("MobileUsers", "Second Method -- getDetails ", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "MobileUsers");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }

    private void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, UtilityHelper helper, String systemUserId) {
        Query = "";
        cStmt = null;
        rset = null;
        String Result;

        String SelectedDriverIndex = request.getParameter("SelectedDriverIndex").trim();
        String[] SelectedIndex = SelectedDriverIndex.split("_");
        String UserName = request.getParameter("UserName").trim();
        String UserId = request.getParameter("UserId").trim();
        String Password = request.getParameter("Password").trim();
        String Phone = request.getParameter("Phone").trim();

        try {
            String CurrDate = helper.getCurrDate(request, conn);
            //int DriverIndex = helper.DriverIndex(request, Integer.parseInt(SelectedIndex[0]), conn, servletContext);

            String EncryptedPassword = this.GenerateEncryption(Password);

            Result = helper.saveMobileUser(request, Integer.parseInt(SelectedIndex[0]), UserName, UserId, EncryptedPassword, Phone, 0, CurrDate,
                    systemUserId, SelectedIndex[1], conn, servletContext);

            if (Result.equals("Success")) {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "MobileUsers");
                Parser.SetField("ActionID", "GetInfo");
                Parser.SetField("Message", "User Has Been Created Successfully!!");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
            } else {
                Supportive.doLogMethodMessage(this.getServletContext(), "Save Records in Mobile User (WEB) ", "Unable to Save Records in Mobile Users [WEB]");
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "MobileUsers");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            }

        } catch (Exception Ex) {
            Supportive.DumException("MobileUsers", "Third Method -- SaveRecords ", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "MobileUsers");
                Parser.SetField("ActionID", "GetInfo");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
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
}
