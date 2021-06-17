package TAMAPP;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AuthorizeUser extends Object {
    Connection conn = null;
    String UserId = "", Passwd = "";
    String Blue_Bullet = "/images/bullet_p.gif";
    String Yellow_Bullet = "/images/bullet_d.gif";
    static String ReturnedErrMsg = "";

    public AuthorizeUser(Connection Conn) {
        conn = Conn;
    }

    public boolean CheckPasswordRights(String UserId, String Passwd) {
        Statement stmt = null;
        ResultSet rset = null;
        int uStatus = 0;
        String Query = "", uPasswd = "", ReturnedErrMsg = "";

        UserId = UserId.toUpperCase();
        if ((UserId.length() == 0) || (Passwd.length() == 0)) {
            ReturnedErrMsg = "<body>" +
                    "<font size=+2 FACE=\"Haettenschweiler\" color=\"#FF0033\">Falcon-I System - Users Authentication Module</font><br>\n" +
                    "<br><img src=\"" + Blue_Bullet + "\" width=31 height=14>" +
                    "UserId or Password field is empty..........<br>" +
                    "<form action=\"/TAMAPP/index.html\" method=\"Get\" target=\"_top\">\n" +
                    "<input type=submit name=Back Value=\"  Back  \">\n" +
                    "</form>" +
                    "</body></html>";
            return false;
        }
        Query = "Select ltrim(rtrim(Password)), Status from Falcon.SystemUsers Where ltrim(rtrim(upper(UserId)))=ltrim(rtrim(Upper('" + UserId + "'))) ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                uPasswd = rset.getString(1);
                uStatus = rset.getInt(2);
                if (uPasswd.compareTo(Passwd) != 0) {
                    ReturnedErrMsg = "<body>" +
                            "<font size=+2 FACE=\"Haettenschweiler\" color=\"#FF0033\">Falcon-I System - Users Authentication Module</font><br>\n" +
                            "<br><img src=\"" + UserId + "," + Passwd + "\" width=31 height=14>" +
                            "Invalid UserId or Password..........Logon denied<br>" +
                            "<form action=\"/TAMAPP/index.html\" target=\"_top\">\n" +
                            "<input type=submit name=Back Value=\"  Back  \">\n" +
                            "</form>" +
                            "</body></html>";
                    rset.close();
                    stmt.close();
                    return false;
                }
                if (uStatus != 0) {
                    ReturnedErrMsg = "<body>" +
                            "<font size=+2 FACE=\"Haettenschweiler\" color=\"#FF0033\">Falcon-I System - Users Authentication Module</font><br>\n" +
                            "<br><img src=\"" + UserId + "," + Passwd + "\" width=31 height=14>" +
                            "Blocked Users..........Logon denied<br>" +
                            "<form action=\"/TAMAPP/index.html\" target=\"_top\">\n" +
                            "<input type=submit name=Back Value=\"  Back  \">\n" +
                            "</form>" +
                            "</body></html>";
                    rset.close();
                    stmt.close();
                    return false;
                }
            } else {
                ReturnedErrMsg = "<body>" +
                        "<font size=+2 FACE=\"Haettenschweiler\" color=\"#FF0033\">Falcon-I System - Users Authentication Module</font><br>\n" +
                        "<br><img src=\"" + Yellow_Bullet + "\" width=31 height=14>User Id not found!!! \n" +
                        "Please login with Correct UserId and Password.(1)\n" +
                        "<b>" + UserId + "</b> \n" +
                        "<form action=\"/TAMAPP/index.html\" target=\"_top\">\n" +
                        "<input type=submit name=Back Value=\"  Back  \">\n" +
                        "</form>" +
                        "</body></html>";
                rset.close();
                stmt.close();
                return false;
            }
            rset.close();
            stmt.close();
        } catch (Exception e2) {
            ReturnedErrMsg = "<body>" +
                    "<font size=+2 FACE=\"Haettenschweiler\" color=\"#FF0033\">Falcon-I System - Users Authentication Module</font><br>\n" +
                    "<br><img src=\"" + Yellow_Bullet + "\" width=31 height=14>System Error e-0001: " + e2.getMessage() + " \n" +
                    "<form action=\"/TAMAPP/index.html\" target=\"_top\">\n" +
                    "<input type=submit name=Back Value=\"  Back  \">\n" +
                    "</form>" +
                    "</body></html>";
            try {
                rset.close();
                stmt.close();
            } catch (Exception e) {
            }
            return false;
        }
        return true;
    }

    public boolean AuthorizeScreen(String UserId, String ScreenNo) {
        Statement stmt = null;
        ResultSet rset = null;

        boolean RightStatus = false;
        String Query = "";

        Query = "Select count(*) From Falcon.UserRights Where ScreenNo=" + ScreenNo + " And EmployeeIndex In (Select Id \n" +
                "From Falcon.SystemUsers Where ltrim(rtrim(Upper(UserId)))=ltrim(rtrim(upper('" + UserId + "'))) )";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (!rset.next()) {
                ReturnedErrMsg = "<body>" +
                        "<font size=+2 FACE=\"Haettenschweiler\" color=\"#FF0033\">Falcon-I System</font><br>\n" +
                        "<br><img src=\"" + Blue_Bullet + "\" width=31 height=14>" +
                        "User Not found.............!!!! \n" +
                        "<form action=\"../TAMAPP/index.html\" method=\"Get\" target=\"_top\">\n" +
                        "<input type=submit name=Back Value=\"  Back  \">\n" +
                        "</form>" +
                        "</body></html>";
                rset.close();
                stmt.close();
                return false;
            }
            if (rset.getInt(1) > 0) {
                rset.close();
                stmt.close();
                return true;
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            ReturnedErrMsg = "<body>" +
                    "<font size=+2 FACE=\"Haettenschweiler\" color=\"#FF0033\">Falcon-I System</font><br>\n" +
                    "<br><img src=\"" + Blue_Bullet + "\" width=31 height=14>" +
                    "Error e-0001: " + e.getMessage() + " \n" +
                    "<form action=\"../TAMAPP/index.html\" method=\"Get\" target=\"_top\">\n" +
                    "<input type=submit name=Back Value=\"  Back  \">\n" +
                    "</form>" +
                    "</body></html>";
            return false;
        }
        ReturnedErrMsg = "<body>" +
                "<font size=+2 FACE=\"Haettenschweiler\" color=\"#FF0033\">Falcon-I System</font><br>\n" +
                "<br><img src=\"" + Blue_Bullet + "\" width=31 height=14>" +
                "This Option is not Accessible for you....!!! \n" +
                "</form>" +
                "</body></html>";
        return false;
    }
}

