package FalconSchedulers;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.google.gson.annotations.SerializedName;
import javax.annotation.Generated;

@SuppressWarnings("Duplicates")
public class FalconNotifications {
    private static Connection conn = null;
    private static Statement stmt = null;
    private static ResultSet rset = null;
    private static String Query = "";
    private static String connect_string = "jdbc:mysql://203.130.0.228/Falcon?user=engro&password=smarttelecard";
    private static String DRIVER = "com.mysql.jdbc.Driver";

    public static void main(String[] args) {
        try {
            Class.forName(DRIVER).newInstance();
            conn = DriverManager.getConnection(connect_string);
        } catch (Exception e) {
            conn = null;
            System.out.println("Exception excp conn: " + e.getMessage());
            return;
        }

        Query = "SELECT a.CustName,a.ComplainNumber,a.RegistrationNum,b.UserId,c.FBTokenId,a.PhoneNo \n" +
                "FROM CustomerData a \n" +
                " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
                " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)\n" +
                " STRAIGHT_JOIN UserBindageFB c ON b.UserId = c.UserId \n" +
                " WHERE a.isNotified=1 ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                // TODO Auto-generated method stub
                pushFCMNotification(rset.getString(1),rset.getString(2),rset.getString(3),rset.getInt(4),rset.getString(5),rset.getString(6));
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void pushFCMNotification(String CustomerName,String ComplainNumber,String RegistrationNumber,int UserId,String FBTokeId,String PhoneNumber) throws Exception {
        stmt = null;
        rset = null;
        Query = "";

        final String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
//        final String SERVER_KEY_FCM = "AAAAfQ4xSiI:APA91bHc948F80C73r5SUh-u9TttqalLEQCJWSPWk5sPNCs8fPHXZ0JLAVQ2MEOEVnpSztDuGcpLQNJEE22mNu1tjZbHKml5_GoQhdFHLAn8iO4P033uKxc2-ZbvyJjMAgkKw-Tkn_lu";
        final String SERVER_KEY_FCM = "AAAAelEk6E8:APA91bFnuyiQhKIevOQTPHBARSWINI9C7YyUtOBAdHw2TksLwG9j59pOmDAZpfmSrlfZmqT5glJGR6cWiPYtFYQyOcDw1f5a0RqjXejDERs_x9Urd07yrF3w71AvnP_ErTjMlgqVfzRPVbAmTYsNj9w9QEUM91mqZw";

        try{
            URL url = new URL(API_URL_FCM);
            HttpURLConnection HttpConn = (HttpURLConnection) url.openConnection();

            HttpConn.setUseCaches(false);
            HttpConn.setDoInput(true);
            HttpConn.setDoOutput(true);
            HttpConn.setRequestMethod("POST");
            HttpConn.setRequestProperty("Authorization", "key=" + SERVER_KEY_FCM);
            HttpConn.setRequestProperty("Content-Type", "application/json");
            JSONObject data = new JSONObject();
            data.put("to", FBTokeId.trim());
            JSONObject info = new JSONObject();
            info.put("title", "Job Assignment"); // Notification title
            info.put("body", "New Ticket has been assigned to you"); // Notification body
            info.put("sound", "default"); // Notification Sound
            data.put("notification", info);
            System.out.println(data.toJSONString());
            OutputStreamWriter wr = new OutputStreamWriter(HttpConn.getOutputStream());
            wr.write(data.toString());
            wr.flush();
            wr.close();
            int responseCode = HttpConn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(HttpConn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }
        catch(Exception e){
            System.out.print(e.getMessage());
        }
    }

}
