package HopOn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PasswordEncryptChange {
    public static void main(String[] args) {
        Connection conn = null;
        String Query = "";
        String Query1 = "";
        Statement stmt = null;
        Statement stmt1 = null;
        ResultSet resultSet = null;
        String Stage = "1";
        try {
            conn = getConnection();
            Query = "SELECT  * FROM SystemUsers ";
            Stage = "2";
            stmt = conn.createStatement();
            Stage = "3";
            resultSet = stmt.executeQuery(Query);
            Stage = "4";
            while (resultSet.next()) {
                String Password = resultSet.getString("Password").trim();
                Stage = "5";
                String passwordEnc = Login.encrypt(Password);
                Stage = "6";
                Query1 = "UPDATE SystemUsers SET Password = '" + passwordEnc + "' WHERE Id = " + resultSet.getInt("Id");
                Stage = "7";
                stmt1 = conn.createStatement();
                stmt1.executeUpdate(Query1);
                stmt1.close();
            }
            resultSet.close();
            stmt.close();
        } catch (Exception Ex) {
            System.out.println("Error While Encrypting" + Ex.getMessage() + "Stage " + Stage);
            return;
        }
    }

    private static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            return DriverManager.getConnection("jdbc:mysql://203.130.0.228/Falcon?user=engro&password=smarttelecard");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
