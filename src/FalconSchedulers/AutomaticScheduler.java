package FalconSchedulers;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Date;

@SuppressWarnings("Duplicates")
public class AutomaticScheduler {

    //    private static CallableStatement cStmt3 = null;
    private static ResultSet rset3 = null;
    private static Statement stmt3 = null;
    private static String Query2 = "";
    public static boolean ASC = true;
    public static boolean DESC = false;
    private static String LogString = null;

    public static void main(String[] args) {
        String getlocation = "";
        String getlocationC = "";


        ArrayList<Double> Miles = new ArrayList<Double>();
        Connection conn = null;
        Date dt1 = new Date();
        long timestamp = dt1.getTime();
        String Query = "";
        String Query1 = "";

        String Query3 = "";
        PreparedStatement pStmt = null;
        Statement stmt = null;
        ResultSet rset = null;
        Statement stmt1 = null;
        ResultSet rset1 = null;
        CallableStatement cStmt2 = null;
        ResultSet rset2 = null;

        Date dt = new Date();
        long StartTime = dt.getTime();

        int TotalJobCount = 0;
        String Technician = "";
        int TechType = 0;
        int TechCount = 0;
        int TechId = 0;
        int JobNatureIndex = 0;
        int ComplainId = 0;
        int JobTypeIndex = 0;
        String ComplainNumber = "";
        String Latitude = "";
        String Longitude = "";
        String QueryPatch = " ";
        int TotalTechCount = 0;
        double RadiusCheck = 0.0;
        double FalconRadiusCheck = 0.0;
        String FalconLat = "";
        String FalconLon = "";
        int i = 0;
        String Stage = "";
        DecimalFormat Dformat = new DecimalFormat("##.000");
        System.out.println("Automatic Scheduling STARTS ....[" + StartTime + "] " + timestamp + "\r\n");
        LogString = "";
        LogString += " \n *********************************************************************  \n ";
        try {
            Stage = "0";
/*            if ( SendProcessAlreadyRunning() ) {
                System.out.println("Unable to start Automatic Scheduler.Process is Already Running ");
                return;
            }*/
            FileWriter filewriter = new FileWriter("/opt/FalconLogs/SchedulerLogs/AutomaticScheduler.log", true);
            filewriter.write("\r\n Scheduler Time starts at " + new Date().toString() + "\r\n");
            conn = getConnection();
            Stage = "1";
            Query1 = "{CALL TechnicianCount()}";
            CallableStatement cStmt = conn.prepareCall(Query1);
            rset1 = cStmt.executeQuery();
            if (rset1.next()) {
                TotalTechCount = rset1.getInt(1);
            }
            rset1.close();
            cStmt.close();
            LogString += " Total Tech Count  : " + TotalTechCount + " | ";

            Stage = "2";
            Query1 = "{CALL radius_check()}";
            cStmt = conn.prepareCall(Query1);
            rset1 = cStmt.executeQuery();
            if (rset1.next()) {
                RadiusCheck = rset1.getDouble(1);
                FalconRadiusCheck = rset1.getDouble(2);
                FalconLat = rset1.getString(3);
                FalconLon = rset1.getString(4);
            }
            rset1.close();
            cStmt.close();
            LogString += " Radius Check  : " + RadiusCheck + " | ";
            LogString += " Falcon Radius Check  : " + FalconRadiusCheck + " | ";
            LogString += " Falcon Lat  : " + FalconLat + " | ";
            LogString += " FalconLon  : " + FalconLon + " | ";

            //-----------------------Agent --------------------------


            ////--------------------------Agent End
            //Dynamically Picking TotalJobsCount in order to implement check of 3 or more complains.
            //Depending on the needs
            Query1 = "{CALL TotalJobsNumber()}";
            cStmt = conn.prepareCall(Query1);
            rset1 = cStmt.executeQuery();
            if (rset1.next()) {
                TotalJobCount = rset1.getInt(1);
            }
            rset1.close();
            cStmt.close();
            LogString += " Total Job Count  : " + TotalJobCount + " | ";

            Stage = "3";
            //Jobs to be assigned
            Query = " SELECT Id,JobTypeIndex,JobNatureIndex,ComplainNumber,Latitude,Longtitude " +
                    " FROM CustomerData WHERE AssignStatus=0 AND Latitude != '' AND Longtitude != '' ";

            try {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    ComplainId = rset.getInt(1);
                    JobTypeIndex = rset.getInt(2);
                    JobNatureIndex = rset.getInt(3);
                    ComplainNumber = rset.getString(4).trim();
                    Latitude = rset.getString(5).trim();
                    Longitude = rset.getString(6).trim();

                    LogString += " ComplainId  : " + ComplainId + " | ";
                    LogString += " JobNatureIndex  : " + JobNatureIndex + " | ";
                    LogString += " ComplainNumber  : " + ComplainNumber + " | ";
                    LogString += " Latitude  : " + Latitude + " | ";
                    LogString += " Longitude  : " + Longitude + " | ";
                    Stage = "4";
                    System.out.println(" LOOP RUNNING TIME " + i++);
//                    System.out.println("Complain " + ComplainId);
                    //System.out.println("Complain Latitude " + Latitude);
                    //System.out.println("Complain Longitude " + Longitude);
                    double agentdistance = 0.0d;
                    HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
                    HashMap<Integer, Double> cm = new HashMap<Integer, Double>();

                    //Determining the Type of the technician
                    //1. Expert
                    //2. Normal
                    // Nature of Job
                    //1. Medium Job
                    //2. Difficult Job
                    //System.out.println("Job Nature Index " + JobNatureIndex);
                    if (JobNatureIndex == 2) {
                        Query1 = " SELECT a.Id,a.TechType,b.Latitude,b.Longtitude " +
                                " FROM MobileUsers a " +
                                " STRAIGHT_JOIN TechnicianLocation b ON a.UserId=b.UserId AND b.CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId=a.UserId) " +
                                " STRAIGHT_JOIN LoginTrail c ON a.UserId=c.UserId AND c.UserType='M' " +
                                " WHERE a.Status=0  ";
                    } else {
                        Query1 = "SELECT a.Id,a.TechType,b.Latitude,b.Longtitude " +
                                " FROM MobileUsers a " +
                                " STRAIGHT_JOIN TechnicianLocation b ON a.UserId=b.UserId AND b.CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId=a.UserId) " +
                                " STRAIGHT_JOIN LoginTrail c ON a.UserId=c.UserId AND c.UserType='M' " +
                                " WHERE a.Status=0 ORDER BY a.TechType ";
                    }
                    try {
                        stmt1 = conn.createStatement();
                        rset1 = stmt1.executeQuery(Query1);
                        while (rset1.next()) {
                            Stage = "5";
                            //System.out.println("1) Tech Id " + rset1.getInt(1));
                            //System.out.println("2) Complain Latitude " + Latitude);
                            //System.out.println("3) Complain Longitude " + Longitude);
                            //System.out.println("4) Tech Latitude  " + rset1.getString(3));
                            //System.out.println("5) Tech Longitude   " + rset1.getString(4));
                            getlocation = getDistance1(Latitude.trim(), Longitude.trim(), rset1.getString(3), rset1.getString(4));
                            LogString += " Location  : " + getlocation + " | ";
                            System.out.println("6) get Location   " + getlocation);

                            Query3 = "{CALL AssignmentCount(?)}";
                            cStmt2 = conn.prepareCall(Query3);
                            cStmt2.setInt(1, rset1.getInt(1));
                            rset2 = cStmt2.executeQuery();
                            if (rset2.next()) {
                                TechCount = rset2.getInt(1);
                            }
                            rset2.close();
                            cStmt2.close();

                            Stage = "6";
                            LogString += " To be Assigned Tech Count  : " + rset1.getInt(1) + " | ";
                            LogString += " TechCount  : " + TechCount + " | ";
                            if (TechCount >= TotalJobCount) {
                                //if ((TotalJobCount > TechCount)) {
                                //Supportive.doLogMethodMessage(null, "Main Method - 001", "No Technician Count  ! ");
                                continue;
                            }
                            Stage = "7";
                            i++;

                            Double Distance = Double.parseDouble(getlocation);

                            hm.put(rset1.getInt(1), Distance);

                            //For Falcon Pool we are checking and filling hash map according to the tech within the radius of falcon-i or not
                            getlocationC = getDistance1(FalconLat, FalconLon, rset1.getString(3), rset1.getString(4));
                            Double Distance2 = Double.parseDouble(getlocationC);
                            cm.put(rset1.getInt(1), Distance2);
                            // Sorting Users in hash map
/*                            Map<Integer, Double> sortedMapAsc = sortByComparator(hm, ASC);
                            for (Map.Entry<Integer, Double> entry : sortedMapAsc.entrySet()) {
                                //System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                                hm.put(entry.getKey(), entry.getValue());
                            }*/
                            //printMap(hm);
                            //new work
/*                            Map<Integer, String> map = sortByValues(hm);
                            Set set2 = map.entrySet();
                            Iterator iterator2 = set2.iterator();
                            System.out.println("First DISTANT " + Distance);
                            while (iterator2.hasNext()) {
                                Map.Entry me2 = (Map.Entry) iterator2.next();
                                System.out.print(me2.getKey() + ": ");
                                System.out.println(me2.getValue());
                            }

                            getlocationC = getDistance1(FalconLat, FalconLon, rset1.getString(3), rset1.getString(4));
                            Double Distance2 = Double.parseDouble(getlocationC);
//                            System.out.println("SECOND DISTANT " + Distance2);
                            cm.put(rset1.getInt(1), Distance2);
                            Map<Integer, String> map1 = sortByValues(cm);
                            Set set3 = map1.entrySet();
                            Iterator iterator3 = set3.iterator();
                            while (iterator3.hasNext()) {
                                Map.Entry me3 = (Map.Entry) iterator3.next();
                                System.out.print(me3.getKey() + ": ");
                                System.out.println(me3.getValue());
                            }*/
                        }
                        rset1.close();
                        stmt1.close();
                    } catch (Exception e) {
                        doLogMethodMessage("Main Method - 002", "Fill Technician Error  ! ");
                        System.out.println("Fill Technician Error --> " + e.getMessage() + "--- Stage is = " + Stage + " ****** Complain Id " + ComplainId);
                        System.out.close();
                        System.out.flush();
                        return;
                    }
                    Stage = "12";
                    int assigneAgentid = 0;
                    Double value = 0.0;

                    Stage = "100";
                    if (hm.size() <= 0)
                        continue;

                    Stage = "13";
                    Double minValue = Double.MAX_VALUE;
                    Double TabVal = Double.MIN_VALUE;
                    for (Integer key : hm.keySet()) {
                        value = hm.get(key);
                        //System.out.println("KEY  --> " + key);
                        //System.out.println("VAL --> " + value);

                        if (value < minValue) {
                            minValue = value;
                            assigneAgentid = key;
                        }
                    }
//                    System.out.println("Assigned Technician Id --> " + assigneAgentid);
                    Stage = "14";
                    LogString += " assigneAgentid  : " + assigneAgentid + " | ";
                    LogString += " minValue  : " + minValue + " | ";
                    //Second Condition Insertion
                    if (assigneAgentid != 0) {
                        if (minValue <= RadiusCheck) {
                            System.out.println("LESS THAN FIVE!!   " + minValue + " ------------ " + RadiusCheck + " ****** " + assigneAgentid + " * * * * " + value);
                            String Result = RecordInsertion(conn, assigneAgentid, ComplainId, "Admin", minValue, 0);
                            InsertionLogs(conn, assigneAgentid, ComplainId, "Admin", minValue, 0);
                            if (Result.equals("Success")) {
//                                Updating only those jobs who has been assigned to any tech only. Rest will not be set to assigned
                                UpdateJobStatus(conn, ComplainNumber);
                                UpdateInitialStatus(conn, ComplainNumber, assigneAgentid);
                                System.out.println("Record Assigned".trim());
                            } else {
                                System.out.println("Error while Record Assignment!!");
                                doLogMethodMessage("Main Method - 003", "Error while Record Assignment!! ! ");
                            }

                        } else {
                            System.out.println("DISTANT GREATER THAN TECH LOCATION   " + minValue + " ------------ " + RadiusCheck + " ****** " + assigneAgentid + " * * * * " + value);

                            //New Check has been implemented
                            // Now Complains will ne assign to those technician if it is in the same area of the un-assigned complain/new complain
                            assigneAgentid = ltr(conn, Latitude.trim(), Longitude.trim(), RadiusCheck, ComplainId, ComplainNumber, assigneAgentid, TotalJobCount);
                            System.out.println("LTR AGENT ID --> " + assigneAgentid);
                            LogString += " Assigned Tech Next  : " + assigneAgentid + " | ";
                            //If no technician is found then it will go to falcon pool and will assign to any technician
                            if (assigneAgentid == 0) {
                                Double value0 = 0.0;
                                Double value01 = 0.0;
                                for (Integer key0 : cm.keySet()) {
                                    value0 = cm.get(key0);
                                    value01 = cm.get(key0);
                                    System.out.println("SEC key 0 --> " + key0);
                                    System.out.println("SEC VAL 0 --> " + value0);
                                    System.out.println("FalconRadiusCheck --> " + FalconRadiusCheck);
                                    LogString += " In Falcon Pool : key0  : " + key0 + " | ";
                                    LogString += " In Falcon Pool : value0  : " + value0 + " | ";
                                    LogString += " In Falcon Pool : FalconRadiusCheck  : " + FalconRadiusCheck + " | ";

                                    TechCount = 0;
                                    Query3 = "{CALL AssignmentCount(?)}";
                                    cStmt2 = conn.prepareCall(Query3);
                                    cStmt2.setInt(1, key0);
                                    rset2 = cStmt2.executeQuery();
                                    if (rset2.next()) {
                                        TechCount = rset2.getInt(1);
                                    }
                                    rset2.close();
                                    cStmt2.close();
                                    LogString += " In Falcon Pool : Tech Count Next   : " + TechCount + " | ";

                                    //Technician in Falcon pool should have 0 jobs in their bank else it will not assign
                                    if (TechCount <= 0) {
                                        //Value0 will be in meter as falcon radius check value is in meter
                                        value0 = value0 * 1000;
                                        if (value0 <= FalconRadiusCheck) {
                                            LogString += " In Falcon Pool When its in meter : After Multiplying   : " + value0 + " | ";

                                            UpdateRecords(conn, key0);
                                            String Result = RecordInsertion(conn, key0, ComplainId, "Admin", value01, 1);
                                            InsertionLogs(conn, assigneAgentid, ComplainId, "Admin", minValue, 1);
                                            LogString += " In Falcon Pool When its in meter Record Assigned | ";
                                            if (Result.equals("Success")) {
                                                //Updating only those jobs who has been assigned to any tech only. Rest will not be set to assigned
                                                UpdateJobStatus(conn, ComplainNumber);
                                                UpdateInitialStatus(conn, ComplainNumber, key0);
                                                System.out.println("Record Assigned".trim());
                                            } else {
                                                System.out.println("Error while Record Assignment!!");
                                                doLogMethodMessage("Main Method - 004", "Error while Record Assignment!! ! ");
                                            }
                                            break;
                                        } else {
                                            System.out.println("GOING IN DISTANT GREATER THAN ELSE COND");
                                            doLogMethodMessage("Main Method - 005", "GOING IN DISTANT GREATER THAN ELSE COND!! ");
                                        }
                                    } else {
                                        System.out.println("Technician has greater count then zero!! ");
                                        doLogMethodMessage("Main Method - 005.1", "Technician has greater count then zero!! ");
                                    }
                                }
                            }//falcon pool if
                        }//No tech in radius else condition
                    } else {
                        System.out.println("NO TECHNICIAN ONLINE !! going for office");
                        doLogMethodMessage("Main Method - 006", "NO TECHNICIAN ONLINE !! going for office ");
                    }
                }
                rset.close();
                stmt.close();

                //******************************************* POSTPONNED WORK *****************************************************//
                int Postponed = 0;
                int TechnicianId = 0;
                int JobId = 0;
                int AssignmentId = 0;
                // Bug in Query 24th May 2018
                // Not picking latest record that is why it is not working correctly
                // Have added new check in the query
                Query = "SELECT DATEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d'), DATE_FORMAT(CreatedDate,'%Y-%m-%d')),UserId ,ComplaintId,Id " +
                        " FROM Assignment WHERE ComplaintStatus = 7 AND AssignmentStatus = 0 AND Status = 0";
                Query = " SELECT DATEDIFF(DATE_FORMAT(NOW(),'%Y-%m-%d'), DATE_FORMAT(b.CreatedDate,'%Y-%m-%d')),  " +
                        " b.UserId ,b.ComplaintId,b.Id  " +
                        " FROM Assignment b  " +
                        " WHERE b.ComplaintStatus = 7 AND  " +
                        " b.Status = 0 AND  " +
                        " b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) ";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    Postponed = 0;
                    TechnicianId = 0;
                    JobId = 0;
                    AssignmentId = 0;

                    Postponed = rset.getInt(1);
                    TechnicianId = rset.getInt(2);
                    JobId = rset.getInt(3);
                    AssignmentId = rset.getInt(4);

                    //if one day is passed with the postponed status then it should mark that case with 0 so that again it will be reassigned
                    if (Postponed >= 1) {

                        int AssignmentIndex = 0;
                        Query = "SELECT Max(Id) FROM Assignment WHERE ComplaintId = " + JobId;
                        stmt = conn.createStatement();
                        rset = stmt.executeQuery(Query);
                        if (rset.next())
                            AssignmentIndex = rset.getInt(1);
                        rset.close();
                        stmt.close();

                        pStmt = conn.prepareStatement(
                                "INSERT INTO PostponedLogs (TechnicianId, JobId, Status, CreatedDate,AssignmentId) " +
                                        "VALUES (?,?,0,NOW(),?)");
                        pStmt.setInt(1, TechnicianId);
                        pStmt.setInt(2, JobId);
                        pStmt.setInt(3, AssignmentIndex);
                        pStmt.executeUpdate();
                        pStmt.close();

                        Query1 = "UPDATE CustomerData SET AssignStatus=0,ReAssigned = 1 WHERE Id = " + JobId + " ";
                        stmt1 = conn.createStatement();
                        stmt1.executeUpdate(Query1);
                        stmt1.close();
                        LogString += " Update Query in Postpone # 1 " + Query1 + " | ";

                        Query1 = "UPDATE Assignment SET Status = 1 WHERE Id = " + AssignmentIndex;
                        stmt1 = conn.createStatement();
                        stmt1.executeUpdate(Query1);
                        stmt1.close();

                        LogString += " Update Query in Postpone # 2 " + Query1 + " | ";
                    }
                }
                rset.close();
                stmt.close();

                LogString += " \n *********************************************************************  \n ";
                LogString(LogString);
            } catch (Exception e) {
                System.out.println("Main try/catch " + e.getMessage() + "--- " + Stage);
                doLogMethodMessage("Main Method - 007", "Main try/catch " + e.getMessage() + " ---- " + Stage);
            }

        } catch (Exception Ex) {
            System.out.println("Update Ends in Exception ....[" + dt.getTime() + "] " + timestamp + "\r\n");
            doLogMethodMessage("Main Method - 008", "Update Ends in Exception ....[\" + dt.getTime() + \"] --- " + Ex.getMessage());
            System.out.println("Outer exception ... " + "-" + Ex.getMessage());
            DumpException("main", "exp", Ex);
        }
    }

    private static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //LIVE
            return DriverManager.getConnection("jdbc:mysql://203.130.0.228/Falcon?user=engro&password=smarttelecard");
            //TEST
//            return DriverManager.getConnection("jdbc:mysql://203.130.0.239/FalconTesting?user=tabish&password=tpassword");
        } catch (Exception e) {
            System.out.println(e);
            DumpException("Connection Error", "exp".trim(), e);
        }
        return null;
    }

    private static boolean SendProcessAlreadyRunning() {
        String s = null;
        int count = 0;
        try {
            Process p = Runtime.getRuntime().exec("ps aux");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            while ((s = stdInput.readLine()) != null) {
                if (s.contains("FalconSchedulers.AutomaticScheduler")) {
                    count++;
                }
            }
            return count > 1;
        } catch (Exception e) {
            System.out.println("Exception in SendSMSAppAlreadyRunning func. " + e.getMessage());
        }
        return false;
    }

    private static void DumpException(String method, String message, Exception exception) {
        String s2 = "";
        try {
            FileWriter filewriter = new FileWriter("/opt/FalconLogs/SchedulerLogs/AutomaticScheduler_Exception.log", true);

            filewriter.write(new Date().toString() + "^" + method + "^" + message + "^" + exception.getMessage() + "\r\n");

            PrintWriter printwriter = new PrintWriter(filewriter, true);
            exception.printStackTrace(printwriter);
            filewriter.write("\r\n");
            filewriter.flush();
            filewriter.close();
            printwriter.close();
        } catch (Exception localException) {
        }
    }

    private static String RecordInsertion(Connection conn, int UserId, int ComplainId, String AssignBy, double Distance, int RadiusFlag) {
        PreparedStatement pStmt = null;
        Statement _sTmt = null;
        ResultSet _rSet = null;
        int _count = 0;
        String Query = "";
        Query = "Select count(*) from Assignment Where ComplaintStatus='6' and ComplaintId='" + ComplainId + "'";
        try {
            _sTmt = conn.createStatement();
            _rSet = _sTmt.executeQuery(Query);
            if (_rSet.next()) {
                _count = _rSet.getInt(1);
            }
            _rSet.close();
            _sTmt.close();
        } catch (Exception e) {
            System.out.println("Error in insertion " + e.getMessage());
            return "-";
        }
        if (_count > 0) {
            return "Error";
        } else {
            try {

/*                String _UserId = "";
                String _Lat = "";
                String _Lon = "";
                Query = "SELECT  UserId FROM MobileUsers WHERE Id = " + UserId;
                _sTmt = conn.createStatement();
                _rSet = _sTmt.executeQuery(Query);
                if (_rSet.next())
                    _UserId = _rSet.getString(1);
                _rSet.close();
                _sTmt.close();

                Query ="SELECT Latitude,Longtitude,CreatedDate FROM TechnicianLocation WHERE UserId='"+_UserId+"' AND\n" +
                        "CreatedDate = (SELECT MAX(CreatedDate) FROM TechnicianLocation WHERE UserId='"+_UserId+"' ) ";
                _sTmt = conn.createStatement();
                _rSet = _sTmt.executeQuery(Query);
                if (_rSet.next()){
                    _Lat = _rSet.getString(1);
                    _Lon = _rSet.getString(2);
                }
                _rSet.close();
                _sTmt.close();

                Query = "UPDATE CustomerData SET InitialLat='"+_Lat+"',InitialLon='"+_Lon+"' WHERE Id = " + ComplainId;
                _sTmt = conn.createStatement();
                _sTmt.executeUpdate(Query);
                _sTmt.close(); */

                //Status Assigmment History
                //In Mobile: Status= 2
                //In Auotmatic Scheduler: Status= 1
                //In Manual Status: Status= 4
                //In TransferJob Status: Status= 3
                // It will be use to pick user name from respective table.

                pStmt = conn.prepareStatement(
                        "INSERT INTO AssignmentHistory(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
                                "AssignmentStatus,Latitude,Longtitude,ManualStatus,Distance,FalconRadius,TransferFlag) " +
                                "VALUES (?,?,1,now(),?,?,?,?,?,0,?,0,0)");

                pStmt.setInt(1, UserId);
                pStmt.setInt(2, ComplainId);
                pStmt.setString(3, "admin");
                pStmt.setInt(4, 0);
                pStmt.setInt(5, 0);
                pStmt.setString(6, "0.0");
                pStmt.setString(7, "0.0");
                pStmt.setDouble(8, Distance);
                pStmt.executeUpdate();
                pStmt.close();

                pStmt = conn.prepareStatement(
                        "INSERT INTO Assignment(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
                                "AssignmentStatus,ManualStatus,Distance,FalconRadius) VALUES(?,?,0,now(),?,0,0,0,?,?)");

                pStmt.setInt(1, UserId);
                pStmt.setInt(2, ComplainId);
                pStmt.setString(3, AssignBy.trim());
                pStmt.setDouble(4, Distance);
                pStmt.setInt(5, RadiusFlag);
                pStmt.executeUpdate();
                pStmt.close();

                return "Success";
            } catch (Exception e) {
                System.out.println("Error in insertion " + e.getMessage());
                return "-";
            }
        }
    }

    //InsertionLogsTable
    private static void InsertionLogs(Connection conn, int UserId, int ComplainId, String AssignBy, double Distance, int RadiusFlag) {
        PreparedStatement pStmt = null;
        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO Insertion_Logs(UserId, ComplaintId, CreatedDate, ComplaintStatus,Distance,RadiusFlag,Status) " +
                            "VALUES (?,?,now(),0,?,?,0)");
            pStmt.setInt(1, UserId);
            pStmt.setInt(2, ComplainId);
            pStmt.setDouble(3, Distance);
            pStmt.setInt(4, RadiusFlag);

            pStmt.executeUpdate();
            pStmt.close();

        } catch (Exception e) {
            System.out.println("Error in insertion " + e.getMessage());
        }
    }

    private static String getDistance(String olat, String olon, String dlat, String dlon) throws IOException {
        String BaseURL = "";
        String reply = "";

        //System.out.println("SMSID= "+smscid+"| SMSPASS= "+smspass+"| Operator= "+operator+"| Mask= "+Mask);
        //BaseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=24.826816,%2067.029033&destinations=24.829744,%2067.033967&key=AIzaSyC_DWDADdI3iz-cG0qIS42-szNnQGdBcU0";
        //BaseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + olat + "," + olon + "&destinations=" + dlat + "," + dlon + "&key=AIzaSyC_DWDADdI3iz-cG0qIS42-szNnQGdBcU0";
        BaseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + olat + "," + olon + "&destinations=" + dlat + "," + dlon + "&key=AIzaSyCiNiJiXwlggLulY3YaJbtv2IylwyL_Bkk";
        //BaseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=40.6655101,-73.89188969999998&destinations=40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.6905615%2C-73.9976592%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626%7C40.659569%2C-73.933783%7C40.729029%2C-73.851524%7C40.6860072%2C-73.6334271%7C40.598566%2C-73.7527626&key=AIzaSyC_DWDADdI3iz-cG0qIS42-szNnQGdBcU0";
        //    System.out.println("R="+BaseURL+"-" );

        URL url = new URL(BaseURL);
        URLConnection uc = url.openConnection();
        InputStream is = uc.getInputStream();

        int size = is.available();
        byte response[] = new byte[size];
        is.read(response);
        reply = new String(response);
        reply = reply.trim();
        return reply;

    }

    private static String getDistance1(String olat, String olon, String dlat, String dlon) {
        final int R = 6371; // Radious of the earth
		/* Double lat1 = Double.parseDouble(args[0]);
		Double lon1 = Double.parseDouble(args[1]);
		Double lat2 = Double.parseDouble(args[2]);
		Double lon2 = Double.parseDouble(args[3]);*/

        Double lat1 = Double.parseDouble(olat);
        Double lon1 = Double.parseDouble(olon);
        Double lat2 = Double.parseDouble(dlat);
        Double lon2 = Double.parseDouble(dlon);

        Double latDistance = toRad(lat2 - lat1);
        Double lonDistance = toRad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double distance = R * c;
        DecimalFormat f = new DecimalFormat("##.00");
        //System.out.println(f.format(distance));
        // System.out.println("The distance between two lat and long is " + distance);
        String Sdistance = String.valueOf(f.format(distance));
        return Sdistance;

    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

    private static void UpdateRecords(Connection conn, int UserId) {
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";
        HashMap<Integer, Integer> MobileUsers = new HashMap<Integer, Integer>();
        int FlagCheck = 0;
        try {
            Query = "SELECT a.Id,a.JobFlag FROM MobileUsers a " +
                    " STRAIGHT_JOIN LoginTrail b ON a.UserId = b.UserId " +
                    " WHERE b.UserType = 'M' ";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                MobileUsers.put(rset.getInt(1), rset.getInt(2));
            }
            rset.close();
            stmt.close();

            Query = "SELECT  count(*) FROM MobileUsers a " +
                    " STRAIGHT_JOIN LoginTrail b ON a.UserId = b.UserId " +
                    " WHERE a.JobFlag = 0";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                FlagCheck = rset.getInt(1);
            rset.close();
            stmt.close();

            if (FlagCheck == 0) {
                Query = "UPDATE MobileUsers SET JobFlag = 0 ";
                stmt = conn.createStatement();
                stmt.executeUpdate(Query);
                stmt.close();
            }


            for (Map.Entry me : MobileUsers.entrySet()) {
                //me.getKey() == UserId &&
                if (me.getValue() == 0) {
                    System.out.println("Key: " + me.getKey() + " & Value: " + me.getValue() + "&HashMap Size : " + MobileUsers.size());
                    Query = "UPDATE MobileUsers SET JobFlag = 1 WHERE Id = " + me.getKey();
                    System.out.println(Query);
                    stmt = conn.createStatement();
                    stmt.executeUpdate(Query);
                    stmt.close();
                    break;
                }
            }
        } catch (Exception Ex) {
            System.out.println("Error In Updation " + Ex.getMessage());
        }
    }

    private static void UpdateJobStatus(Connection conn, String ComplainNumber) {
        try {
//            Query2 = "{CALL UpdateJobStatus(?,?)}";
//            cStmt3 = conn.prepareCall(Query2);
//            cStmt3.setString(1, ComplainNumber);
//            cStmt3.setInt(2, 1);
//            rset3 = cStmt3.executeQuery();
//            rset3.close();
//            cStmt3.close();

            Query2 = "UPDATE CustomerData SET AssignStatus= 1,AssignDate=NOW() WHERE ComplainNumber = '" + ComplainNumber + "' ";
            stmt3 = conn.createStatement();
            stmt3.executeUpdate(Query2);
            stmt3.close();
            LogString += " Update Query in Job Status " + Query2 + " | ";
        } catch (Exception Ex) {
            System.out.println("Update Jobs Status Exception ...");
            DumpException("UpdateJobStatus", "exp", Ex);
        }
    }

    private static void UpdateInitialStatus(Connection conn, String ComplainNumber, int AssignTo) {
        try {
            Query2 = "UPDATE CustomerData SET InitialAssignTo=" + AssignTo + ",InitialAssignDate=NOW() " +
                    " WHERE ComplainNumber = '" + ComplainNumber + "' ";
            stmt3 = conn.createStatement();
            stmt3.executeUpdate(Query2);
            stmt3.close();
            LogString += " Update Query in Initial Status " + Query2 + " | ";
        } catch (Exception Ex) {
            System.out.println("Update Update Initial Status Exception ...");
            DumpException("UpdateInitialStatus", "exp", Ex);
        }
    }

    private static int ltr(Connection conn, String Latitude, String Longtitude, Double Radius, int ComplainId, String ComplainNumber, int SelectedAgentId, int TotalJobCount) {
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";
        int ATechCount = 0;
        String AQuery = "";
        CallableStatement AcStmt = null;
        ResultSet Arset = null;

        HashMap<Integer, Double> LcrUsers = new HashMap<Integer, Double>();

        String distance = "";
        int FlagCheck = 0;
        int assigneAgentid = 0;
        try {
//            Query = "SELECT a.ComplaintId,b.Id,b.Latitude,b.Longtitude,a.ComplaintStatus,a.UserId " +
//                    "FROM Assignment a,CustomerData b WHERE a.complaintid=b.id " +
//                    "AND substr(b.CreatedDate,1,10)=substr(NOW(),1,10) AND a.ComplaintStatus IN (1,2,3,4,5)";
            //Query Changed 3rd June 2018
//            Query = " SELECT a.Latitude,a.Longtitude,b.UserId,d.Latitude,d.Longtitude " +
//                    " FROM CustomerData a " +
//                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus = 0 AND b.UserId = " + SelectedAgentId + " " +
//                    " STRAIGHT_JOIN MobileUsers c ON b.UserId = c.Id " +
//                    " STRAIGHT_JOIN TechnicianLocation d ON c.UserId=d.UserId AND d.CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId=c.UserId) ";
            //Pick only technician's last assigned complain location
            //Query Change 11th June 2018
            //This query will only pick last job i.e. assigned to that particular technician on which he hasn't marked 6,7,8 status on jobs
//            Query = " SELECT a.Latitude AS CompLat,a.Longtitude AS CompLon,b.UserId,a.Id " +
//                    " FROM CustomerData a " +
//                    " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus NOT IN (6,7,8) AND b.UserId = " + SelectedAgentId + " " +
//                    " WHERE a.Id = (SELECT max(ComplaintId) FROM Assignment WHERE UserId= " + SelectedAgentId + " AND " +
//                    " ComplaintStatus NOT IN (6,7,8) ) AND substr(b.CreatedDate,1,10)=substr(NOW(),1,10) ";
            //Query Change 12th July 2018
            //Technician Check is removed from the query
            Query = " SELECT a.Latitude AS CompLat,a.Longtitude AS CompLon,b.UserId,a.Id\n" +
                    "FROM CustomerData a , Assignment b WHERE  a.Id = b.ComplaintId \n" +
                    "AND b.ComplaintStatus NOT IN (6,7,8)  AND substr(b.CreatedDate,1,10)=substr(NOW(),1,10) ";

            //19th June 2018
            //This query will pick all jobs that are not marked as 6,7,8 status and will assign acc to min radius compared to un-assigned job
//            Query = "SELECT a.Latitude AS CompLat,a.Longtitude AS CompLon,b.UserId,a.Id\n" +
//                    "FROM CustomerData a\n" +
//                    "STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId AND b.ComplaintStatus NOT IN(6,7,8) \n" +
//                    "AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId) AND b.UserId = 12";


            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                //distance = getDistance1(Latitude.trim(), Longtitude.trim(), rset.getString(1).trim(), rset.getString(2).trim());
                distance = getDistance1(rset.getString(1).trim(), rset.getString(2).trim(), Latitude.trim(), Longtitude.trim());
                Double Distance = Double.parseDouble(distance);
//                System.out.println("Distance in LTR  --> " + Distance + " OF " + rset.getInt(4));

                //Error in Assignment Count Check
                //Need to pass id whose complains are picked
                AQuery = "{CALL AssignmentCount(?)}";
                AcStmt = conn.prepareCall(AQuery);
                AcStmt.setInt(1, rset.getInt(3));
                Arset = AcStmt.executeQuery();
                if (Arset.next()) {
                    ATechCount = Arset.getInt(1);
                }
                Arset.close();
                AcStmt.close();
                LogString += " Selected Technician Id  : " + SelectedAgentId + " | ";
                LogString += " ATechCount  : " + ATechCount + " | ";
                if (ATechCount >= TotalJobCount) {
//                    System.out.println("Tech Count  --> " + ATechCount);
//                    System.out.println("Total Job Count  --> " + TotalJobCount);
                    //if ((TotalJobCount > TechCount)) {
                    //Supportive.doLogMethodMessage(null, "Main Method - 001", "No Technician Count  ! ");
                    continue;
                }
                LcrUsers.put(rset.getInt(3), Distance);
                //New Code
                // Sorting Users in hash map
/*                Map<Integer, Double> sortedMapAsc = sortByComparator(LcrUsers, ASC);
                for (Map.Entry<Integer, Double> entry : sortedMapAsc.entrySet()) {
                    //System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
                    LcrUsers.put(entry.getKey(), entry.getValue());
                }*/
/*                Map<Integer, String> map = sortByValues(LcrUsers);
                Set set2 = map.entrySet();
                Iterator iterator2 = set2.iterator();
                while (iterator2.hasNext()) {
                    Map.Entry me2 = (Map.Entry) iterator2.next();
                    System.out.print("AGENT " + me2.getKey() + ": ");
                    System.out.println(me2.getValue());
                }*/
/*                Set<Map.Entry<Integer, Double>> set = LcrUsers.entrySet();
                List<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(set);

                Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
                    public int compare(Map.Entry<Integer, Double> o1,
                                       Map.Entry<Integer, Double> o2) {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                });

                for (Map.Entry<Integer, Double> entry : list) {
                    System.out.println("NEW SORTED VAL " + entry.getValue());
                    LcrUsers.put(entry.getKey(), entry.getValue());

                }*/
            }
            rset.close();
            stmt.close();


            Double value = 0.0;
            Double minValue = Double.MAX_VALUE;
            Double TabVal = Double.MIN_VALUE;
            for (Integer key : LcrUsers.keySet()) {
                value = LcrUsers.get(key);
//                System.out.println("LTR KEY  --> " + key);
//                System.out.println("LTR VAL --> " + value);
//                System.out.println("LTR Radius --> " + Radius);
//                System.out.println("LTR Min Val --> " + minValue);

                if (value <= Radius) {
                    LogString += " IN LTR Radius Check : " + value + " | ";
                    minValue = value;
                    assigneAgentid = key;
                    System.out.println("LTR ASSIGNED " + minValue + " ------------ " + Radius + " ****** " + assigneAgentid + " * * * * " + value);
                    LogString += " IN LTR ASSIGNED : " + minValue + " | ";
                    LogString += " IN LTR ASSIGNED Radius : " + Radius + " | ";
                    LogString += " IN LTR ASSIGNED Technician Id : " + assigneAgentid + " | ";
                    LogString += " IN LTR ASSIGNED value : " + value + " | ";
                    String Result = RecordInsertion(conn, assigneAgentid, ComplainId, "Admin", minValue, 0);
                    //Logs Insertion
                    InsertionLogs(conn, assigneAgentid, ComplainId, "Admin", minValue, 0);
                    TechnicianAssigning(assigneAgentid, ComplainId, value);
                    if (Result.equals("Success")) {
                        //Updating only those jobs who has been assigned to any tech only. Rest will not be set to assigned
                        UpdateJobStatus(conn, ComplainNumber);
                        UpdateInitialStatus(conn, ComplainNumber, assigneAgentid);
//                        System.out.println("Record Assigned".trim());
                    } else {
                        System.out.println("Error while Record Assignment!!");
                        doLogMethodMessage("Ali - Bhai Function ", "Error while Record Assignment!! " + Result);
                    }
//                    return assigneAgentid;
                }
            }
            //12 July 2018 Commented this line
            //return assigneAgentid;

        } catch (Exception Ex) {
            System.out.println("Error In Updation " + Ex.getMessage());
            doLogMethodMessage("Ali - Bhai Function ", "Error In Updation  ! ");
        }
        return assigneAgentid;
    }

    private static void TechnicianAssigning(int UserId, int ComplainId, double NewDistance) {
        PreparedStatement pStmt = null;
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";
        int maxAssignId = 0;
        Connection conn = getConnection();
        try {
            Query = "SELECT max(Id) FROM Assignment WHERE ComplaintId = " + ComplainId;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                maxAssignId = rset.getInt(1);
            rset.close();
            stmt.close();

            Query = "UPDATE Assignment SET isSameLocation = 1 WHERE Id = " + maxAssignId;
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();

            pStmt = conn.prepareStatement(
                    "INSERT INTO TechnicianAssigning(UserId, ComplaintId, Status, CreatedDate,Distance) " +
                            "VALUES(?,?,0,NOW(),?)");

            pStmt.setInt(1, UserId);
            pStmt.setInt(2, ComplainId);
            pStmt.setDouble(3, NewDistance);

            pStmt.executeUpdate();
            pStmt.close();
        } catch (Exception E) {

        }
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    private static Map<Integer, Double> sortByComparator(Map<Integer, Double> unsortMap, final boolean order) {

        List<Map.Entry<Integer, Double>> list = new LinkedList<Map.Entry<Integer, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
            public int compare(Map.Entry<Integer, Double> o1,
                               Map.Entry<Integer, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<Integer, Double> sortedMap = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static void printMap(Map<Integer, Double> map) {
        for (Map.Entry<Integer, Double> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
    }

    public static void doLogMethodMessage(String Method, String Message) {
        try {
            String FileName = GetExceptionsLogPath(1) + GetExceptionFileName();
            java.util.Date dt = new java.util.Date();
            FileWriter fr = new FileWriter(FileName, true);
            fr.write(dt.toString() + " -- " + Method + " -- " + Message + "\r\n");
            fr.write("\r\n");
            fr.flush();
            fr.close();
        } catch (Exception e) {
        }
    }

    public static String GetExceptionsLogPath(int Status) {
        String path = Status == 1 ? "/opt/Htmls/TAMAPP/ProcedureLogs/Exceptions/" : "/opt/Htmls/TAMAPP/ProcedureLogs/DailyLogs/";
        return path;
    }

    private static String GetExceptionFileName() {
        // File Name consist of Date
        // Format YYYY_MM_DD.log
        int temp = 0;

        try {
            java.util.Date dt = GetDate();
            NumberFormat nf = new DecimalFormat("#00");
            return nf.format(dt.getYear() + 1900) + "_" + nf.format(dt.getMonth() + 1) + "_" + nf.format(dt.getDate()) + ".log";
        } catch (Exception e) {
            return "invalid filename " + e.getMessage();
        }
    }

    private static Date GetDate() {
        try {
            java.util.Date dt = new java.util.Date();
            return dt;
        } catch (Exception e) {
            return null;
        }
    }

    public static void LogString(String Message) {
        try {
            String FileName = GetExceptionsLogPath(2) + GetExceptionLogFileName() + "_Log_File.log";
            java.util.Date dt = new java.util.Date();
            FileWriter fr = new FileWriter(FileName, true);
            fr.write(dt.toString() + " -- " + Message + "\r\n");
            fr.write("\r\n");
            fr.flush();
            fr.close();
        } catch (Exception e) {
        }
    }

    private static String GetExceptionLogFileName() {
        // File Name consist of Date
        // Format YYYY_MM_DD.log
        int temp = 0;

        try {
            java.util.Date dt = GetDate();
            NumberFormat nf = new DecimalFormat("#00");
            return nf.format(dt.getYear() + 1900) + "_" + nf.format(dt.getMonth() + 1) + "_" + nf.format(dt.getDate());
        } catch (Exception e) {
            return "invalid filename " + e.getMessage();
        }
    }
}
