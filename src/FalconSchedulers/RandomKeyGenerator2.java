package FalconSchedulers;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class RandomKeyGenerator2 {
    private static Connection conn = null;
    private static String Query = "";
    private static Statement stmt = null;
    private static ResultSet rset = null;

    public static void main(String[] args) {
        try {
            String DRIVER = "com.mysql.jdbc.Driver";
            Class.forName(DRIVER).newInstance();
            String connect_string = "jdbc:mysql://127.0.0.1/mydb?user=root&password=Judean123";
            conn = DriverManager.getConnection(connect_string);
        } catch (Exception e) {
            conn = null;
            System.out.println("Exception excp conn: " + e.getMessage());
            return;
        }
        // Get the size n
        int n = 0;
        Query = "SELECT SecureNum FROM SecureRandomNumber";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next())
                n = rset.getInt(1);
            rset.close();
            stmt.close();
        } catch (Exception e) {
            conn = null;
            System.out.println("Exception Secure Num: " + e.getMessage());
        }
        String sessionId = getAlphaNumericStringSecond(n) + getAlphaNumericStringThird(n);
        System.out.println("Final String " + sessionId);
    }

    private static String getAlphaNumericStringSecond(int n) {
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new SecureRandom().nextBytes(array);

        String randomString = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuilder r = new StringBuilder();

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))
                    && (n > 0)) {

                r.append(ch);
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }

    private static String getAlphaNumericStringThird(int n) {

        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new SecureRandom().nextBytes(array);

        String randomString = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // remove all spacial char
        String AlphaNumericString = randomString.replaceAll("[^A-Za-z0-9]", "");

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < AlphaNumericString.length(); k++) {

            if (Character.isLetter(AlphaNumericString.charAt(k))
                    && (n > 0)
                    || Character.isDigit(AlphaNumericString.charAt(k))
                    && (n > 0)) {

                r.append(AlphaNumericString.charAt(k));
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }
}
