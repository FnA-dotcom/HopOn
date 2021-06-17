package FalconSchedulers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

@SuppressWarnings("Duplicates")
public class FalconNotifications {
    private static Connection conn = null;
    private static Statement stmt = null;
    private static Statement stmt1 = null;
    private static ResultSet rset = null;
    private static String Query = "";
    private static String query1 = "";
    private static String CustomerName;
    private static String ComplainNumber;
    private static String RegistrationNum;
    private static int UserId;
    private static String FBTokenId;
    private static String PhoneNo;
    private static String UserName;
    private static String MobileUserId;

    public static void main(String[] args) {
        try {
            String DRIVER = "com.mysql.jdbc.Driver";
            Class.forName(DRIVER).newInstance();
            String connect_string = "jdbc:mysql://203.130.0.228/Falcon?user=engro&password=smarttelecard";
            conn = DriverManager.getConnection(connect_string);
        } catch (Exception e) {
            conn = null;
            System.out.println("Exception excp conn: " + e.getMessage());
            return;
        }
        int i = 0;
        Query = "SELECT a.CustName,a.ComplainNumber,a.RegistrationNum,b.UserId,c.FBTokenId,IFNULL(a.PhoneNo,'0'),a.Id,d.UserName,d.UserId \n" +
                "FROM CustomerData a \n" +
                " STRAIGHT_JOIN Assignment b ON a.Id = b.ComplaintId \n" +
                " AND b.Id = (SELECT MAX(x.Id) FROM Assignment x WHERE x.ComplaintId = b.ComplaintId)\n" +
                " STRAIGHT_JOIN UserBindageFB c ON b.UserId = c.UserId " +
                " STRAIGHT_JOIN MobileUsers d ON b.UserId = d.Id \n" +
                " WHERE a.isNotified=1 ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            while (rset.next()) {
                CustomerName = "";
                ComplainNumber = "";
                RegistrationNum = "";
                UserId = 0;
                FBTokenId = "";
                PhoneNo = "";
                UserName = "";
                CustomerName = rset.getString(1).trim();
                ComplainNumber = rset.getString(2).trim();
                RegistrationNum = rset.getString(3).trim();
                UserId = rset.getInt(4);
                FBTokenId = rset.getString(5).trim();
                PhoneNo = rset.getString(6).trim();
                UserName = rset.getString(8).trim();
                MobileUserId = rset.getString(9).trim();

                pushFCMNotification(CustomerName, ComplainNumber, RegistrationNum, UserId, FBTokenId, PhoneNo, UserName, MobileUserId);
                //pushFCMNotification(rset.getString(1), rset.getString(2), rset.getString(3), rset.getInt(4), rset.getString(5), rset.getString(6));


                query1 = "UPDATE CustomerData SET isNotified = 2,NotifiedDate = NOW() WHERE Id = " + rset.getInt(7);
                stmt1 = conn.createStatement();
                stmt1.executeUpdate(query1);
                stmt1.close();
            }
            rset.close();
            stmt.close();
        } catch (Exception e) {
            System.out.println("EXCEPTIONS --> " + e.getMessage());
        }
    }

    private static void pushFCMNotification(String CustomerName, String ComplainNumber, String RegistrationNumber, int UserId, String FBTokeId, String PhoneNumber, String UserName, String MobileUserId) {
        String EmailFormat = "";
        final String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
        final String SERVER_KEY_FCM = "AAAAKyNsyFQ:APA91bF1Z08X9A4-tAGcIkzqBtCOR3esWfCpoWzgYcRSgS4E8XtO8Cy0RTzh7Bcnj8klalangR4l1j0kuKUCEKFuoHy2gpsl0E5PKL2Ca4wDARPkJ3cE1BiOQDP3-ROnkgCu0c0_3pkC";

        try {
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
            info.put("body", " Dear Mr." + UserName + ", New Job Has been Assigned to you.\n Please check your Assigned tab \n Job Number : " + ComplainNumber + " \n Customer Name : " + CustomerName + " "); // Notification body
            info.put("sound", "default"); // Notification Sound
            data.put("notification", info);
            System.out.println(data.toJSONString());


            OutputStreamWriter wr = new OutputStreamWriter(HttpConn.getOutputStream());
            wr.write(data.toString());
            wr.flush();
            wr.close();
            HttpConn.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(HttpConn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            int responseCode = HttpConn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            System.out.println(response);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse("[" + response + "]");

            JSONArray array = (JSONArray) obj;
            JSONObject obj2 = (JSONObject) array.get(0);

            long FailureMessage = (Long) obj2.get("failure");
            long SuccessMessage = (Long) obj2.get("success");
            long multicast_id = (Long) obj2.get("multicast_id");

            if (FailureMessage == 1) {
                EmailFormat = "<table id=Table1  border=0 align='justify' width='650'><tr>" +
                        "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Assigned To : " + UserName + " </font></div></td></tr>" +
                        "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Registration Number : " + RegistrationNumber + " </font></div></td></tr>"
                        + "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Complain Number : " + ComplainNumber + " </font></div></td></tr>"
                        + "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Customer Name : " + CustomerName + " </font></div></td></tr>" +
                        "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Phone Number : " + PhoneNumber + " </font></div></td></tr>" +
                        "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Error Message : " + obj2.get("results") + " </font></div></td></tr>" +
                        "</table>";
                int EmailVal = SendEmail("Error while Sending Notification", "Error Main Insertion", EmailFormat);
                try {
                    PreparedStatement MainReceipt = conn.prepareStatement(
                            "INSERT INTO PushNotificationsLoggig (Message, MessageType, Status, CreatedDate, EmailVal,MultiCaseId,ComplainNumber,ResponseCode) " +
                                    "VALUES (?,?,0,NOW(),?,?,?,?) ");
                    MainReceipt.setString(1, response.toString());//Message
                    MainReceipt.setString(2, "ERROR"); //MessageType
                    MainReceipt.setInt(3, EmailVal);//EmailVal
                    MainReceipt.setLong(4, multicast_id); //MultiCaseId
                    MainReceipt.setString(5, ComplainNumber); //ComplainNumber
                    MainReceipt.setInt(6, responseCode);//responseCode
                    MainReceipt.executeUpdate();
                    MainReceipt.close();

                } catch (Exception Ex) {
                    System.out.println("Error Message in For Loop " + Ex.getMessage());
                    conn.close();
                    return;

                }
            } else if (SuccessMessage == 1) {
                try {
                    PreparedStatement MainReceipt = conn.prepareStatement(
                            "INSERT INTO PushNotificationsLoggig (Message, MessageType, Status, CreatedDate, EmailVal,MultiCaseId,AssignTo,ComplainNumber,ResponseCode) " +
                                    "VALUES (?,?,0,NOW(),?,?,?,?,?) ");
                    MainReceipt.setString(1, response.toString());//Message
                    MainReceipt.setString(2, "SUCCESS"); //MessageType
                    MainReceipt.setInt(3, 1);//EmailVal
                    MainReceipt.setLong(4, multicast_id); //MultiCaseId
                    MainReceipt.setInt(5, UserId);//AssignTo
                    MainReceipt.setString(6, ComplainNumber); //ComplainNumber
                    MainReceipt.setInt(7, responseCode);//responseCode
                    MainReceipt.executeUpdate();
                    MainReceipt.close();

                } catch (Exception Ex) {
                    System.out.println("Error Message in For Loop " + Ex.getMessage());
                    conn.close();
                    return;

                }
            }

        } catch (Exception Ex) {
            System.out.print("EXCEPTION CAUGHT " + Ex.getMessage());
        }

    }

    private static void pushFCMNotificationOLD(String CustomerName, String ComplainNumber, String RegistrationNumber, int UserId, String FBTokeId, String PhoneNumber) throws Exception {
        stmt = null;
        rset = null;
        Query = "";
        String EmailFormat = "";
//        final String SERVER_KEY_FCM = "AAAAfQ4xSiI:APA91bHc948F80C73r5SUh-u9TttqalLEQCJWSPWk5sPNCs8fPHXZ0JLAVQ2MEOEVnpSztDuGcpLQNJEE22mNu1tjZbHKml5_GoQhdFHLAn8iO4P033uKxc2-ZbvyJjMAgkKw-Tkn_lu";
        //final String SERVER_KEY_FCM = "AAAAelEk6E8:APA91bFnuyiQhKIevOQTPHBARSWINI9C7YyUtOBAdHw2TksLwG9j59pOmDAZpfmSrlfZmqT5glJGR6cWiPYtFYQyOcDw1f5a0RqjXejDERs_x9Urd07yrF3w71AvnP_ErTjMlgqVfzRPVbAmTYsNj9w9QEUM91mqZw";
//        final String SERVER_KEY_FCM = "AAAAelEk6E8:APA91bG3h9WJJL9HekpiADBuBvLIxEr553AkXPpkWEZEVTNm61zXvXOsi8by20bWGVbbvk4nVksMYI1fAWElMOcs1y6kSKDc-DUi3HoEB4HcF1Uv6gRrnuYKQwZL3XnzlROg37sU3Tvv";
        final String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
        final String SERVER_KEY_FCM = "AAAAKyNsyFQ:APA91bF1Z08X9A4-tAGcIkzqBtCOR3esWfCpoWzgYcRSgS4E8XtO8Cy0RTzh7Bcnj8klalangR4l1j0kuKUCEKFuoHy2gpsl0E5PKL2Ca4wDARPkJ3cE1BiOQDP3-ROnkgCu0c0_3pkC";

        try {
   /*          URL url = new URL(API_URL_FCM);
            HttpURLConnection HttpConn = (HttpURLConnection) url.openConnection();
           System.out.println("Response Code " + HttpConn.getResponseCode());

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
            info.put("body", "New Job Has been Assigned to you.\n Please check your Assigned tab \n Job Number : " + ComplainNumber + " "); // Notification body
            info.put("sound", "default"); // Notification Sound
            data.put("notification", info);
            System.out.println(data.toJSONString());

            OutputStreamWriter wr = new OutputStreamWriter(HttpConn.getOutputStream());
            wr.write(data.toString());
            wr.flush();
            wr.close();
            HttpConn.getInputStream();

            int responseCode = HttpConn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(HttpConn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println(response);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse("[" + response + "]");

            JSONArray array = (JSONArray) obj;
            JSONObject obj2 = (JSONObject) array.get(0);

            long FailureMessage = (Long) obj2.get("failure");
            long SuccessMessage = (Long) obj2.get("success");
            long multicast_id = (Long) obj2.get("multicast_id");

            if (FailureMessage == 1) {
                EmailFormat = "<table id=Table1  border=0 align='justify' width='650'><tr>" +
                        "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Registration Number : " + RegistrationNumber + " </font></div></td></tr>"
                        + "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Complain Number : " + ComplainNumber + " </font></div></td></tr>"
                        + "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Customer Name : " + CustomerName + " </font></div></td></tr>" +
                        "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Phone Number : " + PhoneNumber + " </font></div></td></tr>" +
                        "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Error Message : " + obj2.get("results") + " </font></div></td></tr>" +
                        "</table>";
                int EmailVal = SendEmail("Error while Sending Notification", "Error Main Insertion", EmailFormat);
                try {
                    PreparedStatement MainReceipt = conn.prepareStatement(
                            "INSERT INTO PushNotificationsLoggig (Message, MessageType, Status, CreatedDate, EmailVal,MultiCaseId,ResponseCode) " +
                                    "VALUES (?,?,0,NOW(),?,?,?) ");
                    MainReceipt.setString(1, response.toString());//Message
                    MainReceipt.setString(2, "ERROR"); //MessageType
                    MainReceipt.setInt(3, EmailVal);//EmailVal
                    MainReceipt.setLong(4, multicast_id); //MultiCaseId
                    MainReceipt.setInt(5, responseCode);//EmailVal
                    MainReceipt.executeUpdate();
                    MainReceipt.close();

                } catch (Exception Ex) {
                    System.out.println("Error Message in For Loop " + Ex.getMessage());
                    conn.close();
                    return;

                }
            } else if (SuccessMessage == 1) {
//                System.out.println("IN ELSE");
                try {
                    PreparedStatement MainReceipt = conn.prepareStatement(
                            "INSERT INTO PushNotificationsLoggig (Message, MessageType, Status, CreatedDate, EmailVal,MultiCaseId,AssignTo,ResponseCode) " +
                                    "VALUES (?,?,0,NOW(),?,?,?,?) ");
                    MainReceipt.setString(1, response.toString());//Message
                    MainReceipt.setString(2, "SUCCESS"); //MessageType
                    MainReceipt.setInt(3, 1);//EmailVal
                    MainReceipt.setLong(4, multicast_id); //MultiCaseId
                    MainReceipt.setInt(5, UserId);//AssignTo
                    MainReceipt.setInt(6, responseCode);//EmailVal
                    MainReceipt.executeUpdate();
                    MainReceipt.close();

                } catch (Exception Ex) {
                    System.out.println("Error Message in For Loop " + Ex.getMessage());
                    conn.close();
                    return;

                }
            }*/
        } catch (Exception e) {
            System.out.print("EXCEPTION CAUGHT " + e.getMessage());
        }
    }

    public static int SendEmail(String eSection, String eSubject, String eBody) {
        String Email1 = "tabish@supernetesolutions.com.pk";
        String Email2 = "arif.mughal@falconitracking.com";
        String Email3 = "hasnain@supernetesolutions.com.pk";
        String SMTP_HOST_NAME = "203.130.0.228";
        String Port = "8981";
        String Body = "";
        Body = "<center><b><font size= '4'>" + eSection + "</font></b></center>";
        Body = Body + "<center><font size= '2'><br><br>";
        Body = Body + "<table width=100% cellpading=0 cellspacing=0 bgcolor=\"#FFFFFF\">";
        Body = Body + "<tr><td width=100% class=\"fif\" bgcolor=\"#FFFFFF\"><font color=\"#000000\">";
        Body = Body + eBody;
        Body = Body + "</font></td></tr>";
        Body = Body + "</table><br>";
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", SMTP_HOST_NAME);
        props.put("mail.smtp.port", Port);
        props.put("mail.smtp.auth", "true");

        try {

            Authenticator auth = new SMTPAuthenticator();
            Session mailSession = Session.getInstance(props, auth);
            //mailSession.setDebug(true);
            Transport transport = mailSession.getTransport();
            MimeMessage message = new MimeMessage(mailSession);
            message.setContent(Body, "text/html");
            message.setSubject(eSubject);
            message.setFrom(new InternetAddress("SuperNet-E-Solutions <no-reply@supernetesolutions.com.pk>"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(Email1));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(Email2));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(Email3));
            transport.connect();
            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            transport.close();
            System.out.println("1");
        } catch (Exception var18) {
            System.out.println("Error while Generating Email!!!");
            System.out.println(var18.getMessage());
        }

        return 1;
    }

    private static class SMTPAuthenticator extends Authenticator {
        private SMTPAuthenticator() {
        }

        public PasswordAuthentication getPasswordAuthentication() {
            String SMTP_HOST_NAME = "203.130.0.228";
            String SMTP_AUTH_USER = "amadeus@supernetesolutions.com.pk";
            String SMTP_AUTH_PWD = "Ses123";

            String username = SMTP_AUTH_USER;
            String password = SMTP_AUTH_PWD;
            return new PasswordAuthentication(username, password);
        }
    }
}
