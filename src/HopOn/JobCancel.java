package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class JobCancel extends HttpServlet {
    PreparedStatement pStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();
        try {
            ServletContext context = null;
            context = getServletContext();

            try {
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
            if (Action.compareTo("MarkCancelled") == 0) {
                MarkCancelled(request, response, out, conn, context);
            } else {
                out.println("Under Development ... " + Action);
            }
            conn.close();

            out.close();
            out.flush();
        } catch (Exception Ex) {
            out.println("Exception in main... " + Ex.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private void MarkCancelled(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext context) {

        try {
            stmt = null;
            rset = null;
            Query = "";

            String RegNumber = request.getParameter("RegNumber");
            Query = request.getParameter("JobType").trim();
            int JobType = Integer.parseInt(Query);
            int MaxID = -1;
            int MaxAssignmentIndex = 0;
            int ComplaintStatus = -1;
            if (JobType == 2) {

//			   getting Latest ID
                Query = "SELECT MAX(Id) FROM CustomerData WHERE  UPPER(RegistrationNum) = UPPER('" + RegNumber + "')  and JobTypeIndex = " + JobType;
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next()) {
                    MaxID = rset.getInt(1);
                }
                rset.close();
                stmt.close();
//	         latest status , not equals to 6
                if (MaxID != -1 && MaxID == 0) {
                    out.print("Registration number not found.");
                    return;
                } else {
                    //Fetching Max Result from Assignment Table
                    Query = "SELECT MAX(Id) FROM Assignment WHERE ComplaintId = " + MaxID;
                    stmt = conn.createStatement();
                    rset = stmt.executeQuery(Query);
                    if (rset.next())
                        MaxAssignmentIndex = rset.getInt(1);
                    rset.close();
                    stmt.close();

                    Query = "SELECT ComplaintStatus FROM Assignment WHERE ComplaintId = " + MaxAssignmentIndex;
                    stmt = conn.createStatement();
                    rset = stmt.executeQuery(Query);
                    if (rset.next())
                        ComplaintStatus = rset.getInt(1);
                    rset.close();
                    stmt.close();

                    //**********************************TABISH*********************************************************
                    //26th Dec 2018
                    //Complaint Status check
                    //if job's current complaint status is not picked then it will mark it else cancelled else it will throw an error
                    if (ComplaintStatus == 0) {
                        Query = "SELECT Id,UserId,ComplaintId,Status,CreatedDate,RegisterBy,ComplaintStatus,AssignmentStatus,Latitude," +
                                "Longtitude,ManualStatus,Distance,TotalTime,FalconRadius,TransferFlag,ManualDate,TransferDate," +
                                "isSameLocation,CancelReAliveDate,CityIndex,CancelledRemarks FROM Assignment " +
                                "WHERE ComplaintId= " + MaxID + " AND ComplaintStatus <> 6 ";
                        stmt = conn.createStatement();
                        rset = stmt.executeQuery(Query);
                        if (rset.next()) {
                            pStmt = conn.prepareStatement(
                                    "INSERT INTO AssignmentHistory (UserId,ComplaintId,Status,CreatedDate,RegisterBy," +
                                            "ComplaintStatus,AssignmentStatus,Latitude,Longtitude,ManualStatus,Distance," +
                                            "FalconRadius,TransferFlag,ManualDate,TransferDate,CityIndex) " +
                                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
                            pStmt.setString(1, rset.getString(2));
                            pStmt.setString(2, rset.getString(3));
                            pStmt.setString(3, rset.getString(4));
                            pStmt.setString(4, rset.getString(5));
                            pStmt.setString(5, rset.getString(6));
                            pStmt.setString(6, rset.getString(7));
                            pStmt.setString(7, rset.getString(8));
                            pStmt.setString(8, rset.getString(9));
                            pStmt.setString(9, rset.getString(10));
                            pStmt.setString(10, rset.getString(11));
                            pStmt.setString(11, rset.getString(12));
                            pStmt.setString(12, rset.getString(14));
                            pStmt.setString(13, rset.getString(15));
                            pStmt.setString(14, rset.getString(16));
                            pStmt.setString(15, rset.getString(17));
                            pStmt.setString(16, rset.getString(20));
                            pStmt.executeUpdate();
                            pStmt.close();


                            pStmt = conn.prepareStatement(
                                    "INSERT INTO ApiChange (Id,UserId,ComplaintId,Status,CreatedDate,RegisterBy," +
                                            "ComplaintStatus,AssignmentStatus,Latitude,Longtitude,ManualStatus,Distance," +
                                            "TotalTime,FalconRadius,TransferFlag,ManualDate,TransferDate,isSameLocation," +
                                            "CancelReAliveDate,CityIndex,CancelledRemarks,ChangeCreatedDate,ChangeRegNumber," +
                                            "ChangeJobType) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now(),?,?) ");
                            pStmt.setString(1, rset.getString(1));
                            pStmt.setString(2, rset.getString(2));
                            pStmt.setString(3, rset.getString(3));
                            pStmt.setString(4, rset.getString(4));
                            pStmt.setString(5, rset.getString(5));
                            pStmt.setString(6, rset.getString(6));
                            pStmt.setString(7, rset.getString(7));
                            pStmt.setString(8, rset.getString(8));
                            pStmt.setString(9, rset.getString(9));
                            pStmt.setString(10, rset.getString(10));
                            pStmt.setString(11, rset.getString(11));
                            pStmt.setString(12, rset.getString(12));
                            pStmt.setString(13, rset.getString(13));
                            pStmt.setString(14, rset.getString(14));
                            pStmt.setString(15, rset.getString(15));
                            pStmt.setString(16, rset.getString(16));
                            pStmt.setString(17, rset.getString(17));
                            pStmt.setString(18, rset.getString(18));
                            pStmt.setString(19, rset.getString(19));
                            pStmt.setString(20, rset.getString(20));
                            pStmt.setString(21, rset.getString(21));
                            pStmt.setString(22, RegNumber);
                            pStmt.setInt(23, JobType);
                            pStmt.executeUpdate();
                            pStmt.close();

                            // update status to 8
                            Query = "UPDATE Assignment SET ComplaintStatus = 8 WHERE ComplaintId = " + MaxID + "";
                            stmt = conn.createStatement();
                            stmt.executeUpdate(Query);
                            rset.close();
                            stmt.close();

                            out.println(RegNumber + " is updated.");
                        }
                        rset.close();
                        stmt.close();
                    } //ComplaintStatus Check
                    else {
                        out.println("Technician is Already on this Job.");
                        return;
                    }
                }
            } else {
                out.println("Job Type must be Redo.");
                return;
            }
        } catch (Exception e) {
            out.print(e.getMessage());
        }
    }
}
