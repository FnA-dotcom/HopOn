package TAMAPP;

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
public class MobileUsers extends HttpServlet {
    private String ScreenNo = "30";

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
        context = getServletContext();
        String UserId;
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
            } catch (Exception excp) {
                conn = null;
                out.println("Exception excp conn: " + excp.getMessage());
            }
            AuthorizeUser authent = new AuthorizeUser(conn);
            if (!authent.AuthorizeScreen(UserId, this.ScreenNo)) {
                UnAuthorize(authent, out, conn);
                return;
            }
            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            Action = request.getParameter("ActionID");


            if (Action.equals("GETINPUT")) {
                this.GetInput(request, out, conn, context);
            } else if (Action.compareTo("SaveMobileUser") == 0) {
                this.SaveRecords(request, out, conn, context, UserId);
            } else if (Action.compareTo("Edit") == 0) {
                this.ShowEditInfo(request, out, conn, context, UserId);
            } else if (Action.compareTo("Delete") == 0) {
                this.DeleteInfo(request, out, conn, context, UserId);
            } else if (Action.compareTo("EditMobileUsers") == 0) {
                this.UpdateMobileUsers(request, out, conn, context, UserId);
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
        int srlno = 1;
        String Query = "";
        Statement stmt = null;
        ResultSet rset = null;

        StringBuilder EngineerTypes = new StringBuilder();
        StringBuilder UserNames = new StringBuilder();
        StringBuilder DisplayTable = new StringBuilder();

        try {
            Query = "SELECT Id,TechType FROM TechnicianType";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                EngineerTypes.append("<option value=" + rset.getString(1) + ">" + rset.getString(2));
            }

            rset.close();
            stmt.close();

            Query = "SELECT Id,UserName FROM SystemUsers";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                UserNames.append("<option value=" + rset.getString(1) + ">" + rset.getString(2));
            }

            rset.close();
            stmt.close();

        } catch (Exception e) {
            out.println("Error1" + e.getMessage());
        }
        try {
            Query = "SELECT m.Id,m.UserName,m.UserId,m.Password,t.TechType," +
                    "CASE m.Status " +
                    "WHEN 0 THEN 'Active' " +
                    "WHEN 1 THEN 'InActive' " +
                    "ELSE 'Not Applicable' " +
                    "END,t.Id,m.UserName " +
                    "FROM MobileUsers m " +
                    "LEFT JOIN TechnicianType t ON m.TechType = t.Id " +
                    " ORDER BY m.CreatedDate DESC";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                DisplayTable.append("<tr>");
                DisplayTable.append("<td>" + srlno + "</td>");
                DisplayTable.append("<td>" + rset.getString(8) + "</td>");
                DisplayTable.append("<td>" + rset.getString(3) + "</td>");
                //DisplayTable.append("<td>" + rset.getString(4) + "</td>");
                DisplayTable.append("<td>" + rset.getString(5) + "</td>");
                DisplayTable.append("<td>" + rset.getString(6) + "</td>");
                DisplayTable.append("<td align=center><a class=\"btn-sm btn btn-info\" href=/TAMAPP/TAMAPP.MobileUsers?ActionID=Edit&Indexptr=" + rset.getInt(1) + " target=NewFrame1><i class=\"fa fa-edit\"></i> [Edit] </a></td>");
                DisplayTable.append("<td align=center><button class='btn-sm btn btn-danger mylink' value=" + rset.getInt(1) + " target=NewFrame1><i class=\"fa fa-warning \"></i>[Delete]</button> </td>");
                DisplayTable.append("</tr>");

                srlno++;
            }

            rset.close();
            stmt.close();

            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("EngineerTypes", EngineerTypes.toString());
            Parser.SetField("UserNames", UserNames.toString());
            Parser.SetField("DisplayTable", DisplayTable.toString());
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/MobileUsers.html");

        } catch (Exception e) {
            out.println("Error2" + e.getMessage());
        }
    }

    private void SaveRecords(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        PreparedStatement pStmt = null;

        String UserName = request.getParameter("UserName").trim();
        String UserId1 = request.getParameter("UserId").trim();
        String Password = request.getParameter("Password").trim();
        String EngineerType = request.getParameter("Engineer");
        String Status = request.getParameter("Status").trim();

        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO MobileUsers(UserName,UserId,Password,Status,CreatedDate,TechType,CreatedBy) " +
                            "VALUES(?,?,?,?,now(),?,?)");

            pStmt.setString(1, UserName);
            pStmt.setString(2, UserId1);
            pStmt.setString(3, Password);
            pStmt.setString(4, Status);
            pStmt.setString(5, EngineerType);
            pStmt.setString(6, UserId);

            pStmt.executeUpdate();
            pStmt.close();

            Parsehtm parser = new Parsehtm(request);
            parser.SetField("FormName", "MobileUsers");
            parser.SetField("ActionID", "GETINPUT");
            parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
            out.flush();
            out.close();

        } catch (Exception e) {
            out.println("Error1" + e.getMessage());
        }
    }

    private void ShowEditInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        String Query = "";
        Statement stmt = null;
        ResultSet rset = null;

        String TechIndex = request.getParameter("Indexptr").trim();
        int _TIndex = Integer.parseInt(TechIndex);

        StringBuilder EngineerTypes = new StringBuilder();
        StringBuilder Status = new StringBuilder();
        String UserName = "";
        int _Status = 0;
        int EngType = 0;

        String Passwords = "";
        String UserIds = "";

        try {
            Query = "SELECT a.Id,a.UserName,a.UserId,a.Password,a.TechType,a.JobFlag,b.TechType,a.Status " +
                    "FROM MobileUsers a  " +
                    "STRAIGHT_JOIN TechnicianType b ON a.TechType = b.Id " +
                    "where a.Id='" + _TIndex + "'";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                UserName = rset.getString(2).trim();
                UserIds = rset.getString(3);
                Passwords = rset.getString(4);
                EngType = rset.getInt(5);
                _Status = rset.getInt(8);
            }
            rset.close();
            stmt.close();

            Query = "SELECT Id,TechType FROM TechnicianType ";

            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                if (rset.getInt(1) == EngType)
                    EngineerTypes.append("<option value=" + rset.getString(1) + " Selected>" + rset.getString(2) + "</option>");
                else
                    EngineerTypes.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2));
            }

            rset.close();
            stmt.close();


            Status.append("<div class=form-group>");
            Status.append("<label class='col-sm-3 control-label'>Status</label>");
            Status.append("<div class='col-sm-5'>");
            Status.append("<select class='form-control m-b' name=Status id=Status>");
            if (_Status == 1) {
                Status.append(" <option value=0> Active</option>");
                Status.append("<option value=1 Selected> Inactive</option>");
            } else {
                Status.append(" <option value=0 Selected> Active</option>");
                Status.append("<option value=1 > Inactive</option>");
            }
            Status.append(" </select>");
            Status.append("</div>");
            Status.append("</div>");


            Parsehtm Parser = new Parsehtm(request);
            Parser.SetField("EngineerTypes", EngineerTypes.toString());
            Parser.SetField("Status", Status.toString());
            Parser.SetField("UserNames", UserName);
            Parser.SetField("UserIds", UserIds);
            Parser.SetField("Passwords", Passwords);
            Parser.SetField("_Index", String.valueOf(_TIndex));
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "EditForms/EditMobileUsers.html");
        } catch (Exception e) {
            out.println("Error1" + e.getMessage());
        }
    }

    private void UpdateMobileUsers(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {
        String Query = "";
        Statement stmt = null;
        ResultSet rset = null;

        String Indexptr = request.getParameter("Indexptr").trim();
        int _Index = Integer.parseInt(Indexptr);

        String UserName = request.getParameter("UserName").trim();
        String UserId1 = request.getParameter("UserId").trim();
        String Passwords = request.getParameter("Password").trim();
        String EngineerType = request.getParameter("Engineer");
        String Status = request.getParameter("Status").trim();
        int Status2 = Integer.parseInt(Status);

        try {

            Query = "UPDATE MobileUsers SET UserName='" + UserName + "',UserId='" + UserId1 + "',Password='" + Passwords + "'," +
                    "TechType='" + EngineerType + "',Status='" + Status2 + "' WHERE Id=" + _Index;
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            Parsehtm parser = new Parsehtm(request);
            parser.SetField("FormName", "MobileUsers");
            parser.SetField("ActionID", "GETINPUT");
            parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
            out.flush();
            out.close();

        } catch (Exception e) {
            out.println("Error1" + e.getMessage());
        }
    }

    private void DeleteInfo(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext, String UserId) {

        String Query;
        Statement stmt;

        String Indexptr = request.getParameter("Indexptr").trim();
        int _Index = Integer.parseInt(Indexptr);

        try {
            Query = "Delete from MobileUsers WHERE Id=" + _Index;

            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            Parsehtm parser = new Parsehtm(request);
            parser.SetField("FormName", "MobileUsers");
            parser.SetField("ActionID", "GETINPUT");
            parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Success.html");
            out.flush();
            out.close();

        } catch (Exception e) {
            out.println("Error1" + e.getMessage());
        }
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
