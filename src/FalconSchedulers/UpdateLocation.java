package FalconSchedulers;

import HopOn.Supportive;
import Parsehtm.Parsehtm;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

@SuppressWarnings("Duplicates")
public class UpdateLocation extends HttpServlet {
    private String Query = "";
    private CallableStatement cStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        Services(req, res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        Services(req, res);
    }

    private void Services(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        PrintWriter out = new PrintWriter(res.getOutputStream());
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();
        String UserId;
        try {

            try {
                conn = Supportive.getMysqlConn(this.getServletContext());
                if (conn == null) {
                    Parsehtm Parser = new Parsehtm(req);
                    Parser.SetField("Error", "Unable to connect. Our team is looking into it!");
                    Parser.GenerateHtml(out, String.valueOf(Supportive.GetHtmlPath(this.getServletContext())) + "index.html");
                    return;
                }
            } catch (Exception excp) {
                conn = null;
                out.println("Exception excp conn: " + excp.getMessage());
            }

            if (req.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            Action = req.getParameter("ActionID");
            if (Action.equals("LocationChange")) {
                LocationChange(req, out, conn);
            } else {
                out.println("Under Development ... " + Action);
            }

            conn.close();
        } catch (Exception var15) {
            out.println("Exception in main... " + var15.getMessage());
            out.flush();
            out.close();
            return;
        }
        out.flush();
        out.close();
    }

    private void LocationChange(HttpServletRequest req, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = "";
        cStmt = null;
        String EmailFormat = "";

        String RegistrationNumber = req.getParameter("RegNo").trim();
        String CustomerName = req.getParameter("CustName").trim();
        CustomerName = CustomerName.replace("-", " ");
        String ChangeLatitude = req.getParameter("Latitude").trim();
        String ChangeLongitude = req.getParameter("Longitude").trim();
        int CityIndex = req.getParameter("CityIndex") == null ? 0 : Integer.parseInt(req.getParameter("CityIndex"));

        EmailFormat = "<table id=Table1  border=0 align='justify' width='650'><tr>" +
                "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Registration Number : " + RegistrationNumber + " </font></div></td></tr>" +
                "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>CustomerName : " + CustomerName + " </font></div></td></tr>" +
                "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Latitude : " + ChangeLatitude + " </font></div></td></tr>" +
                "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Longitude : " + ChangeLongitude + " </font></div></td></tr>" +
                "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>City Index : " + CityIndex + " </font></div></td></tr>" +
                "</table>";

        String PreLatitude = "";
        String PreLongitude = "";
        int ComplainId = 0;
        try {
            Query = "SELECT MAX(Id),IFNULL(Latitude,'-'),IFNULL(Longtitude,'-') FROM CustomerData " +
                    "WHERE UPPER(RegistrationNum) = UPPER('" + RegistrationNumber + "') AND CityIndex = " + CityIndex;
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                ComplainId = rset.getInt(1);
                PreLatitude = rset.getString(2).trim();
                PreLongitude = rset.getString(3).trim();
            }
            rset.close();
            stmt.close();
        } catch (Exception Ex) {
            int EmailVal = SendEmail("Error while Searching Previous Lat & Long : " + Ex.getMessage() + " ", "Error in Location Updation", EmailFormat);

            out.println("Error while Searching Previous Lat & Long " + Ex.getMessage());
            out.close();
            out.flush();
            return;
        }

        if (ComplainId == 0) {
            out.println("No Complain Number Found Against Send Data!! ");
            int EmailVal = SendEmail("No Complain Number Found Against Send Data!! ", "Error in Location Updation", EmailFormat);

            out.close();
            out.flush();
            return;
        }
        try {
            Query = "{CALL Insertion_of_History_of_Location_Change(?,?,?,?,?,?,?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, RegistrationNumber);
            cStmt.setString(2, CustomerName);
            cStmt.setString(3, PreLatitude);
            cStmt.setString(4, PreLongitude);
            cStmt.setString(5, ChangeLatitude);
            cStmt.setString(6, ChangeLongitude);
            cStmt.setInt(7, CityIndex);
            cStmt.setInt(8, ComplainId);

            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();
        } catch (Exception Ex) {
            int EmailVal = SendEmail("Error while Calling Procedure For History Insertion : " + Ex.getMessage() + " ", "Error in Location Updation", EmailFormat);

            out.println("Error while Calling Procedure For History Insertion " + Ex.getMessage());
            out.close();
            out.flush();
            return;
        }

        try {
            Query = "{CALL UpdateLatLon(?,?,?,?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, ChangeLatitude);
            cStmt.setString(2, ChangeLongitude);
            cStmt.setInt(3, ComplainId);
            cStmt.setInt(4, CityIndex);

            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();

/*            Query = "UPDATE CustomerData SET Latitude='" + ChangeLatitude + "',Longtitude='" + ChangeLongitude + "' " +
                    "WHERE Id = " + ComplainId + " AND CityIndex = " + CityIndex + " ";
            stmt = conn.createStatement();
            stmt.executeUpdate(Query);
            stmt.close();*/

            out.println("SUCCESS!!");
        } catch (Exception Ex) {
            int EmailVal = SendEmail("Error while Calling ProcedureFor Updation Location : " + Ex.getMessage() + " ", "Error in Location Updation", EmailFormat);

            out.println("Error while Calling ProcedureFor Updation Location " + Ex.getMessage());
            out.close();
            out.flush();
            return;
        }
    }

    public int SendEmail(String eSection, String eSubject, String eBody) {
        String Email1 = "tabish@supernetesolutions.com.pk";
        String Email2 = "arif.mughal@falconitracking.com";
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
            mailSession.setDebug(true);
            Transport transport = mailSession.getTransport();
            MimeMessage message = new MimeMessage(mailSession);
            message.setContent(Body, "text/html");
            message.setSubject(eSubject);
            message.setFrom(new InternetAddress("SuperNet-E-Solutions <no-reply@supernetesolutions.com.pk>"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(Email1));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(Email2));
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

    private class SMTPAuthenticator extends Authenticator {
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
