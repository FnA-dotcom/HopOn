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
import java.sql.*;
import java.util.Properties;

@SuppressWarnings("Duplicates")
public class RecordInsertion extends HttpServlet {
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
//        String connect_string = supp.GetConnectString();
        String UserId;
        try {
            try {
//                Class.forName("com.mysql.jdbc.Driver");
//                conn = DriverManager.getConnection(connect_string);
            } catch (Exception var14) {
                conn = null;
                out.println("Exception excp conn: " + var14.getMessage());
            }

            if (req.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            Action = req.getParameter("ActionID");
            if (Action.equals("JobsInsertion")) {
                JobsInsertion(req, out, conn);
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

    @SuppressWarnings("JpaQueryApiInspection")
    private void JobsInsertion(HttpServletRequest req, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = "";
        cStmt = null;
        String EmailFormat = "";

        String RegistrationNumber = req.getParameter("RegNo").trim();
        String CustomerName = req.getParameter("CustName").trim();
        CustomerName = CustomerName.replace("-", " ");
        String CellNo = req.getParameter("Cell");
        CellNo = CellNo.replace("-", " ");
        String PhoneNo = req.getParameter("PhoneNo").trim();
        PhoneNo = PhoneNo.replace("-", " ");
        String Make = req.getParameter("Make").trim();
        Make = Make.replace("-", " ");
        String Model = req.getParameter("Model").trim();
        Model = Model.replace("-", " ");
        String Color = req.getParameter("Color").trim();
        Color = Color.replace("-", " ");
        String ChassisNumber = req.getParameter("ChassisNo").trim();
        ChassisNumber = ChassisNumber.replace("-", " ");
        String JobType = req.getParameter("JobType").trim();
        int JobTypeIndex = Integer.parseInt(JobType);
        //JobType = JobType.replace("-"," ");
        String DeviceNumber = req.getParameter("DeviceNumber").trim();
        DeviceNumber = DeviceNumber.replace("-", " ");
        String Insurance = req.getParameter("Insurance").trim();
        Insurance = Insurance.replace("-", " ");
        String JobNature = req.getParameter("JobNature").trim();
        int JobNatureIndex = Integer.parseInt(JobNature);
        //JobNature = JobNature.replace("-", " ");
        String Latitude = req.getParameter("Latitude").trim();
        String Longitude = req.getParameter("Longitude").trim();
        String Address = req.getParameter("Address").trim();
        Address = Address.replace("-", " ");
        int CityIndex = 0;
        CityIndex = req.getParameter("CityIndex") == null ? 0 : Integer.parseInt(req.getParameter("CityIndex"));
        String ScheduledDate = req.getParameter("ScheduledDate").trim() == null ? "0000-00-00" : req.getParameter("ScheduledDate").trim();
        String ScheduledTime = req.getParameter("ScheduledTime").trim() == null ? "00:00:00" : req.getParameter("ScheduledTime").trim();

        String _tkt = "";
        String TicketNo = "";
        String CurrDate = "";
/*        int JobTypeIndex = 0;
        try {
            Query = "{CALL Job_Type_Index(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, JobType);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                JobTypeIndex = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (SQLException ex) {
            out.println("EXCEPTION 1 <BR>");
            try {
                Parsehtm Parser = new Parsehtm(req);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }

            Supportive.doLog(this.getServletContext(), "Record Insertion-01", ex.getMessage(), ex);
            out.flush();
            out.close();
        }*/


/*        int JobNatureIndex = 0;
        try {
            Query = "{CALL Job_Nature_Index(?)}";
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, JobNature);
            rset = cStmt.executeQuery();
            if (rset.next()) {
                JobNatureIndex = rset.getInt(1);
            }
            rset.close();
            cStmt.close();
        } catch (SQLException ex) {
            out.println("EXCEPTION 2 <BR>");
            try {
                Parsehtm Parser = new Parsehtm(req);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            Supportive.doLog(this.getServletContext(), "Record Insertion-02", ex.getMessage(), ex);
            out.flush();
            out.close();
        }*/
        EmailFormat = "<table id=Table1  border=0 align='justify' width='650'><tr>" +
                "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Registration Number : " + RegistrationNumber + " </font></div></td></tr>"
                + "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Scheduled Date : " + ScheduledDate + " </font></div></td></tr>"
                + "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>Scheduled Time : " + ScheduledTime + " </font></div></td></tr>" +
                "<tr><td  align='justify'><div><font face=Arial size=2 align='justify'>City Index : " + CityIndex + " </font></div></td></tr>" +
                "</table>";
        Query = "SELECT concat('JN#',date_format(now(),'%d%m%y')) ";

        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                _tkt = rset.getString(1);
            }

            rset.close();
            stmt.close();
        } catch (SQLException ex) {
            out.println("EXCEPTION 3 <BR>");

            int EmailVal = SendEmail("Error while data syncing", "Error while Creating JN #", EmailFormat);

            try {
                PreparedStatement preparedStatement = conn.prepareStatement(
                        "INSERT INTO ErrorRecordInsertion (RegistrationNum, CityIndex, ScheduledDate, ScheduledTime, CreatedDate,EmailCount) " +
                                "VALUES (?,?,?,?,NOW(),?) ");
                preparedStatement.setString(1, RegistrationNumber);
                preparedStatement.setInt(2, CityIndex);
                preparedStatement.setString(3, ScheduledDate);
                preparedStatement.setString(4, ScheduledTime);
                preparedStatement.setInt(5, EmailVal);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                Parsehtm Parser = new Parsehtm(req);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            Supportive.doLog(this.getServletContext(), "Record Insertion-03", ex.getMessage(), ex);
            out.flush();
            out.close();
        }

        Query = "SELECT SUBSTRING(IFNULL(MAX(Convert(Substring(ComplainNumber,10,4) ,UNSIGNED INTEGER)),0)+10001,2,4)  " +
                "FROM CustomerData WHERE substring(ComplainNumber,1,9)='" + _tkt + "' ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                TicketNo = _tkt + rset.getString(1);
            }
            rset.close();
            stmt.close();
        } catch (SQLException ex) {
            out.println("EXCEPTION 4 <BR>");
            int EmailVal = SendEmail("Error while data syncing", "Error while Generating Ticket Number", EmailFormat);

            try {
                PreparedStatement preparedStatement = conn.prepareStatement(
                        "INSERT INTO ErrorRecordInsertion (RegistrationNum, CityIndex, ScheduledDate, ScheduledTime, CreatedDate,EmailCount) " +
                                "VALUES (?,?,?,?,NOW(),?) ");
                preparedStatement.setString(1, RegistrationNumber);
                preparedStatement.setInt(2, CityIndex);
                preparedStatement.setString(3, ScheduledDate);
                preparedStatement.setString(4, ScheduledTime);
                preparedStatement.setInt(5, EmailVal);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                Parsehtm Parser = new Parsehtm(req);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            Supportive.doLog(this.getServletContext(), "Record Insertion-04", ex.getMessage(), ex);
            out.flush();
            out.close();
        }

        Query = "{CALL CurrentDate()}";
        try {
            cStmt = conn.prepareCall(Query);
            rset = cStmt.executeQuery();
            if (rset.next())
                CurrDate = rset.getString(1).trim();
            rset.close();
            cStmt.close();
        } catch (SQLException ex) {
            out.println("EXCEPTION 6 <BR>");
            int EmailVal = SendEmail("Error while data syncing", "Error while Fetching Current Date", EmailFormat);

            try {
                PreparedStatement preparedStatement = conn.prepareStatement(
                        "INSERT INTO ErrorRecordInsertion (RegistrationNum, CityIndex, ScheduledDate, ScheduledTime, CreatedDate,EmailCount) " +
                                "VALUES (?,?,?,?,NOW(),?) ");
                preparedStatement.setString(1, RegistrationNumber);
                preparedStatement.setInt(2, CityIndex);
                preparedStatement.setString(3, ScheduledDate);
                preparedStatement.setString(4, ScheduledTime);
                preparedStatement.setInt(5, EmailVal);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                Parsehtm Parser = new Parsehtm(req);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            Supportive.doLog(this.getServletContext(), "Record Insertion-06", ex.getMessage(), ex);
            out.flush();
            out.close();
        }

        Query = "{CALL Insertion_of_Jobs(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
        try {
            cStmt = conn.prepareCall(Query);
            cStmt.setString(1, RegistrationNumber);
            cStmt.setString(2, CustomerName);
            cStmt.setString(3, CellNo);
            cStmt.setString(4, PhoneNo);
            cStmt.setString(5, Make);
            cStmt.setString(6, Model);
            cStmt.setString(7, Color);
            cStmt.setString(8, ChassisNumber);
            cStmt.setString(9, Address);
            cStmt.setInt(10, JobTypeIndex);
            cStmt.setString(11, DeviceNumber);
            cStmt.setString(12, Insurance);
            cStmt.setInt(13, JobNatureIndex);
            cStmt.setString(14, Latitude);
            cStmt.setString(15, Longitude);
            cStmt.setString(16, TicketNo);
            cStmt.setInt(17, 0);
            cStmt.setString(18, CurrDate);
            cStmt.setInt(19, CityIndex);
            cStmt.setString(20, ScheduledDate);//ScheduledDate
            cStmt.setString(21, ScheduledTime);//ScheduledTime
            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();

            out.println("SUCCESS ");
        } catch (SQLException ex) {
            out.println("EXCEPTION 5 <BR>");
            int EmailVal = SendEmail("Error while data syncing", "Error Main Insertion", EmailFormat);

            try {
                PreparedStatement preparedStatement = conn.prepareStatement(
                        "INSERT INTO ErrorRecordInsertion (RegistrationNum, CityIndex, ScheduledDate, ScheduledTime, CreatedDate,EmailCount) " +
                                "VALUES (?,?,?,?,NOW(),?) ");
                preparedStatement.setString(1, RegistrationNumber);
                preparedStatement.setInt(2, CityIndex);
                preparedStatement.setString(3, ScheduledDate);
                preparedStatement.setString(4, ScheduledTime);
                preparedStatement.setInt(5, EmailVal);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                Parsehtm Parser = new Parsehtm(req);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            Supportive.doLog(this.getServletContext(), "Record Insertion-05", ex.getMessage(), ex);
            out.flush();
            out.close();
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
