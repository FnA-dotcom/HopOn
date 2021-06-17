package HopOn;

import Parsehtm.Parsehtm;

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
public class TotalJobs extends HttpServlet {

    private String ScreenNo = "14";
    private String Query = "";
    private PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String ActionID = null;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        ServletContext context = null;
        context = getServletContext();
        String UserId = "";
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
            if (request.getParameter("ActionID") == null) {
                ActionID = "Home";
                return;
            }
/*            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }*/

            ActionID = request.getParameter("ActionID");
            if (ActionID.equals("GETINPUT")) {
                GetInput(request, out, conn, context, UserId);
            } else if (ActionID.equals("SaveRecords")) {
                SaveRecords(request, out, conn, context, UserId);
            } else if (ActionID.equals("GetJobInfo")) {
                GetJobInfo(request, out, conn, context);
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

        out.flush();
        out.close();
    }

    void GetInput(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String userId) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuffer JobList = new StringBuffer();
        StringBuffer CityList = new StringBuffer();
        int SerialNo = 1;

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
        } catch (Exception var15) {
            out.println(var15);
        }

        try {
            Query = "SELECT a.Id,a.TotalCount,(CASE WHEN a.Status=0 THEN \'Active\' ELSE \'InActive\' END),b.CityName  \n " +
                    "FROM TotalJobsHand  a INNER JOIN City b on a.CityIndex = b.Id  ORDER BY a.Id";
            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);

                while (rset.next()) {
                    JobList.append("<tr>");
                    JobList.append("<td width=02%>" + SerialNo + "</td>");
                    JobList.append("<td width=10%> " + rset.getString(2) + "</td>");
                    JobList.append("<td width=10%> " + rset.getString(3) + "</td>");
                    JobList.append("<td width=10%> " + this.rset.getString(4) + "</td>");
                    JobList.append("<td width=10%><i class=\"fa fa-edit\" data-toggle=\"modal\" data-target=\"#myModal\" onClick=\"editRow(" + rset.getInt(1) + ")\"></i></td>");
                    JobList.append("</tr>");
                    SerialNo++;
                }
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                    out.println(ex.getMessage());
                }

                Supportive.doLog(this.getServletContext(), "TotalJobs-01", e.getMessage(), e);
                out.flush();
                out.close();
            }

            Parsehtm e = new Parsehtm(request);
            e.SetField("JobList", JobList.toString());
            e.SetField("CityList", CityList.toString());
            e.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/TotalJobs.html");
            out.flush();
            out.close();
        } catch (Exception e) {
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
            } catch (Exception ex) {
            }

            Supportive.doLog(this.getServletContext(), "TotalJobs-02", e.getMessage(), e);
            out.flush();
            out.close();
        }

    }

    void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        Query = "";
        String chk = "";
        String TotalCount = request.getParameter("TotalCount").trim();
        String Status = request.getParameter("Status").trim();
        this.Query = request.getParameter("Count").trim();
        int count = Integer.parseInt(this.Query);
        this.Query = request.getParameter("City_Code").trim();
        int City_Code = Integer.parseInt(this.Query);
        if (count == 0) {
            try {
                pStmt = conn.prepareStatement("INSERT INTO TotalJobsHand (TotalCount, Status, CreatedDate,CityIndex) " +
                        "VALUES (?,?,now(),?)");
                pStmt.setString(1, TotalCount);
                pStmt.setString(2, Status);
                pStmt.setInt(3, City_Code);
                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                }

                Supportive.doLog(this.getServletContext(), "SaveCount-01", e.getMessage(), e);
                out.flush();
                out.close();
            }
        } else {
            Query = request.getParameter("TotalIndex").trim();
            int JobIndex = Integer.parseInt(Query);
            try {

                Query = "SELECT TotalCount, Status, CreatedDate,UpdatedBy,UpdatedDate,CityIndex FROM TotalJobsHand ";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next()) {
                    pStmt = conn.prepareStatement("INSERT INTO TotalJobsHandHistory (TotalCount, Status, CreatedDate,UpdatedBy,UpdatedDate,CityIndex) " +
                            "VALUES (?,?,?,?,?,?)");
                    pStmt.setString(1, rset.getString(1));
                    pStmt.setString(2, rset.getString(2));
                    pStmt.setString(3, rset.getString(3));
                    pStmt.setString(4, rset.getString(4));
                    pStmt.setString(5, rset.getString(5));
                    pStmt.setString(6, rset.getString(6));

                    pStmt.executeUpdate();
                    pStmt.close();
                }
                rset.close();
                stmt.close();

                Query = "UPDATE TotalJobsHand SET TotalCount='" + TotalCount + "',Status=" + Status + "," +
                        " UpdatedBy = '" + UserId + "',UpdatedDate = NOW() " +
                        " WHERE Id = " + JobIndex;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            } catch (Exception e) {
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Exception6.html");
                } catch (Exception ex) {
                }

                Supportive.doLog(this.getServletContext(), "EditStatus-02", e.getMessage(), e);
                out.flush();
                out.close();
            }
        }
        chk = "1";
        out.println(String.valueOf(chk));
        out.flush();
        out.close();
    }

    void GetJobInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {

        String Query = "";
        stmt = null;
        rset = null;
        String Status = "";
        String chk = "";
        String TotalCount = "";
        Query = request.getParameter("TotalIndex").trim();
        int TotalIndex = Integer.parseInt(Query);

        try {
            Query = "Select Id,TotalCount,Status from TotalJobsHand where Id=" + TotalIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);

            if (rset.next()) {

                TotalCount = rset.getString(2);
                Status = rset.getString(3);
            }
            rset.close();
            stmt.close();
        } catch (Exception ex) {
            chk = "13";
            out.println(String.valueOf(chk));
            out.flush();
            out.close();
            return;
        }

        out.println(TotalCount + "|" + TotalIndex + "|" + Status);
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
 
