package HopOn;

/**
 * Created by Siddiqui on 9/12/2017.
 */

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Properties;

//import javax.http.*;

@SuppressWarnings("Duplicates")
public class Supportive extends HttpServlet {

    /**
     * Get Servlet information
     *
     * @return java.lang.String
     */
//	public String GetConnectString()
//	{
//		return "jdbc:mysql://127.0.0.1/HR?user=TESNew&password=smarttelecard";
//	}
/*    public String GetConnectString() {
        //Live IP
        //return "jdbc:mysql://203.130.0.228/Falcon?user=engro&password=smarttelecard";

        //Testing IP
        //return "jdbc:mysql://203.130.0.235/FalconTesting?user=tabish&password=tpassword";


        //Local-IP
        //return "jdbc:mysql://127.0.0.1/HopOn?user=root&password=Judean123";

        //For HopOn Project
        return "jdbc:mysql://mobiledb/HopOn?user=909090XXXZZZ&password=990909090909ABC";
    }*/
    public static Connection getMysqlConn(ServletContext context) {
        try {
            String mysql_server = context.getInitParameter("mysql_server");
            String mysqlusr = context.getInitParameter("mysqlusr");
            String mysqlpwd = context.getInitParameter("mysqlpwd");
            String mysql_dbuser = context.getInitParameter("mysql_dbuser");
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            return DriverManager.getConnection("jdbc:mysql://" + mysql_server + "/" + mysql_dbuser + "?user=" + mysqlusr + "&password=" + mysqlpwd + "");
        } catch (Exception ex) {
            return null;
        }
    }

    //Get Cookie Name and returns its value
    // returns null if specified cookie not found in the request object
    public static String GetCookie(String CookieToSearch, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null)
            return null;

        for (int coky = 0; coky < cookies.length; coky++) {
            String cName = cookies[coky].getName();
            String cValue = cookies[coky].getValue();

            if (cName.equals(CookieToSearch))
                return cValue;
        }  // End For

        return null;
    }

    public static String GetLogPath(ServletContext servletContext) {
        //String path = "/opt/htmls/kesc/pics/";
        //String path = "d:\\HTMLS\\kesc\\pics\\";

        String path = servletContext.getInitParameter("log_path");
        return path;
    }

    public static String GetXMLPath(ServletContext servletContext) {
        String path = servletContext.getInitParameter("xml_path");
        return path;
    }

    public static void doLog(ServletContext servletcontext, String Method, String Message, Exception exp) {
        try {
            String FileName = GetLogPath(servletcontext) + GetExceptionFileName();
            java.util.Date dt = new java.util.Date();
            FileWriter fr = new FileWriter(FileName, true);
            fr.write(dt.toString() + " -- " + Method + " -- " + Message + "\r\n");
            PrintWriter printwriter = new PrintWriter(fr, true);
            exp.printStackTrace(printwriter);
            fr.write("\r\n");
            fr.flush();
            fr.close();
        } catch (Exception e) {
        }
    }

    public static void doLogMethodMessage(ServletContext servletcontext, String Method, String Message) {
        try {
            String FileName = GetLogPath(servletcontext) + GetExceptionFileName();
            java.util.Date dt = new java.util.Date();
            FileWriter fr = new FileWriter(FileName, true);
            fr.write(dt.toString() + " -- " + Method + " -- " + Message + "\r\n");
            fr.write("\r\n");
            fr.flush();
            fr.close();
        } catch (Exception e) {
        }
    }

    public static void LogString(ServletContext servletcontext, String Message) {
        try {
            String FileName = GetLogPath(servletcontext) + GetExceptionLogFileName() + "_Log_File.log";
            java.util.Date dt = new java.util.Date();
            FileWriter fr = new FileWriter(FileName, true);
            fr.write(dt.toString() + " -- " + Message + "\r\n");
            fr.write("\r\n");
            fr.flush();
            fr.close();
        } catch (Exception e) {
        }
    }

    public static void QueryLog(ServletContext servletcontext, String Message) {
        try {
            String FileName = GetLogPath(servletcontext) + GetExceptionQuery() + "_Query_Log.log";
            java.util.Date dt = new java.util.Date();
            FileWriter fr = new FileWriter(FileName, true);
            fr.write(dt.toString() + " -- " + Message + "\r\n");
            fr.write("\r\n");
            fr.flush();
            fr.close();
        } catch (Exception e) {
        }
    }

    public static String GetHtmlPath(ServletContext servletContext) {
        //String path = "/opt/htmls/kesc/";
        //String path = "d:\\HTMLS\\kesc\\";

        String path = servletContext.getInitParameter("html_path");
        return path;
    }

    public static boolean checkSession(PrintWriter out, HttpServletRequest request) {
        if (request.getSession(false) == null) {
            out.println(" <center><table cellpadding=3 cellspacing=2><tr><td bgcolor=\"#FFFFFF\"> " +
                    " <font color=green face=arial><b>Your Session Has Been Expired</b></font> " +
                    " </td></tr></table> " +
                    " <p> " +
                    " <font face=arial size=+1><b><a href=/HopOn/index.html target=_top> Return to Login " +
                    " </a></b></font>" +
                    " <br><font face=arial size=-2>(You will need to sign in again.)</font><br> " +
                    " </center> ");
            return false;
        } else
            return true;
    }

    private static String GetExceptionFilePath(HttpServletRequest request, ServletContext servletContext) {
        //return "/opt/logs/odrs/";
        String path = servletContext.getInitParameter("log_path");
        return path;
    }

    private static String GetExceptionFileName() {
        // File Name consist of Date
        // Format YYYY_MM_DD.log
        int temp = 0;

        try {
            java.util.Date dt = GetDate();
            NumberFormat nf = new DecimalFormat("#00");
            return nf.format(dt.getYear() + 1900) + "_" + nf.format(dt.getMonth() + 1) + "_" + nf.format(dt.getDate()) + ".log";
        } catch (Exception e) {
            return "invalid filename " + e.getMessage();
        }
    }

    private static String GetExceptionQuery() {
        // File Name consist of Date
        // Format YYYY_MM_DD.log
        int temp = 0;

        try {
            java.util.Date dt = GetDate();
            NumberFormat nf = new DecimalFormat("#00");
            return nf.format(dt.getYear() + 1900) + "_" + nf.format(dt.getMonth() + 1) + "_" + nf.format(dt.getDate());
        } catch (Exception e) {
            return "invalid filename " + e.getMessage();
        }
    }

    private static String GetExceptionLogFileName() {
        // File Name consist of Date
        // Format YYYY_MM_DD.log
        int temp = 0;

        try {
            java.util.Date dt = GetDate();
            NumberFormat nf = new DecimalFormat("#00");
            return nf.format(dt.getYear() + 1900) + "_" + nf.format(dt.getMonth() + 1) + "_" + nf.format(dt.getDate());
        } catch (Exception e) {
            return "invalid filename " + e.getMessage();
        }
    }

    private static Date GetDate() {
        try {
            java.util.Date dt = new java.util.Date();
            return dt;
        } catch (Exception e) {
            return null;
        }
    }

    public static void DumException(String ClassName, String FuncName, HttpServletRequest request, Exception exp, ServletContext servletContext) {
        String FileName = "";

        try {
            FileName = GetExceptionFilePath(request, servletContext) + GetExceptionFileName();
            FileWriter fr = new FileWriter(FileName, true);
            fr.write((new java.util.Date()).toString() + "^" + ClassName + "^" + FuncName + "^" + exp.getMessage() + "\r\n");

            PrintWriter pr = new PrintWriter(fr, true);
            exp.printStackTrace(pr);
            fr.write("\r\n");
            fr.flush();
            fr.close();
            pr.close();
        } catch (Exception e) {
        }
    }

    public static void PrintMessages(String Message, String FuncName, HttpServletRequest request, ServletContext servletContext) {
        String FileName = "";

        try {
            FileName = GetExceptionFilePath(request, servletContext) + GetExceptionFileName();
            FileWriter fr = new FileWriter(FileName, true);
            fr.write(FuncName + "^" + Message + "\r\n");

            PrintWriter pr = new PrintWriter(fr, true);
            fr.write("\r\n");
            fr.flush();
            fr.close();
            pr.close();
        } catch (Exception e) {
        }
    }

    public static String GetHtmlPath(String PathNo, ServletContext context) {
        String path = context.getInitParameter("html_path");
        return path;
    }

    /**
     * Initialize global variables
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    /**
     * Process the HTTP Get request
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = new PrintWriter(response.getOutputStream());
        out.println("<html>");
        out.println("<head><title>SupportService</title></head>");
        out.println("<body>Hello From SupportService doGet()");
        out.println("</body></html>");
        out.close();
    }

    /**
     * Process the HTTP Post request
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = new PrintWriter(response.getOutputStream());
        out.println("<html>");
        out.println("<head><title>SupportService</title></head>");
        out.println("<body>");
        out.println("</body></html>");
        out.close();
    }

    public int SendEmail(String eSection, String eSubject, String eBody, String eToEmail, HttpServletRequest request, PrintWriter out, Connection conn) {
        try {
            //String SMTP_SERVER = "132.147.150.11";
            //  String SMTP_SERVER = "132.147.149.1";
            String SMTP_SERVER = "203.130.2.8";

            String subject = eSubject;
            String Body = "";
            boolean debug = false;


            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_SERVER);
            Body = "<center><b><font size= '4'>" + eSection + "</font></b></center>";
            Body = Body + "<center><font size= '2'><br><br>";
            Body = Body + "<table width=100% cellpading=0 cellspacing=0 bgcolor=\"#FFFFFF\">";
            Body = Body + "<tr><td width=100% class=\"fif\" bgcolor=\"#FFFFFF\"><font color=\"#000000\">";
            Body = Body + eBody;
            Body = Body + "</font></td></tr>";
            Body = Body + "</table><br>";


            Session session = Session.getDefaultInstance(props, null);
            session.setDebug(debug);

            Message msg = new MimeMessage(session);
//      if(EmailType.equals("1"))
//      {
            msg.setFrom(new InternetAddress("HRM <tabish.hafeez1@gmail.com>"));
//      }
//      else if(EmailType.equals("2"))
//      {
//    	  msg.setFrom(new InternetAddress("EFU LIFE <cod@efulife.com>"));
//      }
//      if ( eToEmail6.length() > 1 )
//      {
//    	  msg.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(eToEmail6));
//      }
            msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(eToEmail));
            msg.setSubject(subject);
            msg.setContent(Body, "text/html");
            Transport.send(msg);
        } catch (Exception e) {
            System.out.println("Error while Generating Email!!!");
        }
        return 1;
    }
}