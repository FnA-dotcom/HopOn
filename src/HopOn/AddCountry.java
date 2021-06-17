package HopOn;

/**
 * Created by Siddiqui on 9/12/2017.
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
public class AddCountry extends HttpServlet {

    String ScreenNo = "5";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;
    String Query = "";

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
        context = this.getServletContext();
        String UserId;
        try {
            HttpSession session = request.getSession(true);
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
                this.GetInput(request, out, conn, context);
            } else if (Action.equals("SaveRecords")) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if (Action.equals("GetCountryInfo")) {
                this.GetCountryInfo(request, out, conn, context);
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

    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuffer CountryList = new StringBuffer();
        int SerialNo = 1;
        try {
            Query = "Select Id,countryName,(Case When Status=0 Then \'Active\' Else \'Inactive\' End) \n " +
                    "from Country  Order By Id";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (true) {
                    if (!rset.next()) {
                        rset.close();
                        stmt.close();
                        break;
                    }

                    CountryList.append("<tr>");
                    CountryList.append("<td width=02%>" + SerialNo + "</td>");
                    CountryList.append("<td width=10%> " + rset.getString(2) + "</td>");
                    CountryList.append("<td width=10%> " + rset.getString(3) + "</td>");
                    CountryList.append("<td width=10%><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");
                    CountryList.append("</tr>");
                    SerialNo++;
                }
            } catch (Exception var16) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "AddCountry-01", var16.getMessage(), var16);
                out.flush();
                out.close();
            }

            Parsehtm e = new Parsehtm(request);
            e.SetField("CountryList", CountryList.toString());
            e.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddCountry.html");
            out.flush();
            out.close();
        } catch (Exception var17) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }

            Supportive.doLog(this.getServletContext(), "AddCountry-02", var17.getMessage(), var17);
            out.flush();
            out.close();
        }

    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        String chk = "";
        String Country = request.getParameter("Country").trim();
        String Status = request.getParameter("Status").trim();
        Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(Query);

        if (count == 0) {
            try {
                pStmt = conn.prepareStatement("Insert into Country (countryName, Status, CreatedDate) " +
                        "values (?,?,now())");
                pStmt.setString(1, Country);
                pStmt.setString(2, Status);
                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception var35) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "SaveCountry-01", var35.getMessage(), var35);
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("CountryIndex").trim();
            int CountryIndex = Integer.parseInt(Query);
            try {
                Query = "UPDATE Country SET CountryName='" + Country + "',Status=" + Status + " WHERE Id = " + CountryIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception var34) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "EditCountry-02", var34.getMessage(), var34);
                out.flush();
                out.close();
            }
        }

        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();
    }

    void GetCountryInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {

        String Query = "";
        stmt = null;
        rset = null;
        String Status = "";
        String chk = "";
        String CountryName = "";
        Query = request.getParameter("CountryIndex").trim();
        int CountryIndex = Integer.parseInt(Query);

        try {
            Query = "Select Id,CountryName,Status from Country where Id=" + CountryIndex;

            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next()) {
                    CountryName = rset.getString(2);
                    Status = rset.getString(3);
                }
            } catch (Exception var31) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception var15) {
                }

                Supportive.doLog(this.getServletContext(), "CountryInfo-01", var31.getMessage(), var31);
                out.flush();
                out.close();
            }
        } catch (Exception var32) {
            chk = "13";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
            return;
        }

        out.println(CountryName + "|" + CountryIndex + "|" + Status);
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

