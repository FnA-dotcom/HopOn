package FalconSchedulers;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@SuppressWarnings("Duplicates")
public class AutomaticSchedulerOLD {

    CallableStatement cStmt = null;

    public static void main(String[] args) throws Exception {
        String getlocation = "";
/*        String olat = "24.826816";
        String olon = "67.029033";
        String dlat = "24.829744";
        String dlon = "67.033967";
        String getlocation = getdistance(olat, olon, dlat, dlon);
        //System.out.println(getlocation);

        JSONParser parser = new JSONParser();

        Object obj = parser.parse(getlocation);

        JSONObject jsonObject = (JSONObject) obj;

        JSONArray destination_addresses = (JSONArray) jsonObject.get("destination_addresses");
        JSONArray elements = (JSONArray) jsonObject.get("rows");

        for (Object o1 : destination_addresses) {
            System.out.println("FIRST LOOP -- > " + o1);
        }
        String AA = "";
        for (Object o2 : elements) {
            System.out.println("SECOND LOOP --> " + o2);
            AA = o2.toString();
        }


        JSONParser parser1 = new JSONParser();
        Object obj1 = parser1.parse(AA);
        JSONObject jsonObject1 = (JSONObject) obj1;
        JSONArray duration = (JSONArray) jsonObject.get("elements");
        for (Object o3 : elements) {
            System.out.println("THIRD LOOP --> " + o3);
        }

        JSONObject json = (JSONObject) parser.parse(AA);
        JSONArray text = (JSONArray) json.get("elements");
        for (Object aText : text) {
            System.out.println("FOURTH LOOP" + aText);
        }*/

        ArrayList<Double> Miles = new ArrayList<Double>();
        Connection conn = null;
        Date dt1 = new Date();
        long timestamp = dt1.getTime();
        String Query = "";
        String Query1 = "";
        String Query2 = "";
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
        int i = 0;
        String Stage = "";
        DecimalFormat Dformat = new DecimalFormat("##.000");
        System.out.println("Automatic Scheduling STARTS ....[" + StartTime + "] " + timestamp + "\r\n");
        try {
            Stage = "0";
/*            if ( SendProcessAlreadyRunning() ) {
                System.out.println("Unable to start Automatic Scheduler.Process is Already Running ");
                return;
            }*/
            FileWriter filewriter = new FileWriter("/opt/FalconLogs/AutomaticScheduler.log", true);
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
            Stage = "2";
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
            Stage = "3";
            //Jobs to be assigned
            Query = " SELECT Id,JobTypeIndex,JobNatureIndex,ComplainNumber,Latitude,Longtitude " +
                    " FROM CustomerData WHERE STATUS=0 ";
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
                    Stage = "4";
                    System.out.println(" LOOP RUNNING TIME "+ i++);
                    if(Latitude.isEmpty() || Longitude.isEmpty() || Latitude.length() < 1 || Longitude.length() < 1 )
                        continue;
                    System.out.println("Complain Latitude " + Latitude);
                    System.out.println("Complain Longitude " + Longitude);
                    double agentdistance = 0.0d;
                    HashMap<Integer, Double> hm = new HashMap<Integer, Double>();

                    //Determining the Type of the technician
                    //1. Expert
                    //2. Normal
                    // Nature of Job
                    //1. Medium Job
                    //2. Difficult Job
                    //System.out.println("Job Nature Index " + JobNatureIndex);
                    if (JobNatureIndex == 2) {
                        Query1 = " SELECT a.Id,a.TechType,b.Latitude,b.Longtitude FROM MobileUsers a " +
                                " STRAIGHT_JOIN TechnicianLocation b ON a.UserId=b.UserId AND b.CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId=a.UserId) " +
                                " STRAIGHT_JOIN LoginTrail c ON a.UserId=c.UserId AND c.UserType='M' " +
                                " WHERE a.Status=0  ";
                    } else {
                        Query1 = " SELECT a.Id,a.TechType,b.Latitude,b.Longtitude FROM MobileUsers a " +
                                " STRAIGHT_JOIN TechnicianLocation b ON a.UserId=b.UserId AND b.CreatedDate = (SELECT max(x.CreatedDate) FROM TechnicianLocation x WHERE x.UserId=a.UserId) " +
                                " STRAIGHT_JOIN LoginTrail c ON a.UserId=c.UserId AND c.UserType='M' " +
                                " WHERE a.Status=0 ORDER BY a.TechType ";
                    }
                    //System.out.println("Query " + Query1);
                    try {
                        stmt1 = conn.createStatement();
                        rset1 = stmt1.executeQuery(Query1);
                        while (rset1.next()) {
                            System.out.println(" LOOP RUNNING TIME "+ i);
                            Stage = "5";
/*                            System.out.println("Tech Id " + rset1.getInt(1));
                            System.out.println("Complain Latitude " + Latitude);
                            System.out.println("Complain Longitude " + Longitude);
                            System.out.println("Tech Latitude  " + rset1.getString(3));
                            System.out.println("Tech Longitude   " + rset1.getString(4));*/
                            getlocation = getDistance(Latitude.trim(), Longitude.trim(), rset1.getString(3), rset1.getString(4));

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
                            //System.out.println("TechCount " + TechCount + " Of " + rset1.getInt(1));
                            //System.out.println("TotalJobCount " + TotalJobCount);
                            if (TechCount >= TotalJobCount)
                                continue;
                            Stage = "7";

                            //System.out.println("TotalJobCount " + Stage);
                            JSONParser parser = new JSONParser();
                            Object obj = parser.parse(getlocation);

                            JSONObject jsonObject = (JSONObject) obj;

                            JSONArray destination_addresses = (JSONArray) jsonObject.get("destination_addresses");
                            JSONArray elements = (JSONArray) jsonObject.get("rows");
                            Stage = "7.1";

                            //System.out.println("Stage " + Stage);
                            //System.out.println("destination_addresses " + destination_addresses);

                            for (Object o1 : destination_addresses) {
                                //System.out.println("FIRST LOOP -- > " + o1);
                            }
                            String AA = "";
                            for (Object o2 : elements) {
                                //System.out.println("SECOND LOOP --> " + o2);
                                AA = o2.toString();
                            }

                            Stage = "8";
                            JSONParser parser1 = new JSONParser();
                            Object obj1 = parser1.parse(AA);
                            JSONObject jsonObject1 = (JSONObject) obj1;
                            JSONArray duration = (JSONArray) jsonObject.get("elements");
                            for (Object o3 : elements) {
                                //System.out.println("THIRD LOOP --> " + o3);
                            }
                            Stage = "9";
                            JSONObject json = (JSONObject) parser.parse(AA);
                            JSONArray text = (JSONArray) json.get("elements");
                            for (Object aText : text) {
                                AA = aText.toString();
                            }
                            //System.out.println("FOURTH LOOP" + AA);
                            Stage = "10";
                            String[] ddata = AA.split(":");
                            //System.out.println(ddata[2]);

                            String[] ddata1 = ddata[2].split(",");
                            System.out.println(ddata1[0]);
                            String FinalDistance = ddata1[0];
                            Double FinalDistancekm = 0.0d;
                            FinalDistance = FinalDistance.replace("\"", "");
                            String[] DistancewithUnit = FinalDistance.split(" ");
                            Double Distance = Double.parseDouble(DistancewithUnit[0]);
                            String DistanceUnit = DistancewithUnit[1];

                            //System.out.println(DistanceUnit);
                            if (DistanceUnit.compareTo("m") == 0) {
                                Distance = Distance / 1000;
                            }
                            //System.out.println(Distance);
                            Stage = "11";

                                       /* for(double MilesString: Miles){
                                            System.out.println(" Actual " + MilesString );
                                            System.out.println(" After Conversion " + Dformat.format( MilesString * 1.609344));
                                        }*/

                            // agentdistance=Double.parseDouble(AA.substring(21,25).trim());


                            i++;
/*                            if (TechCount < TotalJobCount) {
                                System.out.print(rset1.getInt(1) + "|" + Distance);
                                hm.put(rset1.getInt(1), Distance);
                            }*/
                            hm.put(rset1.getInt(1), Distance);
                        }
                        rset1.close();
                        stmt1.close();
                    } catch (Exception e) {
                        System.out.println("Fill Technician Error --> " + e.getMessage() + "--- Stage is = " + Stage);
                        System.out.close();
                        System.out.flush();
                        return;
                    }
                    Stage = "12";
                    int assigneAgentid = 0;
                    Stage = "100";
                    //System.out.println("HASH MAP SIZE " + hm.size());
                    if (hm.size() <= 0)
                        continue;
                    Stage = "13";
                    Double minValue = Double.MAX_VALUE;
                    for (Integer key : hm.keySet()) {
                        Double value = hm.get(key);
                        if (value < minValue) {
                            //System.out.println("KEY  --> " + key);
                            //System.out.println("VAL --> " + value);
                            minValue = value;
                            assigneAgentid = key;
                        }
                    }
                    Stage = "14";
                    //System.out.println("AssignAgent =" + assigneAgentid);
                    //System.out.println("Min Distance =" + minValue);
                    //System.out.println("ComplainId =" + ComplainId);
                    //System.out.println(assigneAgentid);
                    Stage = "101";

                    if (assigneAgentid != 0) {
                        String Result = RecordInsertion(conn, assigneAgentid, ComplainId, "Admin");
                        if (Result.equals("Success"))
                            System.out.println("Record Assigned");
                        else
                            System.out.println("Error while Record Assignment!!");
                    }
                   /* Stage = "15";
                    Query2 = "{CALL UpdateJobStatus(?,?)}";
                    cStmt2 = conn.prepareCall(Query2);
                    cStmt2.setString(1, ComplainNumber);
                    cStmt2.setInt(2, 1);
                    rset2 = cStmt2.executeQuery();
                    rset2.close();
                    cStmt2.close();

                    Query1 = "UPDATE CustomerData SET Status=1 WHERE Id=" + ComplainId;
                    stmt1 = conn.createStatement();
                    //stmt1.executeUpdate(Query1);
                    stmt1.close();
                    */

                }
                rset.close();
                stmt.close();
            } catch (Exception e) {
                System.out.println("Main try/catch " + e.getMessage() + "--- " + Stage);
            }

        } catch (Exception Ex) {
            System.out.println("Update Ends in Exception ....[" + dt.getTime() + "] " + timestamp + "\r\n");

            System.out.println("Outer exception ... " + "-" + Ex.getMessage());
            DumpException("main", "exp", Ex);
        }
    }

    private static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            return DriverManager.getConnection("jdbc:mysql://203.130.0.228/Falcon?user=engro&password=smarttelecard".trim());
        } catch (Exception e) {
            System.out.println(e);
            DumpException("Connection Error", "exp", e);
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
                if (s.contains("FalconSchedulers.AutomaticScheduler".trim())) {
                    count++;
                }
            }
            if (count > 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Exception in SendSMSAppAlreadyRunning func. " + e.getMessage());
        }
        return false;
    }

    private static void DumpException(String method, String message, Exception exception) {
        String s2 = "";
        try {
            FileWriter filewriter = new FileWriter("/opt/FalconLogs/AutomaticScheduler_Exception.log".trim(), true);

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

    private static String RecordInsertion(Connection conn, int UserId, int ComplainId, String AssignBy) {
        PreparedStatement pStmt = null;
        try {
            pStmt = conn.prepareStatement(
                    "INSERT INTO Assignment(UserId, ComplaintId, Status, CreatedDate, RegisterBy,ComplaintStatus, " +
                            "AssignmentStatus,ManualStatus) VALUES(?,?,0,now(),?,0,0,0)");

            pStmt.setInt(1, UserId);
            pStmt.setInt(2, ComplainId);
            pStmt.setString(3, AssignBy);
            pStmt.executeUpdate();
            pStmt.close();

            return "Success";
        } catch (Exception e) {
            System.out.println("Error in insertion " + e.getMessage());
            return "-";
        }
    }

    private static String getDistance(String olat, String olon, String dlat, String dlon) throws IOException {
        String BaseURL = "";
        String reply = "";

        //System.out.println("SMSID= "+smscid+"| SMSPASS= "+smspass+"| Operator= "+operator+"| Mask= "+Mask);
        //BaseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=24.826816,%2067.029033&destinations=24.829744,%2067.033967&key=AIzaSyC_DWDADdI3iz-cG0qIS42-szNnQGdBcU0";
        //BaseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + olat + "," + olon + "&destinations=" + dlat + "," + dlon + "&key=AIzaSyC_DWDADdI3iz-cG0qIS42-szNnQGdBcU0";
        BaseURL = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + olat + "," + olon + "&destinations=" + dlat + "," + dlon + "&key=AIzaSyCiNiJiXwlggLulY3YaJbtv2IylwyL_Bkk".trim();
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
}
