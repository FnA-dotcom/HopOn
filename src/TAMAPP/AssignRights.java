package TAMAPP;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@SuppressWarnings("Duplicates")
public class AssignRights extends HttpServlet {
    String ScreenNo = "31";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public String ErrorText() {
        return "Error In Assign Rights Option";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Connection conn = null;
        String ActionID = null;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = this.getServletContext();
        String UserId;

        try {
            HttpSession session = request.getSession(true);
            if (session.getAttribute("UserId") == null || session.getAttribute("UserId").equals("")) {
                out.println(" <center><table cellpadding=3 cellspacing=2><tr><td bgcolor=\"#FFFFFF\"> " +
                        " <font color=green face=arial><b>Your Session Has Been Expired</b></font> " +
                        " </td></tr></table> " +
                        " <p> " +
                        " <font face=arial size=+1><b><a href=/NatureApp/index.html target=_top> Return to Login Page " +
                        " </a></b></font>" +
                        " <br><font face=arial size=-2>(You will need to sign in again.)</font><br> " +
                        " </center> ");
                out.flush();
                out.close();
                session.removeAttribute("UserId");
                return;
            }
            UserId = session.getAttribute("UserId").toString();
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

                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connect_string);
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
                ActionID = "Home";
                return;
            }

            ActionID = request.getParameter("ActionID");
            if (ActionID.equals("GetUserInfo")) {
                GetUserInfo(request, out, conn, UserId);
            } else if (ActionID.equals("GetUserRights")) {
                this.GetUserRights(request, out, conn, UserId);
            } else if (ActionID.equals("SaveUserRights")) {
                SaveUserRights(request, out, conn, UserId);
            } else {
                out.println("Under Development ... " + ActionID);
            }

            conn.close();
        } catch (Exception var15) {
            out.println("Exception in main... " + var15.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private void GetUserInfo(HttpServletRequest request, PrintWriter out, Connection conn, String UserId) throws FileNotFoundException {
        Statement stmt = null;
        ResultSet rset = null;
        int ListCount = 0;
        String Query = "";
        StringBuffer UserList = new StringBuffer();
        StringBuffer OptionsList = new StringBuffer();

        Query = "SELECT Id, UserName FROM SystemUsers WHERE Status=0 ORDER BY UserName ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            UserList.append("<option value=0> Select User </option>");
            while (rset.next()) {
                UserList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>\n");
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            out.println(e.getMessage());
            Supportive.doLog(this.getServletContext(), "Assign Rights-01", e.getMessage(), e);
            out.flush();
            out.close();
        }
        Query = "SELECT max(Indexptr) FROM ScreenNames WHERE SystemIndex=2 ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                ListCount = rset.getInt(1);
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            out.println(e.getMessage());
            Supportive.doLog(this.getServletContext(), "Assign Rights-02", e.getMessage(), e);
            out.flush();
            out.close();
        }

        OptionsList.append("<div class=\"col-sm-12\">");
        OptionsList.append("<div class=\"table-responsive\">");
        OptionsList.append("<table class=\"table table-bordered table-hover\">");
        OptionsList.append("<thead>");
        OptionsList.append("<tr>");
        OptionsList.append("<th>List of Options:&nbsp;<b style='font-size:30px;'>Isadmin</b>: <input type=\"checkbox\" id=\"isadmincheck\"  name=\"isadmincheck\" value='1'></th>");
        OptionsList.append("</tr>");
        OptionsList.append("</thead>");
        OptionsList.append("<tbody>");
        Query = "SELECT Indexptr, ScreenName, ScreenLevel FROM ScreenNames WHERE SystemIndex=2 " +
                " ORDER BY DisplayOrder, ScreenName ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                OptionsList.append("<tr>");
                OptionsList.append("<td width=100%>");
                if (rset.getInt(3) == 1) {
                    OptionsList.append("<div class=\"i-checks\" style=\"margin-left: 30px;\"><label style=\"font-weight:normal;\"> <input type=\"checkbox\" name=\"Option_" + rset.getInt(1) + "\" id=\"Option_" + rset.getInt(1) + "\" style=\"display:none;\"> <b> " + rset.getString(2) + " </b></label></div>");
                } else {
                    OptionsList.append("<div class=\"col-sm-10 col-sm-offset-1\"><div class=\"i-checks\" style=\"margin-left: 30px;\"><label style=\"font-weight:normal;\"> <input type=\"checkbox\" name=\"Option_" + rset.getInt(1) + "\" id=\"Option_" + rset.getInt(1) + "\"> " + rset.getString(2) + " </label></div></div>");
                }
                OptionsList.append("</td>");
                OptionsList.append("</tr>");
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            out.println(e.getMessage());
            Supportive.doLog(this.getServletContext(), "Assign Rights-03", e.getMessage(), e);
            out.flush();
            out.close();
        }
        OptionsList.append("</tbody>");
        OptionsList.append("</table>");
        OptionsList.append("</div>");
        OptionsList.append("</div>");

        Parsehtm Parser = new Parsehtm(request);
        Parser.SetField("UserList", UserList.toString());
        Parser.SetField("OptionsList", OptionsList.toString());
        Parser.SetField("ListCount", String.valueOf(ListCount));
        Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Forms/AssignRights.html");
        out.flush();
        out.close();
    }

    private void GetUserRights(HttpServletRequest request, PrintWriter out, Connection conn, String UserId) throws IOException {
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "", chk = "", RightsString = "";
        StringBuffer OptionsList = new StringBuffer();

        Query = request.getParameter("Indexptr").trim();
        int Indexptr = Integer.parseInt(Query);
        Query = request.getParameter("SystemIndex").trim();
        int SystemIndex = Integer.parseInt(Query);

        String isadmincheck = request.getParameter("isadmincheck").trim();
        int _admicheck = Integer.parseInt(isadmincheck);
        String ClientUserIndex = "";
        Query = "Select Id from SystemUsers Where ltrim(rtrim(upper(UserId)))=ltrim(rtrim(upper('" + UserId + "'))) ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                ClientUserIndex = rset.getString(1);
            }
        } catch (Exception e) {
            out.println("Error in Query-0001");
        }

        if (_admicheck == 1) {

            Query = "UPDATE SystemUsers SET isAdmin='1' WHERE Id='" + ClientUserIndex + "' ";
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception e) {
                out.println("Error in Query-00002");
            }
        } else {
            Query = " UPDATE SystemUsers SET isAdmin='0' WHERE Id='" + ClientUserIndex + "'";
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception e) {
                out.println("Error in Query-0002");
            }
        }


        OptionsList.append("<div class=\"col-sm-12\">");
        OptionsList.append("<div class=\"table-responsive\">");
        OptionsList.append("<table class=\"table table-bordered table-hover\">");
        OptionsList.append("<thead>");
        OptionsList.append("<tr>");
        OptionsList.append("<th>List of Options:&nbsp;<b style='font-size:30px;'>Isadmin</b>: <input type=\"checkbox\" id=\"isadmincheck\"  name=\"isadmincheck\" value='1'></th>");
        OptionsList.append("</tr>");
        OptionsList.append("</thead>");
        OptionsList.append("<tbody>");
        Query = "Select a.Indexptr, a.ScreenName, a.ScreenLevel, b.UserRights, a.SystemIndex, \n" +
                "a.DisplayOrder From ScreenNames a LEFT JOIN UserRights b ON \n" +
                "a.Indexptr=b.ScreenNo And a.SystemIndex=" + SystemIndex + " And \n" +
                "b.EmployeeIndex=" + Indexptr + " Order By a.DisplayOrder ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                if (rset.getInt(5) != SystemIndex)
                    continue;

                OptionsList.append("<tr>");
                OptionsList.append("<td width=100%>");
                if (rset.getInt(3) == 1) {
                    OptionsList.append("<div class=\"i-checks\" style=\"margin-left: 30px;\"><label style=\"font-weight:normal;\"> <input type=\"checkbox\" name=\"Option_" + rset.getInt(1) + "\" id=\"Option_" + rset.getInt(1) + "\" style=\"display:none;\"> <b> " + rset.getString(2) + " </b></label></div>");
                } else {
                    if (rset.getInt(4) == 1)
                        OptionsList.append("<div class=\"col-sm-10 col-sm-offset-1\"><div class=\"i-checks\" style=\"margin-left: 30px;\"><label style=\"font-weight:normal;\"> <input type=\"checkbox\" checked name=\"Option_" + rset.getInt(1) + "\" id=\"Option_" + rset.getInt(1) + "\"> " + rset.getString(2) + " </label></div></div>");
                    else
                        OptionsList.append("<div class=\"col-sm-10 col-sm-offset-1\"><div class=\"i-checks\" style=\"margin-left: 30px;\"><label style=\"font-weight:normal;\"> <input type=\"checkbox\" name=\"Option_" + rset.getInt(1) + "\" id=\"Option_" + rset.getInt(1) + "\"> " + rset.getString(2) + " </label></div></div>");
                }
                OptionsList.append("</td>");
                OptionsList.append("</tr>");
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            out.println("<br>" + ErrorText());
            out.println("<br>Error No.: 0003");
            out.println("<br>Error Is : Could not get Task Index ...!!! \n\n\n</b>");
            out.println("<input class=\"buttonERP\" type=button name=Back Value=\"  Back  \" onclick=history.back()>");
            out.println("</form></body></html>");
            out.flush();
            out.close();
            return;
        }
        OptionsList.append("</tbody>");
        OptionsList.append("</table>");
        OptionsList.append("</div>");
        OptionsList.append("</div>");
        out.println(String.valueOf(OptionsList));
        out.flush();
        out.close();
    }

    private void SaveUserRights(HttpServletRequest request, PrintWriter out, Connection conn, String UserId) throws IOException {
        Statement stmt = null;
        ResultSet rset = null;
        int i = 0, opt = 0;
        String Query = "", chk = "", RightsString = "";

        Query = request.getParameter("UserIndex").trim();
        int Indexptr = Integer.parseInt(Query);
        Query = request.getParameter("ScreenCount").trim();
        int ScreenCount = Integer.parseInt(Query);
        String isadmincheck = request.getParameter("isadmincheck").trim();
        int _admicheck = Integer.parseInt(isadmincheck);

        String ClientUserIndex = "";

        Query = "Select Id from SystemUsers Where ltrim(rtrim(upper(UserId)))=ltrim(rtrim(upper('" + UserId + "'))) ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                ClientUserIndex = rset.getString(1);
            }
        } catch (Exception e) {
            out.println("Error in Query-00001");
        }


        if (_admicheck == 1) {

            Query = "UPDATE SystemUsers SET isAdmin='1' WHERE Id='" + ClientUserIndex + "' ";
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception e) {
                out.println("Error in Query-002");
            }

        } else {
            Query = " UPDATE SystemUsers SET isAdmin='0' WHERE Id='" + ClientUserIndex + "'";
            try {
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception e) {
                out.println("Error in Query-02");
            }
        }


        Query = "Delete From UserRights Where SystemIndex=2 And \n" +
                "EmployeeIndex=" + Indexptr;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();
        } catch (Exception e) {
            chk = "20";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
        }
        for (i = 0; i <= ScreenCount; i++) {
            opt = 0;
            if (request.getParameter("Option_" + i) == null)
                opt = 0;
            else {
                Query = request.getParameter("Option_" + i).trim();
                opt = Integer.parseInt(Query);
            }
            if (opt == 0)
                continue;

            try {
                PreparedStatement MainReceipt = conn.prepareStatement(
                        "INSERT INTO UserRights (ClientIndex, SystemIndex, EmployeeIndex, ScreenNo, UserRights) \n" +
                                "VALUES (1,2,?,?,?) ");
                MainReceipt.setInt(1, Indexptr);
                MainReceipt.setInt(2, i);
                MainReceipt.setInt(3, opt);
                MainReceipt.executeUpdate();
                MainReceipt.close();
            } catch (Exception e) {
                chk = "30";
                out.println(String.valueOf(chk));
                out.flush();
                out.close();
            }
        }
        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();
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
