package FalconSchedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.simple.JSONObject;

@SuppressWarnings("Duplicates")
public class FalconNotificationsOLD {
    private static Connection conn = null;
    private static Statement stmt = null;
    private static ResultSet rset = null;
    private static String Query = "";
    private static String connect_string = "jdbc:mysql://203.130.0.228/Falcon?user=engro&password=smarttelecard";
    private static String DRIVER = "com.mysql.jdbc.Driver";

    // Method to send Notifications from server to client end
    public static void main(String[] args) throws Exception {

        try {

            if (AppAlreadyRunning()) {
                System.out.println("Unable to start MSG Engine,  Process Already Running ");
                return;
            }

            Class.forName(DRIVER).newInstance();
            conn = DriverManager.getConnection(connect_string);
        } catch (Exception e) {
            conn = null;
            System.out.println("Exception excp conn: " + e.getMessage());
            return;
        }

        int i = 0;
        Query = "SELECT CustName,ComplainNumber,RegistrationNum FROM CustomerData WHERE isNotified=0 ";
        try {
            while (i == 0) {
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                while (rset.next()) {
                    // TODO Auto-generated method stub
                    pushFCMNotification(rset.getString(1), rset.getString(2) , rset.getString(3),conn);
                }
                rset.close();
                stmt.close();
                System.out.println("Going for Sleep");
                Thread.sleep(10000);
                System.out.println("Start Again");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void pushFCMNotification(String DeviceIdKey, String UniqueId, String UniqueId1, Connection FuncCon) throws Exception {
        stmt = null;
        rset = null;
        Query = "";

        final String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
        final String SERVER_KEY_FCM = "AAAAfQ4xSiI:APA91bHc948F80C73r5SUh-u9TttqalLEQCJWSPWk5sPNCs8fPHXZ0JLAVQ2MEOEVnpSztDuGcpLQNJEE22mNu1tjZbHKml5_GoQhdFHLAn8iO4P033uKxc2-ZbvyJjMAgkKw-Tkn_lu";

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
            data.put("to", DeviceIdKey.trim());
            JSONObject info = new JSONObject();
            info.put("title", "Ticket Assigning"); // Notification title
            info.put("body", "New Ticket has assigned to you"); // Notification body
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

    private static boolean AppAlreadyRunning() {
        String s = null;
        int count = 0;
        try {
            Process p = Runtime.getRuntime().exec("ps aux");
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            while ((s = stdInput.readLine()) != null) {
                if (s.contains("FalconSchedulers.FalconNotificationsOLD")) count++;

            }
            if (count > 1)
                return true;
            else
                return false;
        } catch (Exception e) {
            System.out.println("Exception in SendSMSAppAlreadyRunning func. " + e.getMessage());
            return false;
        }
    }
}
