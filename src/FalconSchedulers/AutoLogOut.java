package FalconSchedulers;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Date;

@SuppressWarnings("Duplicates")
public class AutoLogOut {
    public static void main(String[] args) throws Exception {
        Connection conn = null;
        Date dt1 = new Date();
        long timestamp = dt1.getTime();
        String Query = "";

        Statement stmt = null;
        Date dt = new Date();
        long StartTime = dt.getTime();

        System.out.println("Automatic Scheduling STARTS ....[" + StartTime + "] " + timestamp + "\r\n");

        try {
            /*            if ( SendProcessAlreadyRunning() ) {
                System.out.println("Unable to start Automatic Scheduler.Process is Already Running ");
                return;
            }*/
            FileWriter filewriter = new FileWriter("/opt/FalconLogs/LogOutLogs/AutoLogOut.log", true);
            filewriter.write("\r\n Scheduler Time starts at " + new Date().toString() + "\r\n");
            conn = getConnection();

            //Query = "DELETE FROM LoginTrail WHERE UserType='M' ";
            Query = "TRUNCATE TABLE LoginTrail";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();
            filewriter.write("\r\n Table Count " + stmt.getUpdateCount() + "\r\n");

            filewriter.flush();
            filewriter.close();
        } catch (Exception e) {
            System.out.println("Update Ends in Exception ....[" + dt.getTime() + "] " + timestamp + "\r\n");
            System.out.println("Outer exception ... " + "-" + e.getMessage());
            DumpException("main", "exp", e);
        }
    }

    private static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            return DriverManager.getConnection("jdbc:mysql://203.130.0.228/Falcon?user=engro&password=smarttelecard");
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
                if (s.contains("FalconSchedulers.AutoLogOut")) {
                    count++;
                }
            }
            if (count > 1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Exception in Auto Service func. " + e.getMessage());
        }
        return false;
    }

    private static void DumpException(String method, String message, Exception exception) {
        String s2 = "";
        try {
            FileWriter filewriter = new FileWriter("/opt/FalconLogs/LogOutLogs/FalconLogOut.log", true);

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
}
