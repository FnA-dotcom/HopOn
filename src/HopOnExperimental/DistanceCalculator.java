package HopOnExperimental;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

@SuppressWarnings("Duplicates")
public class DistanceCalculator {
    private static Connection conn = null;
    private static Statement stmt = null;
    private static ResultSet rset = null;
    private static String Query = "";

    public static void main(String[] args) throws Exception {
        try {
            String DRIVER = "com.mysql.jdbc.Driver";
            Class.forName(DRIVER).newInstance();
            String connect_string = "jdbc:mysql://127.0.0.1/HopOn?user=root&password=Judean123";
            conn = DriverManager.getConnection(connect_string);
        } catch (Exception e) {
            conn = null;
            System.out.println("Exception excp conn: " + e.getMessage());
            return;
        }

        Double myLoc = 0.0d;
        Double calculateDistance = 0.0d;
        try {
            Query = "SELECT a.ChildName, a.SchoolIndex, b.SchoolName, a.PickupLat, a.PickupLon, a.DropOffLat, a.DropOffLon, d.RunID\n" +
                    "FROM Childrens a\n" +
                    "INNER JOIN School b ON a.SchoolIndex = b.Id\n" +
                    "INNER JOIN StudentWiseRun c ON a.Id = c.StudentIndex\n" +
                    "INNER JOIN Runs d ON c.RunIndex = d.Id AND d.Id = 1 \n" +
                    "WHERE a.ParentIndex = 1";
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                myLoc = 0.0;
                System.out.println("Pickup Lat " + rset.getDouble(4));
                System.out.println("Pickup Lon " + rset.getDouble(5));
                System.out.println("DropOff Lat " + rset.getDouble(6));
                System.out.println("DropOff Lon " + rset.getDouble(7));

                myLoc = calculateDistance(rset.getDouble(4), rset.getDouble(5), rset.getDouble(6), rset.getDouble(7));
                System.out.println("myLoc " + myLoc);
                calculateDistance += myLoc;
                System.out.println("calculateDistance " + calculateDistance);
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            System.out.println("First Exception  " + e.getMessage());
        }

        System.out.println("Distance Location is " + myLoc);
    }

    private static Double calculateDistance(Double pLat, Double pLon, Double dLat, Double dLon) {
        final int R = 6371; // Radious of the earth

        Double latDistance = toRad(dLat - pLat);
        Double lonDistance = toRad(dLon - pLon);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(pLat)) * Math.cos(toRad(dLat)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double distance = R * c;
        DecimalFormat f = new DecimalFormat("##.00");
        distance = Double.valueOf(f.format(distance));
        return distance;

    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }
}
