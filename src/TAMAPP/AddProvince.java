package TAMAPP;

/**
 * Created by Siddiqui on 9/13/2017.
 */

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
public class AddProvince extends HttpServlet {
    String ScreenNo = "6";
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;

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
        String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = this.getServletContext();
        String UserId = "";
        try {
            HttpSession session = request.getSession(true);
            if (session.getAttribute("UserId") == null || session.getAttribute("UserId").equals("")) {
                out.println(" <center><table cellpadding=3 cellspacing=2><tr><td bgcolor=\"#FFFFFF\"> " +
                        " <font color=green face=arial><b>Your Session Has Been Expired</b></font> " +
                        " </td></tr></table> " +
                        " <p> " +
                        " <font face=arial size=+1><b><a href=/TAMAPP/index.html target=_top> Return to Login Page " +
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
                if ( !ValidSession ) {
                    out.flush();
                    out.close();
                    return;
                }
                if ( UserId == "" ) {
                    out.println("<font size=\"3\" face=\"Calibri\">Your session has been expired, please login again.</font>");
                    out.flush();
                    out.close();
                    return;
                }

                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connect_string);
            } catch (Exception e) {
                conn = null;
                out.println("Exception conn: " + e.getMessage());
            }
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }
            if ( request.getParameter("ActionID") == null ) {
                Action = "Home";
                return;
            }

            Action = request.getParameter("ActionID");
            if ( Action.equals("GETINPUT") ) {
                this.GetInput(request, out, conn, context, UserId);
            } else if ( Action.equals("SaveRecords") ) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if ( Action.equals("GetProvinceInfo") ) {
                this.GetProvinceInfo(request, out, conn, context, UserId);
            } else {
                out.println("Under Development ... " + Action);
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

    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuffer ProvinceList = new StringBuffer();
        StringBuffer CountryList = new StringBuffer();
        try {
            Query = "SELECT Id,countryName FROM Country  WHERE Status=0  ORDER BY Id";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            CountryList.append("<option value=\'\' selected disabled>Select Country</option>");

            while (rset.next()) {
                CountryList.append("<option value=" + rset.getString(1) + ">" + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();
            Query = "SELECT a.Id,a.ProvinceName,(CASE WHEN a.Status=0 THEN \'Active\' ELSE \'Inactive\' END), \n " +
                    " a.CountryIndex,a.CreatedDate,b.countryName FROM  Province a, Country b \n" +
                    " WHERE a.CountryIndex=b.Id AND a.Status=0 ORDER BY a.Id";

            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (true) {
                    if ( !rset.next() ) {
                        rset.close();
                        stmt.close();
                        break;
                    }

                    ProvinceList.append("<tr>");
                    ProvinceList.append("<td width=02%>" + rset.getInt(1) + "</td>");
                    ProvinceList.append("<td width=10%> " + rset.getString(2) + "</td>");
                    ProvinceList.append("<td width=10%> " + rset.getString(6) + "</td>");
                    ProvinceList.append("<td width=10%> " + rset.getString(3) + "</td>");
                    ProvinceList.append("<td width=10%><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");
                    //ProvinceList.append("<td width=10%><button class='btn-xs btn btn-info mylink' value="+rset.getInt(1)+" target=NewFrame1><i class=\"fa fa-edit \"></i>[Edit]</button> </td>");

                    ProvinceList.append("</tr>");
                }
            } catch (Exception var18) {
                out.println(var18.getMessage());
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var17) {
                }

                Supportive.doLog(this.getServletContext(), "Add Province-01", var18.getMessage(), var18);
                out.flush();
                out.close();
            }

            Parsehtm e1 = new Parsehtm(request);
            e1.SetField("ProvinceList", ProvinceList.toString());
            e1.SetField("CountryList", CountryList.toString());
            e1.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddProvince.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddProvince");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }

            Supportive.doLog(this.getServletContext(), "Add Province-01", var19.getMessage(), var19);
            out.flush();
            out.close();
        }
    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
    	
    	String chk = "";
        String ProvinceName = request.getParameter("ProvinceName").trim();
        String Status = request.getParameter("Status").trim();
        String CountryIndex = request.getParameter("CountryIndex").trim();
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);

        if (count == 0) {
            try {
                pStmt = conn.prepareStatement(
                        "INSERT INTO Province (ProvinceName,Status,CreatedDate,CountryIndex) \n " +
                                " VALUES (?,?,now(),?)");
                pStmt.setString(1, ProvinceName);
                pStmt.setString(2, Status);
                pStmt.setString(3, CountryIndex);
                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception e1) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddProvince");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception e) {
                }

                Supportive.doLog(this.getServletContext(), "Save Province-01", e1.getMessage(), e1);
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("ProvinceIndex").trim();
            int ProvinceIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE Province SET ProvinceName='" + ProvinceName + "',Status=" + Status + ",CountryIndex=" + CountryIndex + " " +
                        " WHERE Id = " + ProvinceIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception var34) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddProvince");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "Edit Province-02", var34.getMessage(), var34);
                out.flush();
                out.close();
            }
        }

        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();
    }

    void GetProvinceInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        int CountryIndex = 0;
        String Status = "";
        String chk = "";
        String ProvinceName = "";
        int ProvinceId = 0;
        Query = request.getParameter("ProvinceIndex").trim();
        int SelectedIndex = Integer.parseInt(Query);

        try {
            Query = "Select Id,ProvinceName,Status,CountryIndex from Province where Id=" + SelectedIndex;

            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (true) {
                    if ( !rset.next() ) {
                        rset.close();
                        stmt.close();
                        break;
                    }

                    ProvinceId = rset.getInt(1);
                    ProvinceName = rset.getString(2);
                    Status = rset.getString(3);
                    CountryIndex = rset.getInt(4);
                }
            } catch (Exception var31) {
                out.flush();
                out.close();
                return;
            }
        } catch (Exception var32) {
            chk = "13";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
            return;
        }

        out.println(ProvinceName + "|" + Status + "|" + ProvinceId + "|" + CountryIndex);
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
