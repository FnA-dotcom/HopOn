package TAMAPP;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@SuppressWarnings("Duplicates")
public class EventsGeneration extends HttpServlet {
    private CallableStatement cStmt = null;
    private ResultSet rset1 = null;
    private String Query1 = "";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
//        PrintWriter out = new PrintWriter(response.getOutputStream());
        //content type must be set to text/event-stream
        response.setContentType("text/event-stream, charset=UTF-8");
        //cache must be set to no-cache
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "-1"); // Proxies.
//        //encoding is set to UTF-8
//        request.setCharacterEncoding("UTF-8");

        try {
            Supportive supp = new Supportive();
            String connect_string = supp.GetConnectString();
            PrintWriter writer = response.getWriter();
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var14) {
                conn = null;
                System.out.println("Exception excp conn: " + var14.getMessage());
            }


            int Online = 0;
            int LogOut = 0;
            int Offline = 0;
            try {
                Query1 = "{CALL TotalOnline()}";
                cStmt = conn.prepareCall(Query1);
                rset1 = cStmt.executeQuery();
                if (rset1.next()) {
                    Online = rset1.getInt(1);
                }
                rset1.close();
                cStmt.close();

                Query1 = "{CALL TotalLogOut()}";
                cStmt = conn.prepareCall(Query1);
                rset1 = cStmt.executeQuery();
                if (rset1.next()) {
                    LogOut = rset1.getInt(1);
                }
                rset1.close();
                cStmt.close();

                Query1 = "{CALL TotalOffline()}";
                cStmt = conn.prepareCall(Query1);
                rset1 = cStmt.executeQuery();
                if (rset1.next()) {
                    Offline = rset1.getInt(1);
                }
                rset1.close();
                cStmt.close();

                if (Offline > 0)
                    LogOut = LogOut - Offline;

                int UnAssignedJobs = 0;
                Query1 = "{CALL UnAssignedJobs()}";
                cStmt = conn.prepareCall(Query1);
                rset1 = cStmt.executeQuery();
                if (rset1.next()) {
                    UnAssignedJobs = rset1.getInt(1);
                }
                rset1.close();
                cStmt.close();

                int AssignedJobs = 0;
                Query1 = "{CALL AssignedJobs()}";
                cStmt = conn.prepareCall(Query1);
                rset1 = cStmt.executeQuery();
                if (rset1.next()) {
                    AssignedJobs = rset1.getInt(1);
                }
                rset1.close();
                cStmt.close();

                int TotalTech = 0;
                Query1 = "{CALL TotalTechs()}";
                cStmt = conn.prepareCall(Query1);
                rset1 = cStmt.executeQuery();
                if (rset1.next()) {
                    TotalTech = rset1.getInt(1);
                }
                rset1.close();
                cStmt.close();

                int LoginCount = 0;
                String TechId = "";
                String TechName = "";
                int TAssigned = 0;
                int TPicked = 0;
                int TCompleted = 0;
                int TPospond = 0;
                int TCanceled = 0;
                String TechnicianLocationDate = "";
                int Difference = 0;
                int CurrDateCheck = 0;
                String Query2 = "";
                Statement stmt2 = null;
                ResultSet rset2 = null;
                StringBuilder TotalTechStatus = new StringBuilder();

                TotalTechStatus.append("<div class=table-responsive>");
                TotalTechStatus.append("<table id=\"example\" class=\"table table-hover no-margins display nowrap dataTables-example\">");
                TotalTechStatus.append("<thead>");
                TotalTechStatus.append("<tr>");
                TotalTechStatus.append("<th>Technician</th>");
                TotalTechStatus.append("<th><span class=\"label label-info\">Queue</span></th>");
                TotalTechStatus.append("<th><span class=\"label label-success\">Picked</span></th>");
                TotalTechStatus.append("<th><span class=\"label label-primary\">Completed</span></th>");
                TotalTechStatus.append("<th><span class=\"label label-warning\">Postponed</span></th>");
                TotalTechStatus.append("<th><span class=\"label label-danger\">Canceled</span></th>");
                TotalTechStatus.append("<th><span class=\"label label-info\">Technician Status</span></th>");
                TotalTechStatus.append("<th><span class=\"label label-info\">Location Date</span></th>");
                TotalTechStatus.append("</tr>");
                TotalTechStatus.append("</thead>");
                TotalTechStatus.append("<tbody>");

                Query1 = "{CALL TotalTechStatus()}";
                cStmt = conn.prepareCall(Query1);
                rset1 = cStmt.executeQuery();
                while (rset1.next()) {
                    TechId = rset1.getString(8);
                    TechName = rset1.getString(2).trim();
                    TAssigned = rset1.getInt(3);
                    TPicked = rset1.getInt(4);
                    TCompleted = rset1.getInt(5);
                    TPospond = rset1.getInt(6);
                    TCanceled = rset1.getInt(7);

                    Query2 = " SELECT count(*)  FROM LoginTrail " +
                            " WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + rset1.getString(8) + "')) AND " +
                            " UserType = 'M' ";
                    stmt2 = conn.createStatement();
                    rset2 = stmt2.executeQuery(Query2);
                    if (rset2.next())
                        LoginCount = rset2.getInt(1);
                    rset2.close();
                    stmt2.close();

                    TechnicianLocationDate = "";
                    Difference = 0;
                    CurrDateCheck = 0;
                    Query2 = "SELECT DATE_FORMAT(CreatedDate,'%d-%m-%Y %H:%i:%s'),TIMESTAMPDIFF(MINUTE,CreatedDate, NOW() ) as difference," +
                            " CASE WHEN  DATE_FORMAT(CreatedDate,'%d-%m-%Y') = DATE_FORMAT(NOW(),'%d-%m-%Y') THEN 1 ELSE 0 END  " +
                            " FROM TechnicianLocation WHERE UserId='" + rset1.getString(8) + "' AND " +
                            " CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId='" + rset1.getString(8) + "') ";
                    stmt2 = conn.createStatement();
                    rset2 = stmt2.executeQuery(Query2);
                    if (rset2.next()) {
                        TechnicianLocationDate = rset2.getString(1);
                        Difference = rset2.getInt(2);
                        CurrDateCheck = rset2.getInt(3);
                    }
                    rset2.close();
                    stmt2.close();

                    TotalTechStatus.append("<tr>");

                    //within 45 min and current date
                    if (Difference < 46 && CurrDateCheck == 1) {
                        TotalTechStatus.append("<td  bgcolor=#befca4 align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + TechId + " \" target=\"_self\"> <small>" + TechName + "</small> </a></td>");

                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-info\">" + TAssigned + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-success\">" + TPicked + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-primary\">" + TCompleted + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-warning\">" + TPospond + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-danger\">" + TCanceled + "</span></td>");
                        if (LoginCount == 0)
                            TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center \"><img src=/TAMAPP/images/OfflineImage.png width=20 height=10 ><br></td>");// Status
                        else
                            TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center \" ><img src=/TAMAPP/images/OnlineImage.png width=20 height=10 ><br></td>");// Status

                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center small\" >" + TechnicianLocationDate + "</span></td>");

                    }
                    //Greater than 45 min and current date
                    else if (Difference > 46 && CurrDateCheck == 1) {
                        TotalTechStatus.append("<td bgcolor=#fafca4 align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + TechId + " \" target=\"_self\"> <small>" + TechName + "</small> </a></td>");

                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-info\">" + TAssigned + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-success\">" + TPicked + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-primary\">" + TCompleted + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-warning\">" + TPospond + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-danger\">" + TCanceled + "</span></td>");
                        if (LoginCount == 0)
                            TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center \"><img src=/TAMAPP/images/OfflineImage.png width=20 height=10 ><br></td>");// Status
                        else
                            TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center \" ><img src=/TAMAPP/images/OnlineImage.png width=20 height=10 ><br></td>");// Status

                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center small\" >" + TechnicianLocationDate + "</span></td>");
                    }
                    //Less then Curr Date
                    else {
                        TotalTechStatus.append("<td bgcolor=#f47a7a align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + TechId + " \" target=\"_self\"> <small>" + TechName + "</small> </a></td>");

                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-info\">" + TAssigned + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-success\">" + TPicked + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-primary\">" + TCompleted + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-warning\">" + TPospond + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-danger\">" + TCanceled + "</span></td>");
                        if (LoginCount == 0)
                            TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center \"><img src=/TAMAPP/images/OfflineImage.png width=20 height=10 ><br></td>");// Status
                        else
                            TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center \" ><img src=/TAMAPP/images/OnlineImage.png width=20 height=10 ><br></td>");// Status

                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center small\" >" + TechnicianLocationDate + "</span></td>");
                    }

                    //For Row wise
/*                    //within 45 min and current date
                    if (Difference < 46 && CurrDateCheck == 1) {
                        TotalTechStatus.append("<tr>");
                        TotalTechStatus.append("<td bgcolor=#befca4 align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + TechId + " \" target=\"_self\"> <small>" + TechName + "</small> </a></td>");

                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-info\">" + TAssigned + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-success\">" + TPicked + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-primary\">" + TCompleted + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-warning\">" + TPospond + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center medium\" ><span class=\"label label-danger\">" + TCanceled + "</span></td>");
                        if (LoginCount == 0)
                            TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center \"><img src=/TAMAPP/images/OfflineImage.png width=20 height=10 ><br></td>");// Status
                        else
                            TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center \" ><img src=/TAMAPP/images/OnlineImage.png width=20 height=10 ><br></td>");// Status
                        TotalTechStatus.append("<td bgcolor=#befca4 class=\"text-center small\" ><span class=\"label label-info\">" + TechnicianLocationDate + "</span></td>");
                        TotalTechStatus.append("</tr>");
                    }
                    //Greater than 45 min and current date
                    else if (Difference > 46 && CurrDateCheck == 1) {
                        TotalTechStatus.append("<tr>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + TechId + " \" target=\"_self\"> <small>" + TechName + "</small> </a></td>");

                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-info\">" + TAssigned + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-success\">" + TPicked + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-primary\">" + TCompleted + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-warning\">" + TPospond + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center medium\" ><span class=\"label label-danger\">" + TCanceled + "</span></td>");
                        if (LoginCount == 0)
                            TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center \"><img src=/TAMAPP/images/OfflineImage.png width=20 height=10 ><br></td>");// Status
                        else
                            TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center \" ><img src=/TAMAPP/images/OnlineImage.png width=20 height=10 ><br></td>");// Status
                        TotalTechStatus.append("<td bgcolor=#fafca4 class=\"text-center small\" ><span class=\"label label-info\">" + TechnicianLocationDate + "</span></td>");
                        TotalTechStatus.append("</tr>");
                    }
                    //Less then Curr Date
                    else {
                        TotalTechStatus.append("<tr>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a align='left' data-container='body' data-toggle='tooltip' data-placement='left' title= 'Click to Find Technician Location' ><a href = \"/TAMAPP/TAMAPP.ManualAssignment?ActionID=GetTechLocation&TechId=" + TechId + " \" target=\"_self\"> <small>" + TechName + "</small> </a></td>");

                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-info\">" + TAssigned + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-success\">" + TPicked + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-primary\">" + TCompleted + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-warning\">" + TPospond + "</span></td>");
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center medium\" ><span class=\"label label-danger\">" + TCanceled + "</span></td>");
                        if (LoginCount == 0)
                            TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center \"><img src=/TAMAPP/images/OfflineImage.png width=20 height=10 ><br></td>");// Status
                        else
                            TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center \" ><img src=/TAMAPP/images/OnlineImage.png width=20 height=10 ><br></td>");// Status
                        TotalTechStatus.append("<td bgcolor=#f47a7a class=\"text-center small\" ><span class=\"label label-info\">" + TechnicianLocationDate + "</span></td>");
                        TotalTechStatus.append("</tr>");
                    }*/
                    TotalTechStatus.append("</tr>");
                }
                rset1.close();
                cStmt.close();

                TotalTechStatus.append("</tbody>");
                TotalTechStatus.append("<tfoot align=\"right\">");
                TotalTechStatus.append("<tr>");
                TotalTechStatus.append("<th></th>");
                TotalTechStatus.append("<th></th>");
                TotalTechStatus.append("<th></th>");
                TotalTechStatus.append("<th></th>");
                TotalTechStatus.append("<th></th>");
                TotalTechStatus.append("<th></th>");
                TotalTechStatus.append("<th></th>");
                TotalTechStatus.append("</tr>");
                TotalTechStatus.append("</tfoot>");
                TotalTechStatus.append("</table>");
                TotalTechStatus.append("</div>");

                writer.write("data: " + Online + "|" + LogOut + "|" + Offline + "|" + UnAssignedJobs + "|" + AssignedJobs + "|" + TotalTech + "|" + TotalTechStatus.toString() + "\n\n");
                writer.flush();
                writer.close();

            } catch (Exception ex) {
                System.out.println("ERROR IS " + ex.getMessage());
            }
            conn.close();
        } catch (Exception var15) {
            System.out.println("Exception in main... " + var15.getMessage());
            System.out.flush();
            System.out.close();
            return;
        }

        System.out.flush();
        System.out.close();
    }
}
