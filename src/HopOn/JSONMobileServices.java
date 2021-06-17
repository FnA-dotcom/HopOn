package HopOn;

import Parsehtm.Parsehtm;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.List;

@SuppressWarnings("Duplicates")

public class JSONMobileServices extends HttpServlet {
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";
    private PreparedStatement pStmt = null;
    private String LogString = null;
    private CallableStatement cStmt = null;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        //For Printing
        PrintWriter out = new PrintWriter(response.getOutputStream());

        //For Printing JSON Objects
        ServletOutputStream SOS = response.getOutputStream();

        Connection conn = null;
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
        ServletContext context = null;
        context = this.getServletContext();

        try {
            UtilityHelper helper = new UtilityHelper();
            String RequestName = request.getParameter("RequestName").trim();
            switch (RequestName) {
                case "DriverData":
                    DriverData(request, conn, SOS, helper, out, context);
                    break;
                case "RouteWiseRunData":
                    RouteWiseRunData(request, conn, SOS, helper, out, context);
                    break;
                case "getStudentInfoRunWise":
                    StudentInfoRunWise(request, conn, SOS, helper, out, context);
                    break;
                case "SaveDriverInspectionData":
                    SaveDriverInspectionData(request, conn, SOS, helper, out, context);
                    break;
                case "checkDriverInspection":
                    checkDriverInspection(request, conn, SOS, helper, out, context);
                    break;

                case "MarkStatus":
                    MarkStatus(request, conn, SOS);
                    break;
                case "CustomerData":
                    CustomerData(request, conn, SOS);
                    break;
                case "MarkLocation":
                    MarkLocation(request, conn, SOS);
                    break;
                case "InspectionForm":
                    InspectionForm(request, conn, SOS);
                    break;
                case "QAForm":
                    QAForm(request, conn, SOS);
                    break;
                case "SavePicture":
                    SavePicture(request, out);
                    break;
                case "PostponedReasonInsertion":
                    PostponedReasonInsertion(request, conn, SOS);
                    break;
                case "RevertStatus":
                    RevertStatus(request, conn, SOS);
                    break;
                case "UpdateFirebaseToken":
                    UpdateFirebaseToken(request, conn, SOS);
                    break;
                case "getCompleteJobs":
                    getCompleteJobs(request, conn, SOS);
                    break;
                default:
                    out.println("Under Development");
                    break;
            }
        } catch (Exception Ex) {
            out.println("Error in Main " + Ex.getMessage());
        }
        out.close();
        out.flush();
    }

    /********************** HOP ON *************************************/

    private void DriverData(HttpServletRequest request, Connection conn, ServletOutputStream SOS, UtilityHelper helper, PrintWriter out, ServletContext context) {
        cStmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();

        int RunCount = 0;
        int StdCount = 0;
        double TotalRouteDistance = 0.0;

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        int SystemUserIndex = helper.MobileUserIndex(request, UserId, conn, getServletContext());
        try {
            Query = "{CALL MobileDataAssignment(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, SystemUserIndex);
            rset = cStmt.executeQuery();
            while (rset.next()) {
                RunCount = helper.RunCount(request, rset.getInt(1), conn, context);
                StdCount = helper.StudentCountRouteWise(request, rset.getInt(1), conn, context);
                TotalRouteDistance = helper.getRouteWiseDistance(request, rset.getInt(1), conn, context);

                JSONObject jsonObj = new JSONObject();

                jsonObj.put("RouteIndex", rset.getInt(1));
                jsonObj.put("RouteID", rset.getString(2));
                jsonObj.put("WebDriverIndex", rset.getInt(3));
                jsonObj.put("WebCreatedDate", rset.getString(4));
                jsonObj.put("RouteFromDate", rset.getString(5));
                jsonObj.put("RouteEndDate", rset.getString(6));
                jsonObj.put("DriverName", rset.getString(7));
                jsonObj.put("WebSystemUserIndex", rset.getInt(8));
                jsonObj.put("CellPhone", rset.getString(9));
                jsonObj.put("RunCount", RunCount);
                jsonObj.put("StudentCountRouteWise", StdCount);
                jsonObj.put("TotalRouteDistance", TotalRouteDistance);

                jsonArray.add(jsonObj);
            }
            rset.close();
            cStmt.close();

            //Printing
            SOS.print(jsonArray.toJSONString());
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Error In XML Creation Main Function", e.getMessage(), e);
            out.println(e.getMessage());
            return;
        }
    }

    private void RouteWiseRunData(HttpServletRequest request, Connection conn, ServletOutputStream SOS, UtilityHelper helper, PrintWriter out, ServletContext context) {
        cStmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();
        int RouteIdx = Integer.parseInt(request.getParameter("RouteIdx").trim());
        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        int SystemUserIndex = helper.MobileUserIndex(request, UserId, conn, getServletContext());
        double calculateDistance = 0.0d;
        int StdCount = 0;
        try {
            Query = "{CALL SP_GET_RunInfo(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, RouteIdx);
            rset = cStmt.executeQuery();
            while (rset.next()) {
                JSONObject jsonObj = new JSONObject();
                calculateDistance = helper.distanceCalculator(request, rset.getInt(10), conn, getServletContext());
                StdCount = helper.StudentCountRunWise(request, rset.getInt(10), conn, context);

                jsonObj.put("RouteID", rset.getString(1));
                jsonObj.put("DriverName", rset.getString(2));
                jsonObj.put("RunIndex", rset.getInt(10));
                jsonObj.put("RunID", rset.getString(3));
                jsonObj.put("Type", rset.getString(7));
                jsonObj.put("RunType", rset.getString(8));
                jsonObj.put("RunDistance", calculateDistance);
                jsonObj.put("RunRate", rset.getString(4));
                jsonObj.put("RunStartDate", rset.getString(5));
                jsonObj.put("RunStdCount", StdCount);

/*                Object[] StudentInfo = helper.getStudentInfoRunWise(request, rset.getInt(9), rset.getInt(10), conn, context);

                jsonObj.put("StudentIndex", StudentInfo[0]);
                jsonObj.put("SchoolIndex", StudentInfo[1]);
                jsonObj.put("ChildName", StudentInfo[2]);
                jsonObj.put("Age", StudentInfo[3]);
                jsonObj.put("DOB", StudentInfo[4]);
                jsonObj.put("PickupLat", StudentInfo[5]);
                jsonObj.put("PickupLon", StudentInfo[6]);
                jsonObj.put("DropOffLat", StudentInfo[7]);
                jsonObj.put("DropOffLon", StudentInfo[8]);
                jsonObj.put("SchoolName", StudentInfo[9]);*/


                jsonArray.add(jsonObj);
            }
            rset.close();
            cStmt.close();

            //Printing
            SOS.print(jsonArray.toJSONString());
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Error In XML Creation Main Function", e.getMessage(), e);
            out.println(e.getMessage());
            return;
        }
    }

    private void SaveDriverInspectionData(HttpServletRequest request, Connection conn, ServletOutputStream SOS, UtilityHelper helper, PrintWriter out, ServletContext context) {
        cStmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();

        String DentFound = request.getParameter("DentFound").trim();
        String AccessoriesMissing = request.getParameter("AccessoriesMissing").trim();
        String Documents = request.getParameter("Documents").trim();
        String Remarks = request.getParameter("Remarks").trim();
        String Pic1 = request.getParameter("Pic1").trim();
        String Pic2 = request.getParameter("Pic2").trim();
        String Pic3 = request.getParameter("Pic3").trim();
        String Pic4 = request.getParameter("Pic4").trim();
        String Pic5 = request.getParameter("Pic5").trim();


        try {
            //JSON Variables
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObj = new JSONObject();

            String CurrDate = helper.getCurrDate(request, conn);
            int SystemUserIndex = helper.MobileUserIndex(request, UserId, conn, getServletContext());
            String Result = helper.saveInspectionData(request, conn, context, DentFound, AccessoriesMissing, Documents,
                    Remarks, Pic1, Pic2, Pic3, Pic4, Pic5, 0, CurrDate, "MobileUser", SystemUserIndex);
            //out.println("Result --> " + Result);
            if (Result.equals("Success")) {
                jsonObj.put("Result", "true");
                jsonObj.put("Message", Result);
            } else {
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "Something went Wrong while saving Data!!. Error Message 001 ");
            }
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Error In SaveDriverInspectionData ", e.getMessage(), e);
            out.println(e.getMessage());
            return;
        }
    }

    private void StudentInfoRunWise(HttpServletRequest request, Connection conn, ServletOutputStream SOS, UtilityHelper helper, PrintWriter out, ServletContext context) {
        cStmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();
        int RouteIdx = Integer.parseInt(request.getParameter("RouteIdx").trim());
        int RunIdx = Integer.parseInt(request.getParameter("RunIdx").trim());
        //JSON Variables
        JSONArray jsonArray = new JSONArray();

        try {

            Query = "{CALL SP_GET_LatLonStdWise(?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setInt(1, RouteIdx);
            cStmt.setInt(2, RunIdx);
            rset = cStmt.executeQuery();
            while (rset.next()) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("StudentIndex", rset.getInt(1));
                jsonObj.put("SchoolIndex", rset.getInt(2));
                jsonObj.put("ChildName", rset.getString(3));
                jsonObj.put("Age", rset.getInt(4));
                jsonObj.put("DOB", rset.getString(5));
                jsonObj.put("PickupLat", rset.getString(6));
                jsonObj.put("PickupLon", rset.getString(7));
                jsonObj.put("DropOffLat", rset.getString(8));
                jsonObj.put("DropOffLon", rset.getString(9));
                jsonObj.put("SchoolName", rset.getString(10));
                jsonObj.put("ParentName", rset.getString(11));
                jsonObj.put("Grade", rset.getString(12));
                jsonObj.put("PhoneNumber", rset.getString(13));
                jsonArray.add(jsonObj);
            }
            rset.close();
            cStmt.close();

            //Printing
            SOS.print(jsonArray.toJSONString());
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Error In StudentInfo Main Function", e.getMessage(), e);
            out.println(e.getMessage());
            return;
        }
    }

    private void checkDriverInspection(HttpServletRequest request, Connection conn, ServletOutputStream SOS, UtilityHelper helper, PrintWriter out, ServletContext context) {
        cStmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();

        try {
            //JSON Variables
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObj = new JSONObject();

            String CurrDate = helper.getDefaultCurrDate(request, conn);
            int SystemUserIndex = helper.MobileUserIndex(request, UserId, conn, getServletContext());

            int countFound = helper.checkDriverInspectionData(request, SystemUserIndex, CurrDate, conn, context);
            if (countFound == 0) {
                jsonObj.put("Result", "true");
                jsonObj.put("Message", countFound);
            } else {
                jsonObj.put("Result", "false");
                jsonObj.put("Message", countFound);
            }
            jsonArray.add(jsonObj);
            out.print(jsonArray.toJSONString());

        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Error In checkDriverInspection ", e.getMessage(), e);
            out.println(e.getMessage());
            return;
        }
    }

    /********************** HOP ON *************************************/

    private void MarkStatus(HttpServletRequest request, Connection conn, ServletOutputStream SOS) throws IOException {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;
        String UserId = request.getParameter("UserId").trim();
        String Status = request.getParameter("Status").trim();
        Status = Status.replace("-", " ");
        String ComplainNumber = request.getParameter("JobNumber").trim();
        ComplainNumber = ComplainNumber.replace("^", "#");
        String Lat = "";
        String Lon = "";
        String QueryPatch = "";

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();

        if (Status.equals("Job Completed") || Status.equals("Job Postponed") || Status.equals("Job Cancelled")) {
            Lat = request.getParameter("Lat").trim();
            //Lat = Lat.substring(0, 9);
            Lon = request.getParameter("Lon").trim();
            //Lon = Lon.substring(0, 9);
            QueryPatch = "";
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
                    " upper(trim(ComplaintStatus))='" + Status.toUpperCase().trim() + "' AND Status = 0";
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
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "No Status Found.Please try again!!");
                jsonArray.add(jsonObj);
                SOS.print(jsonArray.toJSONString());
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
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "No User Record Found.Please try again!!");
                jsonArray.add(jsonObj);
                SOS.print(jsonArray.toJSONString());
                return;
            }

            Query = "SELECT  Id FROM CustomerData WHERE ComplainNumber='" + ComplainNumber + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                CustomerDataId = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (CustomerDataId == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Mark Status - Third Check", "No Complain Found.Please try again!!");
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "No Complain Found.Please try again!!");
                jsonArray.add(jsonObj);
                SOS.print(jsonArray.toJSONString());
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
                            "AssignmentStatus,Latitude,Longtitude,ManualStatus,Distance,FalconRadius,TransferFlag,OldId) " +
                            "VALUES (?,?,2,now(),?,?,?,?,?,0,0,0,0,?)");

            pStmt.setInt(1, TechId);
            pStmt.setInt(2, CustomerDataId);
            pStmt.setInt(3, TechId);
            pStmt.setInt(4, ComplainStatusId);
            pStmt.setInt(5, AssignmentStatus);
            pStmt.setString(6, Lat);
            pStmt.setString(7, Lon);
            pStmt.setInt(8, AssignmentIndex);
            pStmt.executeUpdate();
            pStmt.close();

//            Query = "UPDATE Assignment SET ComplaintStatus=" + ComplainStatusId + ",AssignmentStatus=" + AssignmentStatus + "," +
//                    "Latitude='" + Lat + "',Longtitude='" + Lon + "',CreatedDate = DATE_FORMAT(NOW(),'%Y-%m-%d %I:%h:%s') " +
//                    " WHERE ComplaintId=" + CustomerDataId + " AND UserId=" + TechId;
            Query = "UPDATE Assignment SET ComplaintStatus=" + ComplainStatusId + ",AssignmentStatus=" + AssignmentStatus + "," +
                    "Latitude='" + Lat + "',Longtitude='" + Lon + "',CreatedDate = DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s') " +
                    " WHERE Id = " + AssignmentIndex;
            LogString += " \n " + Query + "  \n ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();
            LogString += " \n *********************************************************************  \n ";
            Supportive.QueryLog(this.getServletContext(), LogString);

            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Mark Status - Fourth Check", e.getMessage(), e);
            jsonObj.put("Result", "false");
            jsonObj.put("Message", e.getMessage());
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
            return;
        }
    }

    private void CustomerData(HttpServletRequest request, Connection conn, ServletOutputStream SOS) {
        cStmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();

        //JSON Variables
        JSONArray jsonArray = new JSONArray();

        try {
            Query = "{CALL MobileDataAssignment(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, UserId);
            rset = cStmt.executeQuery();
            while (rset.next()) {
                JSONObject jsonObj = new JSONObject();

                jsonObj.put("RegNo", rset.getString(1));
                jsonObj.put("CustName", rset.getString(2));
                jsonObj.put("CellNo", rset.getString(3));
                jsonObj.put("PhNo", rset.getString(4));
                jsonObj.put("Make", rset.getString(5));
                jsonObj.put("Model", rset.getString(6));
                jsonObj.put("Color", rset.getString(7));
                jsonObj.put("ChNo", rset.getString(8));
                jsonObj.put("Type", rset.getString(9));
                jsonObj.put("Address", rset.getString(10));
                jsonObj.put("DeviceNo", rset.getString(11));
                jsonObj.put("Insurance", rset.getString(12));
                jsonObj.put("ComplainNumber", rset.getString(13));
                jsonObj.put("CreatedDate", rset.getString(16));
                jsonObj.put("ServerDate", rset.getString(17));
                jsonObj.put("Longitude", rset.getString(18));
                jsonObj.put("Latitude", rset.getString(19));

                jsonArray.add(jsonObj);
            }
            rset.close();
            cStmt.close();

            //Printing
            SOS.print(jsonArray.toJSONString());
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Error In XML Creation Main Function", e.getMessage(), e);

            return;
        }
    }

    private void getCompleteJobs(HttpServletRequest request, Connection conn, ServletOutputStream SOS) {
        cStmt = null;
        rset = null;
        Query = " ";
        String UserId = request.getParameter("UserId").trim();

        //JSON Variables
        JSONArray jsonArray = new JSONArray();

        try {
            Query = "{CALL GetCompleteJobsData(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, UserId);
            rset = cStmt.executeQuery();
            while (rset.next()) {
                JSONObject jsonObj = new JSONObject();

                jsonObj.put("RegNo", rset.getString(1));
                jsonObj.put("CustName", rset.getString(2));
                jsonObj.put("CellNo", rset.getString(3));
                jsonObj.put("PhNo", rset.getString(4));
                jsonObj.put("Make", rset.getString(5));
                jsonObj.put("Model", rset.getString(6));
                jsonObj.put("Color", rset.getString(7));
                jsonObj.put("ChNo", rset.getString(8));
                jsonObj.put("Type", rset.getString(9));
                jsonObj.put("Address", rset.getString(10));
                jsonObj.put("DeviceNo", rset.getString(11));
                jsonObj.put("Insurance", rset.getString(12));
                jsonObj.put("ComplainNumber", rset.getString(13));
                jsonObj.put("CreatedDate", rset.getString(16));
                jsonObj.put("ServerDate", rset.getString(17));
                jsonObj.put("Longitude", rset.getString(18));
                jsonObj.put("Latitude", rset.getString(19));

                jsonArray.add(jsonObj);
            }
            rset.close();
            cStmt.close();

            //Printing
            SOS.print(jsonArray.toJSONString());
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Error In XML Creation Main Function", e.getMessage(), e);

            return;
        }
    }

    private void MarkLocation(HttpServletRequest request, Connection conn, ServletOutputStream SOS) throws IOException {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;

        String UserId = request.getParameter("UserId").trim();
        String Latitude = request.getParameter("Lat").trim();
        //Latitude = Latitude.substring(0, 9);
        Latitude = Latitude.length() >= 9 ? Latitude.substring(0, 9) : Latitude;
        String Longitude = request.getParameter("Lon").trim();
        //Longitude = Longitude.substring(0, 9);
        Longitude = Longitude.length() >= 9 ? Longitude.substring(0, 9) : Longitude;
        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();

        int LocationCount = 0;
        try {
            if (!Latitude.equals("0.0") || !Longitude.equals("0.0")) {

                Query = "SELECT  COUNT(*) FROM TechnicianLocation WHERE upper(UserId) = '" + UserId + "' ";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                if (rset.next())
                    LocationCount = rset.getInt(1);
                rset.close();
                stmt.close();

                if (LocationCount == 0) {
                    pStmt = conn.prepareStatement(
                            "INSERT INTO TechnicianLocation (Longtitude, Latitude, UserId, Status, CreatedDate) " +
                                    "VALUES (?,?,?,0,NOW())");
                    pStmt.setString(1, Longitude);
                    pStmt.setString(2, Latitude);
                    pStmt.setString(3, UserId);

                    pStmt.executeUpdate();
                    pStmt.close();
                } else {
                    Query = "UPDATE  TechnicianLocation SET Longtitude = '" + Longitude + "',Latitude = '" + Latitude + "' " +
                            " WHERE UserId = '" + UserId + "' ";
                    stmt = conn.createStatement();
                    stmt.executeUpdate(Query);
                    stmt.close();
                }

            }
            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());

        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Mark Location", e.getMessage(), e);
            jsonObj.put("Result", "false");
            jsonObj.put("Message", e.getMessage());
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
            return;
        }
    }

    private void InspectionForm(HttpServletRequest request, Connection conn, ServletOutputStream SOS) throws IOException {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();

        String UserId = request.getParameter("UserId").trim();
        String ComplainNumber = request.getParameter("JobNumber").trim();

        String Horn = request.getParameter("Horn").trim();
        String InteriorLights = request.getParameter("InteriorLights").trim();
        String SideMirrors = request.getParameter("SideMirrors").trim();
        String PowerWindows = request.getParameter("PowerWindows").trim();
        String CentralLockingSystem = request.getParameter("CentralLockingSystem").trim();
        String AC = request.getParameter("AC").trim();
        String Radio = request.getParameter("Radio").trim();
        String TempGauge = request.getParameter("TempGauge").trim();
        String FuelGauge = request.getParameter("FeulGauge").trim();
        String ABSLight = request.getParameter("ABSLight").trim();
        String AirBagLight = request.getParameter("AirBagLight").trim();
        String EngineCheckLight = request.getParameter("EngineCheckLight").trim();
        String HandBrake = request.getParameter("HandBrake").trim();
        String CleanInterior = request.getParameter("CleanInterior").trim();
        String DashBoard = request.getParameter("DashBoard").trim();
        String GearShift = request.getParameter("GearShift").trim();
        String CustBelongings = request.getParameter("CustBelongings").trim();
        String ExteriorLights = request.getParameter("ExteriorLights").trim();
        String BodyDamage = request.getParameter("BodyDamage").trim();
        String IntExtScratches = request.getParameter("IntExtScratches").trim();
        String Windscreen = request.getParameter("Windscreen").trim();
        String ToolKit = request.getParameter("ToolKit").trim();
        String Carjack = request.getParameter("Carjack").trim();
        String Pic1 = request.getParameter("Pic1").trim();
        String Pic2 = request.getParameter("Pic2").trim();
        String Pic3 = request.getParameter("Pic3").trim();
        String Pic4 = request.getParameter("Pic4").trim();

        try {

            pStmt = conn.prepareStatement(
                    "INSERT INTO FaultInspection (Horn, InteriorLights, SideMirrors, PowerWindows, CentralLockingSystem, AC, " +
                            "Radio, TempGauge, FuelGauge, ABSLight, AirBagLight, EngineCheckLight, HandBrake, CleanInterior, " +
                            "DashBoard, GearShift, CustBelongings, ExteriorLights, BodyDamage, IntExtScratches, Windscreen, " +
                            "ToolKit, Carjack, Pic1, Pic2, Pic3, Pic4, UserId, Status, CreatedDate,ComplainNumber) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,0,NOW(),?) ");
            pStmt.setString(1, Horn);
            pStmt.setString(2, InteriorLights);
            pStmt.setString(3, SideMirrors);
            pStmt.setString(4, PowerWindows);
            pStmt.setString(5, CentralLockingSystem);
            pStmt.setString(6, AC);
            pStmt.setString(7, Radio);
            pStmt.setString(8, TempGauge);
            pStmt.setString(9, FuelGauge);
            pStmt.setString(10, ABSLight);
            pStmt.setString(11, AirBagLight);
            pStmt.setString(12, EngineCheckLight);
            pStmt.setString(13, HandBrake);
            pStmt.setString(14, CleanInterior);
            pStmt.setString(15, DashBoard);
            pStmt.setString(16, GearShift);
            pStmt.setString(17, CustBelongings);
            pStmt.setString(18, ExteriorLights);
            pStmt.setString(19, BodyDamage);
            pStmt.setString(20, IntExtScratches);
            pStmt.setString(21, Windscreen);
            pStmt.setString(22, ToolKit);
            pStmt.setString(23, Carjack);
            pStmt.setString(24, Pic1);
            pStmt.setString(25, Pic2);
            pStmt.setString(26, Pic3);
            pStmt.setString(27, Pic4);
            pStmt.setString(28, UserId);
            pStmt.setString(29, ComplainNumber);

            pStmt.executeUpdate();
            pStmt.close();

            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Inspection Form Data", e.getMessage(), e);
            jsonObj.put("Result", "false");
            jsonObj.put("Message", "Error while saving inspection data. Please contact system administrator!!");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
            return;
        }
    }

    private void QAForm(HttpServletRequest request, Connection conn, ServletOutputStream SOS) throws IOException {
        stmt = null;
        rset = null;
        Query = "";
        pStmt = null;

        String UserId = request.getParameter("UserId").trim();

        String Ans = request.getParameter("Ans").trim();
        String Ans1 = request.getParameter("Ans1").trim();
        String Ans2 = request.getParameter("Ans2").trim();
        String Ans3 = request.getParameter("Ans3").trim();
        String Ans4 = request.getParameter("Ans4").trim();
        String Ans5 = request.getParameter("Ans5").trim();
        String Pic1 = request.getParameter("Pic1").trim();
        String Pic2 = request.getParameter("Pic2").trim();
        String Pic3 = request.getParameter("Pic3").trim();
        String Pic4 = request.getParameter("Pic4").trim();
        String ComplainNumber = request.getParameter("CaseID").trim();

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();

        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO QAForm (Ans, Ans1, Ans2, Ans3, Ans4, Ans5, Pic1, Pic2, Pic3, Pic4, ComplainNumber, UserId,Status,CreatedDate) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,0,NOW()) ");
            pStmt.setString(1, Ans);
            pStmt.setString(2, Ans1);
            pStmt.setString(3, Ans2);
            pStmt.setString(4, Ans3);
            pStmt.setString(5, Ans4);
            pStmt.setString(6, Ans5);
            pStmt.setString(7, Pic1);
            pStmt.setString(8, Pic2);
            pStmt.setString(9, Pic3);
            pStmt.setString(10, Pic4);
            pStmt.setString(11, ComplainNumber);
            pStmt.setString(12, UserId);

            pStmt.executeUpdate();
            pStmt.close();

            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
        } catch (Exception Ex) {
            Supportive.doLog(this.getServletContext(), "QA Form Data", Ex.getMessage(), Ex);
            jsonObj.put("Result", "false");
            jsonObj.put("Message", Ex.getMessage());
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
            return;
        }
    }

    private void SavePicture(HttpServletRequest request, PrintWriter out) {
        try {
            FileOutputStream fout = new FileOutputStream(new File("/opt/HopOn.txt"), true);

            Enumeration e = request.getParameterNames();
            String Param;
            while (e.hasMoreElements()) {
                Param = (String) e.nextElement();
                fout.write(("Modified Code Param =  " + Param + "  Value = " + request.getParameter(Param) + "\r\n").getBytes());
            }
            fout.close();

            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();

            fileItemFactory.setSizeThreshold(1024 * 1024); // 1 MB

            File tmpDir = null;
            tmpDir = new File(getPicsFolder());
            fileItemFactory.setRepository(tmpDir);

            ServletFileUpload uploadHandler = new ServletFileUpload(
                    fileItemFactory);
            try {
                List items = uploadHandler.parseRequest(request);

                for (Object item1 : items) {
                    FileItem item = (FileItem) item1;
                    if (!item.isFormField()) {
                        File file = new File(getPicsFolder(), item.getName());
                        item.write(file);
                    }
                }
                out.close();
            } catch (FileUploadException ex) {
                log("Error encountered while parsing the request", ex);
            } catch (Exception ex) {
                log("Error encountered while uploading file", ex);
            }
        } catch (Exception e) {
            Supportive.doLog(this.getServletContext(), "Picture Saving Error!!", e.getMessage(), e);
            out.println("Exception : " + e.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    @SuppressWarnings("SameReturnValue")
    private String getPicsFolder() {
        return "/opt/Htmls/HopOn/pics/";
    }

    private void PostponedReasonInsertion(HttpServletRequest request, Connection conn, ServletOutputStream SOS) throws IOException {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;

        String UserId = request.getParameter("UserId").trim();
        String Status = request.getParameter("Status").trim();
        Status = Status.replace("-", " ");
        String ComplainNumber = request.getParameter("UniqueID").trim();
        ComplainNumber = ComplainNumber.replace("^", "#");
        String PostponedReason = request.getParameter("PostponedReason");

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();

        int CustDataIndex = 0;
        try {
            Query = "SELECT  Id FROM CustomerData WHERE ComplainNumber='" + ComplainNumber + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                CustDataIndex = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (CustDataIndex == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "PostponedReason Check - 0001", "No Complain Found.Please try again!!");
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "No Complain Found.Please try again!!");
                jsonArray.add(jsonObj);
                SOS.print(jsonArray.toJSONString());
                return;
            }
            int AssignmentIndex = 0;
            Query = "SELECT Max(Id) FROM Assignment WHERE ComplaintId = " + CustDataIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                AssignmentIndex = rset.getInt(1);
            rset.close();
            stmt.close();

            int TechnicianId = 0;
            Query = "SELECT Id FROM MobileUsers WHERE upper(trim(UserId))='" + UserId.toUpperCase().trim() + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                TechnicianId = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (TechnicianId == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Postponed Reason - Second Check", "No User Record Found.Please try again!!");
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "No User Record Found.Please try again!!");
                jsonArray.add(jsonObj);
                SOS.print(jsonArray.toJSONString());
                return;
            }

            pStmt = conn.prepareStatement(
                    "INSERT INTO PostponedReasons(PostponedReason, JobId, UserId, Status, CreatedDate, AssignmentIndex) " +
                            "VALUES (?,?,?,0,NOW(),?)");

            pStmt.setString(1, PostponedReason);
            pStmt.setInt(2, CustDataIndex);
            pStmt.setInt(3, TechnicianId);
            pStmt.setInt(4, AssignmentIndex);
            pStmt.executeUpdate();
            pStmt.close();

            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
        } catch (Exception Ex) {
            Supportive.doLogMethodMessage(this.getServletContext(), "Postponed Reason - Third Check", Ex.getMessage());
            jsonObj.put("Result", "false");
            jsonObj.put("Message", "Exception Message in Postponed Reason");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
            return;
        }
    }

    private void RevertStatus(HttpServletRequest request, Connection conn, ServletOutputStream SOS) throws IOException {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;

        String UserId = request.getParameter("UserId").trim();
        String Status = request.getParameter("Status").trim();
        Status = Status.replace("-", " ");
        String ComplainNumber = request.getParameter("JobNumber").trim();
        ComplainNumber = ComplainNumber.replace("^", "#");
        String StatusVal = request.getParameter("StatusVal").trim();
        String AssignmentStatus = request.getParameter("AssignmentStatus").trim();

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();

        int CustDataIndex = 0;
        try {
            Query = "SELECT  Id FROM CustomerData WHERE ComplainNumber='" + ComplainNumber + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                CustDataIndex = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (CustDataIndex == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Revert Status Check - 0001", "No Complain Found.Please try again!!");
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "No Complain Data Found.Please try again!!");
                jsonArray.add(jsonObj);
                SOS.print(jsonArray.toJSONString());
                return;
            }
            int AssignmentIndex = 0;
            Query = "SELECT Max(Id) FROM Assignment WHERE ComplaintId = " + CustDataIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                AssignmentIndex = rset.getInt(1);
            rset.close();
            stmt.close();

            int TechnicianId = 0;
            Query = "SELECT Id FROM MobileUsers WHERE upper(trim(UserId))='" + UserId.toUpperCase().trim() + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                TechnicianId = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (TechnicianId == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Revert Status - Second Check", "No User Record Found.Please try again!!");
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "No User Record Found.Please try again!!");
                jsonArray.add(jsonObj);
                SOS.print(jsonArray.toJSONString());
                return;
            }

            Query = "UPDATE Assignment SET ComplaintStatus=" + StatusVal + ",AssignmentStatus=" + AssignmentStatus + "," +
                    "CreatedDate = DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s') " +
                    " WHERE Id = " + AssignmentIndex;
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
        } catch (Exception Ex) {
            Supportive.doLogMethodMessage(this.getServletContext(), "Revert Status - Third Check", Ex.getMessage());
            jsonObj.put("Result", "false");
            jsonObj.put("Message", "Exception Message in Postponed Reason");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
            return;
        }
    }

    private void UpdateFirebaseToken(HttpServletRequest request, Connection conn, ServletOutputStream SOS) throws IOException {
        stmt = null;
        rset = null;
        Query = " ";
        pStmt = null;

        String UserId = request.getParameter("UserId").trim();
        String TokenString = request.getParameter("TokenString").trim();

        //JSON Variables
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObj = new JSONObject();

        int UserCount = 0;
        int TechId = 0;
        try {
            Query = "SELECT Id FROM MobileUsers WHERE upper(trim(UserId))='" + UserId.toUpperCase().trim() + "' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                TechId = rset.getInt(1);
            }
            rset.close();
            stmt.close();

            if (TechId == 0) {
                Supportive.doLogMethodMessage(this.getServletContext(), "Updating Firebase Token First", "No User Record Found.Please try again!!");
                jsonObj.put("Result", "false");
                jsonObj.put("Message", "No User Record Found.Please try again!!");
                jsonArray.add(jsonObj);
                SOS.print(jsonArray.toJSONString());
                return;
            }

            Query = "SELECT  COUNT(*) FROM UserBindageFB WHERE UserId = " + TechId;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                UserCount = rset.getInt(1);
            rset.close();
            stmt.close();

            if (UserCount == 0) {
                pStmt = conn.prepareStatement(
                        "INSERT INTO UserBindageFB(UserId, FBTokenId, Status, CreatedDate) " +
                                "VALUES (?,?,0,NOW())");

                pStmt.setInt(1, TechId);
                pStmt.setString(2, TokenString);
                pStmt.executeUpdate();
                pStmt.close();
            } else {
                Query = "UPDATE UserBindageFB SET FBTokenId = '" + TokenString + "' WHERE UserId = " + TechId;
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            }

            jsonObj.put("Result", "true");
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());

        } catch (Exception Ex) {
            jsonObj.put("Result", "false");
            jsonObj.put("Message", Ex.getMessage());
            jsonArray.add(jsonObj);
            SOS.print(jsonArray.toJSONString());
            return;
        }
    }
}
