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
public class AddJobNature extends HttpServlet {
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;
    private String ScreenNo = "11";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        handleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        handleRequest(request, response);
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        String Action = null;

        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");

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

            if (Action.equals("GETINPUT"))
                GETINPUT(request, out, conn, context, UserId, CityIndex, isAdmin);
            else if (Action.equals("SaveRecords"))
                SaveRecords(request, out, conn, context, UserId, CityIndex);
            else if (Action.equals("GetJobNatureInfo"))
                GetJobNature(request, out, conn, context, UserId);

            else {
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

    void GETINPUT(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int cityIndex, int isAdmin) {
        stmt = null;
        rset = null;
        int SrlNo = 1;
        Query = "";
        StringBuffer JobNature = new StringBuffer();
        StringBuffer CityList = new StringBuffer();
        try {
            this.Query = "SELECT Id,CityName FROM City  WHERE Status=0  ORDER BY Id";
            this.stmt = conn.createStatement();
            this.rset = this.stmt.executeQuery(this.Query);
            CityList.append("<option value='' selected disabled>Select City</option>");
            while (this.rset.next()) {
                CityList.append("<option value=" + this.rset.getString(1) + ">" + this.rset.getString(2) + "</option>");
            }
            this.rset.close();
            this.stmt.close();
        } catch (Exception e) {
            out.println(e);
        }
        try {
//            Query = "SELECT Id,JobNature,(CASE WHEN Status=0 THEN 'Active' ELSE 'Inactive' END) " +
//                    "FROM JobNature ORDER BY Id";
            if (isAdmin == 1) {
                this.Query = "SELECT a.Id,a.JobNature,(CASE WHEN a.Status=0 THEN 'Active' ELSE 'Inactive' END),b.CityName FROM JobNature a INNER JOIN City b on a.CityIndex = b.Id ORDER BY Id;";
            } else {
                this.Query = ("SELECT a.Id,a.JobNature,(CASE WHEN a.Status=0 THEN 'Active' ELSE 'Inactive' END),b.CityName FROM JobNature a INNER JOIN City b on a.CityIndex = b.Id  where b.Id = " + cityIndex + " ORDER BY Id;");

                this.Query = ("SELECT a.Id,a.JobNature,(CASE WHEN a.Status=0 THEN 'Active' ELSE 'Inactive' END) FROM JobNature ,b.CityName FROM JobNature a INNER JOIN City b on a.CityIndex = b.Id where CityIndex = " + cityIndex + " ORDER BY Id");
            }
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    JobNature.append("<tr>");
                    JobNature.append("<td>" + SrlNo + "</td>");
                    JobNature.append("<td>" + rset.getString(2) + "</td>");
                    JobNature.append("<td>" + rset.getString(3) + "</td>");
                    JobNature.append("<td>" + this.rset.getString(4) + "</td>");
                    //JobNature.append("<td width=10%><button class='btn-xs btn btn-info mylink' value="+rset.getInt(1)+" target=NewFrame1><i class=\"fa fa-edit \"></i>[Edit]</button> </td>");
                    JobNature.append("<td><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");

                    JobNature.append("</tr>");
                    SrlNo++;
                }
                rset.close();
                stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddJobNature");
                    Parser.SetField("ActionID", "GETINPUT");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception ex) {
                }
                Supportive.doLog(this.getServletContext(), "Add Job Type -01", e.getMessage(), e);
                out.flush();
                out.close();
            }


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("JobNature", JobNature.toString());
            Parser.SetField("CityList", CityList.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AddJobNature.html");
            out.flush();
            out.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "AddJobNature");
                Parser.SetField("ActionID", "GETINPUT");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception ex) {
            }
            Supportive.doLog(this.getServletContext(), "AddJobNature 0000-01", e.getMessage(), e);
            out.flush();
            out.close();
        }
    }

    void GetJobNature(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        this.Query = "";
        this.stmt = null;
        this.rset = null;
        String Status = "";
        String chk = "";
        String CountryName = "";
        String ID = request.getParameter("JobNatureIndex").trim();
        int indexptr = Integer.parseInt(ID);

        int CityIndex = 0;
        try {
            this.Query = ("Select Id,JobNature,Status,CityIndex from JobNature where Id=" + indexptr);
            try {
                this.stmt = conn.createStatement();
                this.rset = this.stmt.executeQuery(this.Query);
                while (this.rset.next()) {
                    CountryName = this.rset.getString(2);
                    Status = this.rset.getString(3);
                    CityIndex = this.rset.getInt(4);
                }
                this.rset.close();
                this.stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception localException1) {
                }
                Supportive.doLog(getServletContext(), "AddJobNature 0001-01", e.getMessage(), e);
                out.flush();
                out.close();
            }
            out.println(String.valueOf(CountryName) + "|" + String.valueOf(ID) + "|" + String.valueOf(Status) + "|" + String.valueOf(CityIndex));
        } catch (Exception e) {
            chk = "13";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
            return;
        }
        out.flush();
        out.close();
    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId, int cityIndex) {
        String chk = "";

        String JobNature = request.getParameter("JobNature").trim();
        String Status = request.getParameter("Status").trim();

        this.Query = request.getParameter("City_Code").trim();
        int City_Code = Integer.parseInt(this.Query);

        this.Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(this.Query);
        if (count == 0) {
            try {
                PreparedStatement MainReceipt = conn.prepareStatement(
                        "INSERT INTO JobNature (JobNature, Status, CreatedDate,CityIndex) VALUES (?,?,now(),?)");

                MainReceipt.setString(1, JobNature);
                MainReceipt.setString(2, Status);
                MainReceipt.setInt(3, City_Code);
                MainReceipt.executeUpdate();
                MainReceipt.close();
            } catch (Exception e) {
                out.println(e);
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception localException1) {
                }
                Supportive.doLog(getServletContext(), "AddDesignation-01", e.getMessage(), e);
                out.flush();
                out.close();
            }
            out.println("1");
        } else {
            try {
                String JobNatureIndex = request.getParameter("JobNatureIndex").trim();

                PreparedStatement MainReceipt = conn.prepareStatement(
                        "Update JobNature SET JobNature = ?, Status = ?,CityIndex=" + City_Code + ", CreatedDate=now()  WHERE Id = " +
                                JobNatureIndex);
                MainReceipt.setString(1, JobNature);
                MainReceipt.setString(2, Status);
                MainReceipt.executeUpdate();
                MainReceipt.close();
            } catch (Exception e) {
                chk = "13";
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception localException2) {
                }
                Supportive.doLog(getServletContext(), "AddJobNature-01", e.getMessage(), e);
                out.flush();
                out.close();
            }
            out.println("1");
        }
        out.flush();
        out.close();
        return;
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
