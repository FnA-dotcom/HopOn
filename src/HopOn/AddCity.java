package HopOn;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class AddCity extends HttpServlet {
    String ScreenNo = "7";
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
//        String connect_string = supp.GetConnectString();
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
            if (Action.equals("GETINPUT")) {
                this.GetInput(request, out, conn, context, UserId, CityIndex);
            } else if (Action.equals("SaveRecords")) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if (Action.equals("GetCityInfo")) {
                this.GetCityInfo(request, out, conn, context, UserId);
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

    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int CityIndex) {
        stmt = null;
        rset = null;
        int SrlNo = 1;
        Query = "";
        StringBuffer CountryList = new StringBuffer();
        StringBuffer ProvinceList = new StringBuffer();
        StringBuffer CityList = new StringBuffer();

        try {
            if (UserId.equals("tabish"))
                Query = "SELECT Id, Upper(CountryName) FROM Country WHERE Status=0 ORDER BY Upper(CountryName) ";
            else
                Query = "Select Id, Upper(CountryName) from Country Where Status=0 AND CityIndex = " + CityIndex + " Order By Upper(CountryName) ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            CountryList.append("<option value=selected disabled>Select Country</option>");
            while (rset.next()) {
                CountryList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();
            Query = "SELECT Id, Upper(ProvinceName) FROM Province WHERE " +
                    "CountryIndex IN (SELECT Id FROM Country WHERE Status=0) AND Status=0 " +
                    "ORDER BY Upper(ProvinceName) ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            ProvinceList.append("<option value=selected disabled>Select ProvinceList</option>");
            while (rset.next()) {
                ProvinceList.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }

            rset.close();
            stmt.close();

            Query = "SELECT a.Id,a.cityName,(CASE WHEN a.Status=0 THEN 'Active' ELSE 'Inactive' END),b.countryName, " +
                    "c.ProvinceName FROM City a,Country b,Province c " +
                    "WHERE a.CountryIndex=b.Id AND a.ProvinceIndex=c.Id AND a.Status='0'";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (true) {
                    if (!rset.next()) {
                        rset.close();
                        stmt.close();
                        break;
                    }

                    CityList.append("<tr>");
                    CityList.append("<td>" + SrlNo + "</td>");
                    CityList.append("<td>" + rset.getString(2) + "</td>");
                    CityList.append("<td>" + rset.getString(5) + "</td>");
                    CityList.append("<td>" + rset.getString(4) + "</td>");
                    CityList.append("<td>" + rset.getString(3) + "</td>");
                    CityList.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");
                    //CityList.append("<td width=10%><button class='btn-xs btn btn-info mylink' value="+rset.getInt(1)+" target=NewFrame1><i class=\"fa fa-edit \"></i>[Edit]</button> </td>");

                    CityList.append("</tr>");
                    ++SrlNo;
                }
            } catch (Exception var18) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddCity");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var17) {
                }
            }

            Parsehtm var20 = new Parsehtm(request);
            var20.SetField("CityList", CityList.toString());
            var20.SetField("StateList", ProvinceList.toString());
            var20.SetField("CountryList", CountryList.toString());
            var20.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddCity.html");
            out.flush();
            out.close();
        } catch (Exception var19) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddCity");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }

        out.flush();
        out.close();
    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String chk = "";

        String City = request.getParameter("CityName").trim();
        String Status = request.getParameter("Status").trim();
        Query = request.getParameter("CountryIndex").trim();
        int CountryIndex = Integer.parseInt(Query);
        Query = request.getParameter("ProvinceIndex").trim();
        int ProvinceIndex = Integer.parseInt(Query);
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);

        if (count == 0) {
            try {
                pStmt = conn.prepareStatement(
                        "INSERT INTO City (cityName, Status,CountryIndex,ProvinceIndex,CreatedDate,EnterBy) " +
                                "VALUES (?,?,?,?,now(),?)");
                pStmt.setString(1, City);
                pStmt.setString(2, Status);
                pStmt.setInt(3, CountryIndex);
                pStmt.setInt(4, ProvinceIndex);
                pStmt.setString(5, UserId);
                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddCity");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception e1) {
                }

                Supportive.doLog(this.getServletContext(), "Edit City-02", e.getMessage(), e);
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("CityIndex").trim();
            int CityIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE City SET cityName='" + City + "',Status=" + Status + ",CountryIndex=" + CountryIndex + ",ProvinceIndex=" + ProvinceIndex + " " +
                        " WHERE Id = " + CityIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception var34) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddCity");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "Edit City-02", var34.getMessage(), var34);
                out.flush();
                out.close();
            }
        }

        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();
    }

    void GetCityInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        stmt = null;
        rset = null;
        String chk = "";
        Query = request.getParameter("CityIndex").trim();
        int CityIndex = Integer.parseInt(Query);
        String CityName = "";
        int ProvinceIndex = 0;
        int CountryIndex = 0;
        String Status = "";

        Query = "Select Id,cityName,Status,ProvinceIndex,CountryIndex from City where Id=" + CityIndex;

        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            while (true) {
                if (!rset.next()) {
                    rset.close();
                    stmt.close();
                    break;
                }

                CityName = rset.getString(2);
                Status = rset.getString(3);
                ProvinceIndex = rset.getInt(4);
                CountryIndex = rset.getInt(5);
            }
            rset.close();
            stmt.close();
        } catch (Exception var35) {
            out.flush();
            out.close();
            return;
        }

        out.println(CityName + "|" + CityIndex + "|" + Status + "|" + CountryIndex + "|" + ProvinceIndex);
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


