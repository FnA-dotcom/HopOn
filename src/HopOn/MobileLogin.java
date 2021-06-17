package HopOn;

/**
 * Created by Siddiqui on 9/19/2017.
 */

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class MobileLogin extends HttpServlet {
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";
    private PreparedStatement pStmt = null;
    private String LogString = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection conn = null;
        String ActionID = request.getParameter("ActionID").trim();

        PrintWriter out = new PrintWriter(response.getOutputStream());

        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();

        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            conn = DriverManager.getConnection(connect_string);
        } catch (Exception excp) {
            conn = null;
            out.println("Exception excp conn: " + excp.getMessage());
        }

        if (ActionID.equals("LoginHelper")) {
            LoginHelper(request, out, conn);
        } else if (ActionID.equals("StatusMark")) {
            MarkStatus(request, out, conn);
        } else if (ActionID.equals("LogOut")) {
            MobileLogOut(request, out, conn);
        } else if (ActionID.equals("MobileOnline")) {
            MobileOnline(request, out, conn);
        } else if (ActionID.equals("MobileOffline")) {
            MobileOffline(request, out, conn);
        }
        try {
            assert conn != null;
            conn.close();
        } catch (Exception ignored) {
        }

        out.flush();
        out.close();
    }

    private void LoginHelper(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = "";
        int found = 0;
        int LoginCount = 0;
        String appVersion = "";
        String UserIP = request.getRemoteAddr();
        String UserId = request.getParameter("Name").trim();
        UserId = UserId.replace("-", "");
        String Password = request.getParameter("Password");
        String MobileAppVersion = request.getParameter("version");
        String passwordEnc = "";
        String passwordDec = "";

//        out.println("Password " + Password + "<br>");
        try {
            passwordEnc = Login.encrypt(Password);
            passwordDec = Login.decrypt(passwordEnc);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        out.println("passwordEnc " + passwordEnc + "<br>");
//        out.println("passwordDec " + passwordDec + "<br>");

        //Status 0 means active
        //Status 1 measns inactive
        Query = " Select count(*),UserName from MobileUsers where upper(trim(UserId))='" + UserId.trim() + "' " +
                " and Password='" + passwordEnc + "' and Status=0 ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                found = rset.getInt(1);
            rset.close();
            stmt.close();

            if (found > 0) {
                Query = "SELECT AppVersion FROM AppVersion";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next())
                    appVersion = rset.getString(1).trim();
                rset.close();
                stmt.close();

                if (MobileAppVersion.compareToIgnoreCase(appVersion) != 0) {
                    Supportive.doLogMethodMessage(this.getServletContext(), "Login Method ", "Version Exception");
                    out.println("Exception :  You are using an outdated Mobile App. Current Version is  " + appVersion + " ");
                    out.flush();
                    out.close();
                    return;
                } else {
                    out.println("true|" + UserId);
                    TechnicianActivity(out, conn, UserId, "LogIn");
                    try {
                        Query = "SELECT  count(*) FROM LoginTrail WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + UserId + "')) AND UserType='M'";
                        stmt = conn.createStatement();
                        rset = stmt.executeQuery(Query);
                        if (rset.next())
                            LoginCount = rset.getInt(1);
                        rset.close();
                        stmt.close();

                        int CityIndex = CityIndex(conn, UserId);

                        if (LoginCount == 0) {
                            pStmt = conn.prepareStatement(
                                    "INSERT INTO LoginTrail(UserId, UserType, IP, Status, CreatedDate,CityIndex) " +
                                            "VALUES(?,?,?,0,now(),?)");

                            pStmt.setString(1, UserId);
                            pStmt.setString(2, "M");
                            pStmt.setString(3, UserIP);
                            pStmt.setInt(4, CityIndex);

                            pStmt.executeUpdate();
                            pStmt.close();
                        } else {
                            Query = "UPDATE  LoginTrail SET IP = '" + UserIP + "',CreatedDate=NOW() " +
                                    " WHERE ltrim(rtrim(UserId)) = '" + UserId + "' ";
                            stmt = conn.createStatement();
                            stmt.executeUpdate(Query);
                            stmt.close();
                        }
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        Supportive.doLog(this.getServletContext(), "Login Trail First Exception", e.getMessage(), e);
                        out.println("Error1" + e.getMessage());
                        out.flush();
                        out.close();
                        return;
                    }

                }
            } else {
                Supportive.doLogMethodMessage(this.getServletContext(), "Login Method - Credential Error", "InCorrect Credentials. Please Enter Correct Credentials!!");
                out.println("Exception :  InCorrect Credentials. Please Enter Correct Credentials!! ");
                out.flush();
                out.close();
                return;
            }
        } catch (Exception e) {
            Supportive.doLogMethodMessage(this.getServletContext(), "Login Method - User Not Exist", "Exception :  Invalid UserId or password");
            out.println("Exception :  Invalid UserId or password ");
            out.flush();
            out.close();
            return;
        }
    }

    private void MarkStatus(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;
        String UserId = request.getParameter("Name").trim();
        String Status = request.getParameter("Status").trim();
        Status = Status.replace("-", " ");
        String ComplainNumber = request.getParameter("UniqueID").trim();
        ComplainNumber = ComplainNumber.replace("^", "#");
        String Lat = "";
        String Lon = "";
        String CancelledRemarks = null;
        String QueryPatch = "";
        String SendComplainStatusFalcon = "";
        if (Status.equals("Job Completed") || Status.equals("Job Postponed") || Status.equals("Job Cancelled")) {
            Lat = request.getParameter("Lat").trim();
            //Lat = Lat.substring(0, 9);
            Lon = request.getParameter("Lon").trim();
            //Lon = Lon.substring(0, 9);
            QueryPatch = "";
            if (Status.equals("Job Cancelled")) {
                if (request.getParameter("Reason") == null)
                    CancelledRemarks = "-";
                else
                    CancelledRemarks = request.getParameter("Reason").trim();
            }
        } else {
            Lat = "-";
            Lon = "-";
        }
        int ComplainStatusId = 0;
        int AssignmentStatus = 0;
        int CustomerDataId = 0;
        int TechId = 0;
        int ComplainStatusCount = 0;
        LogString += " \n *********************************************************************  \n ";

        try {
            Query = "SELECT Id,StatusFlag,count(*) FROM ComplaintStatus WHERE " +
                    " upper(trim(ComplaintStatus))='" + Status.toUpperCase().trim() + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                ComplainStatusId = rset.getInt(1);
                AssignmentStatus = rset.getInt(2);
                ComplainStatusCount = rset.getInt(3);
            }
            rset.close();
            stmt.close();

            if (ComplainStatusCount == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Mark Status - First Check", "No Status Found.Please try again!!");
                out.println("No Status Found.Please try again!!");
                out.close();
                out.flush();
                return;
            }

            Query = "SELECT Id FROM MobileUsers WHERE upper(trim(UserId))='" + UserId.toUpperCase().trim() + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                TechId = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (TechId == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Mark Status - Second Check", "No User Record Found.Please try again!!");
                out.println("No User Record Found.Please try again!!");
                out.close();
                out.flush();
                return;
            }

            String JobType = "";
            String RegistrationNum = "";
            String ChassisNo = "";
            String EngineNo = "";
            String JobStatus = "";
            int CityIndex = 0;
            Query = "SELECT  a.Id,b.JobType,a.RegistrationNum,a.ChesisNo,IFNULL(a.CityIndex,0) FROM CustomerData a " +
                    "STRAIGHT_JOIN JobType b ON a.JobTypeIndex = b.Id " +
                    "WHERE a.ComplainNumber='" + ComplainNumber + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                CustomerDataId = rset.getInt(1);
                JobType = rset.getString(2).trim();
                RegistrationNum = rset.getString(3).trim();
                ChassisNo = rset.getString(4).trim();
                CityIndex = rset.getInt(5);
            }
            rset.close();
            stmt.close();

            if (CustomerDataId == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Mark Status - Third Check", "No Complain Found.Please try again!!");
                out.println("No Complain Found.Please try again!!");
                out.close();
                out.flush();
                return;
            }


//
//            Query = "SELECT  UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus,AssignmentStatus," +
//                    "Latitude,Longtitude,ManualStatus,Distance,FalconRadius,TransferFlag " +
//                    " FROM  Assignment " +
//                    " WHERE ComplaintId='" + ComplainNumber + "' AND UserId=" + TechId;
//            stmt = conn.createStatement();
//            rset = stmt.executeQuery(Query);
//            if (rset.next()) {
//                pStmt = conn.prepareStatement(
//                        "INSERT INTO AssignmentHistory(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
//                                "AssignmentStatus,Latitude,Longtitude,ManualStatus,Distance,FalconRadius,TransferFlag) " +
//                                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
//
//                pStmt.setInt(1, rset.getInt(1));
//                pStmt.setInt(2, rset.getInt(2));
//                pStmt.setInt(3, rset.getInt(3));
//                pStmt.setString(4, rset.getString(4));
//                pStmt.setString(5, rset.getString(5));
//                pStmt.setString(6, rset.getString(6));
//                pStmt.setString(7, rset.getString(7));
//                pStmt.setString(8, rset.getString(8));
//                pStmt.setString(9, rset.getString(9));
//                pStmt.setInt(10, rset.getInt(10));
//                pStmt.setString(11, rset.getString(11));
//                pStmt.setString(12, rset.getString(12));
//                pStmt.setInt(13, rset.getInt(13));
//                pStmt.executeUpdate();
//                pStmt.close();
//            }
//            rset.close();
//            stmt.close();


            //Status Assigmment History
            //In Mobile: Status= 2
            //In Auotmatic Scheduler: Status= 1
            //In Manual Status: Status= 4
            //In TransferJob Status: Status= 3
            int AssignmentIndex = 0;
            Query = "SELECT Max(Id) FROM Assignment WHERE ComplaintId = " + CustomerDataId;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                AssignmentIndex = rset.getInt(1);
            rset.close();
            stmt.close();

            pStmt = conn.prepareStatement(
                    "INSERT INTO AssignmentHistory(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
                            "AssignmentStatus,Latitude,Longtitude,ManualStatus,Distance,FalconRadius,TransferFlag,OldId,CityIndex) " +
                            "VALUES (?,?,2,now(),?,?,?,?,?,0,0,0,0,?,?)");

            pStmt.setInt(1, TechId);
            pStmt.setInt(2, CustomerDataId);
            pStmt.setInt(3, TechId);
            pStmt.setInt(4, ComplainStatusId);
            pStmt.setInt(5, AssignmentStatus);
            pStmt.setString(6, Lat);
            pStmt.setString(7, Lon);
            pStmt.setInt(8, AssignmentIndex);
            pStmt.setInt(9, CityIndex);
            pStmt.executeUpdate();
            pStmt.close();

//            Query = "UPDATE Assignment SET ComplaintStatus=" + ComplainStatusId + ",AssignmentStatus=" + AssignmentStatus + "," +
//                    "Latitude='" + Lat + "',Longtitude='" + Lon + "',CreatedDate = DATE_FORMAT(NOW(),'%Y-%m-%d %I:%h:%s') " +
//                    " WHERE ComplaintId=" + CustomerDataId + " AND UserId=" + TechId;
            Query = "UPDATE Assignment SET ComplaintStatus=" + ComplainStatusId + ",AssignmentStatus=" + AssignmentStatus + "," +
                    "Latitude='" + Lat + "',Longtitude='" + Lon + "',CreatedDate = DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'),CancelledRemarks = '" + CancelledRemarks + "' " +
                    " WHERE Id = " + AssignmentIndex;
            LogString += " \n " + Query + "  \n ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();


            //Sending data to Falcon(external) Server when a job is completed only
            if (ComplainStatusId == 6 || ComplainStatusId == 8 || ComplainStatusId == 7 || ComplainStatusId == 10) {
                if (ComplainStatusId == 6)
                    SendComplainStatusFalcon = "Job-Complete";
                else if (ComplainStatusId == 8)
                    SendComplainStatusFalcon = "Job-Cancelled";
                else if (ComplainStatusId == 7)
                    SendComplainStatusFalcon = "Job-Postponed";
                else if (ComplainStatusId == 10)
                    SendComplainStatusFalcon = "Testing-Phase";
                else
                    SendComplainStatusFalcon = "N/A";
                RegistrationNum = RegistrationNum.replace(" ", "-");
                ChassisNo = ChassisNo.replace(" ", "-");
                ComplainNumber = ComplainNumber.replace("#", "^");

                int ResponseMessage = FalconURL(conn, out, ComplainNumber, JobType, RegistrationNum, ChassisNo, "1234ENGNo", SendComplainStatusFalcon, UserId, TechId, ComplainStatusId, CityIndex);

                //out.println("Response Message " + ResponseMessage);
                LogString += " \n Response Message FROM Falcon URL --> " + String.valueOf(ResponseMessage) + "  \n ";
            }

            out.println("true");
            LogString += " \n *********************************************************************  \n ";
            Supportive.QueryLog(this.getServletContext(), LogString);

            out.flush();
            out.close();
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Mark Status - Fourth Check", e.getMessage(), e);
            out.println("Exception URL " + e.getMessage());
            out.close();
            out.flush();
            return;
        }
    }

    private void MobileLogOut(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("Name").trim();

        try {
            Query = "DELETE FROM LoginTrail WHERE UserId='" + UserId + "' AND UserType='M' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            out.println("true");
            TechnicianActivity(out, conn, UserId, "LogOut");
            out.close();
            out.flush();
            return;

        } catch (Exception Ex) {
            Supportive.doLog(this.getServletContext(), "Mobile Log Out ", Ex.getMessage(), Ex);
            out.println("Exception" + Ex.getMessage());
            out.close();
            out.flush();
            return;
        }
    }

    private void MobileOffline(HttpServletRequest request, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("Name").trim();

        try {
            Query = "DELETE FROM LoginTrail WHERE UserId='" + UserId + "' AND UserType='M' ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            out.println("true");
            TechnicianActivity(out, conn, UserId, "Offline");
            out.close();
            out.flush();
            return;

        } catch (Exception Ex) {
            Supportive.doLog(this.getServletContext(), "Mobile Offline ", Ex.getMessage(), Ex);
            out.println("Exception" + Ex.getMessage());
            out.close();
            out.flush();
            return;
        }
    }

    private void MobileOnline(HttpServletRequest request, PrintWriter out, Connection conn) {
        pStmt = null;
        String UserIP = request.getRemoteAddr();
        String UserId = request.getParameter("Name").trim();
        int LoginCount = 0;
        try {
            TechnicianActivity(out, conn, UserId, "OnlineAgain");

            Query = "SELECT  count(*) FROM LoginTrail WHERE ltrim(rtrim(UserId)) = ltrim(rtrim('" + UserId + "')) AND UserType='M'";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                LoginCount = rset.getInt(1);
            rset.close();
            stmt.close();

            if (LoginCount == 0) {
                int CityIndex = CityIndex(conn, UserId);
                pStmt = conn.prepareStatement(
                        "INSERT INTO LoginTrail(UserId, UserType, IP, Status, CreatedDate,CityIndex) VALUES(?,?,?,0,now(),?)");

                pStmt.setString(1, UserId.trim());
                pStmt.setString(2, "M");
                pStmt.setString(3, UserIP);
                pStmt.setInt(4, CityIndex);

                pStmt.executeUpdate();
                pStmt.close();
            } else {
                Query = "UPDATE  LoginTrail SET IP = '" + UserIP + "',CreatedDate=NOW() " +
                        " WHERE ltrim(rtrim(UserId)) = '" + UserId + "' ";
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            }
            out.flush();
            out.close();
            return;
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Mobile Online ", e.getMessage(), e);
            out.println("Error1" + e.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private String TechnicianActivity(PrintWriter out, Connection conn, String UserId, String LoggedFlag) {
        pStmt = null;
        Query = "";
        String Message = "";
        try {
            int CityIndex = CityIndex(conn, UserId);
            pStmt = conn.prepareStatement(
                    "INSERT INTO TechnicianLoginTrail(TechId,LoggedTime, Status, CreatedDate,LoginFlag,CityIndex) " +
                            "VALUES(?,NOW(),0,now(),?,?)");

            pStmt.setString(1, UserId.trim());
            pStmt.setString(2, LoggedFlag.trim());
            pStmt.setInt(3, CityIndex);
            pStmt.executeUpdate();
            pStmt.close();

            out.flush();
            out.close();
            Message = "Success";
        } catch (Exception Ex) {
            out.println("Exception  = " + Ex.getMessage());
            Message = Ex.getMessage();
        }
        return Message;
    }

    private int CityIndex(Connection conn, String UserId) {
        stmt = null;
        rset = null;
        Query = "";

        int CityIndex = 0;
        try {
            Query = "SELECT CityIndex FROM MobileUsers WHERE UserId = '" + UserId + "' AND Status = 0";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                CityIndex = rset.getInt(1);
            rset.close();
            stmt.close();
        } catch (Exception ignored) {

        }
        return CityIndex;
    }

    private int FalconURL(Connection conn, PrintWriter out, String ComplainNumber, String JobType, String RegistrationNum, String ChassisNum, String EngineNum, String JobStatus, String UserId, int TechId, int ComplainStatusId, int CityIndex) {
        pStmt = null;
        Query = "";
        int ResponseCode = 0;
        String BaseURL = "";
        String reply = "";
        try {

            //XML Creation and Dump into the folder from vibe
            // BaseURL = "http://203.130.0.235/getcustinfo/xmlpro.php?PNR=" + BookingPNR + "&OptionIndex=" + OptionIndex + " ";
            BaseURL = "http://thegpslink.com:85/service1.asmx/JobsCompleted?JOBID=" + ComplainNumber + "&JobType=" + JobType + "&VRN=" + RegistrationNum + "&" +
                    "ChassisNo=" + ChassisNum + "&EngineNo=1234567890eng&JobStatus=" + JobStatus + "&TechnicianID=" + UserId + "";
            URL url = new URL(BaseURL);
            URLConnection uc = url.openConnection();
            uc.setReadTimeout(10000);
            uc.setConnectTimeout(15000);

            uc.setDoInput(true);
            uc.setUseCaches(false);
            uc.setRequestProperty("Content-Type", "application/xml");
            InputStream is = uc.getInputStream();

            int size = is.available();
            byte[] responseByteArray = new byte[size];
            is.read(responseByteArray);
            reply = new String(responseByteArray);
            reply = reply.trim();
            //out.println("Response is " + reply);

            //Table Insertion of all URL.
            //Whether it is a success or not
            pStmt = conn.prepareStatement(
                    "INSERT INTO FalconURLInsertion(UserId, ComplaintId, Status, CreatedDate, URL,CityIndex) " +
                            "VALUES (?,?,0,NOW(),?,?)");

            pStmt.setInt(1, TechId);
            pStmt.setInt(2, ComplainStatusId);
            pStmt.setString(3, BaseURL);
            pStmt.setInt(4, CityIndex);

            pStmt.executeUpdate();
            pStmt.close();


            if (reply.contains("true")) {
                //Table Insertion of URL
                ResponseCode = 1;
            }
        } catch (Exception Ex) {
            try {
                pStmt = conn.prepareStatement(
                        "INSERT INTO FalconURLException (ExceptionMessage, Status, CreatedDate,URL,CityIndex) " +
                                "VALUES (?,0,NOW(),?,?)");
                pStmt.setString(1, Ex.getMessage());
                pStmt.setString(2, BaseURL);
                pStmt.setInt(3, CityIndex);

                pStmt.executeUpdate();
                pStmt.close();
            } catch (Exception ignored) {
            }
            ResponseCode = 0;
            return ResponseCode;
        }
        return ResponseCode;
    }
}
