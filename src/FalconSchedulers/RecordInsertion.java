package FalconSchedulers;

import Parsehtm.Parsehtm;
import TAMAPP.Supportive;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

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
        String connect_string = supp.GetConnectString();
        String UserId;
        try {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(connect_string);
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

    private void JobsInsertion(HttpServletRequest req, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = "";
        cStmt = null;
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
//        String ScheduledDate = req.getParameter("ScheduledDate").trim();
//        String ScheduledTime = req.getParameter("ScheduledTime").trim();

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
            try {
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
            try {
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
            try {
                Parsehtm Parser = new Parsehtm(req);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            Supportive.doLog(this.getServletContext(), "Record Insertion-06", ex.getMessage(), ex);
            out.flush();
            out.close();
        }

        Query = "{CALL Insertion_of_Jobs(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
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
//            cStmt.setString(19, ScheduledDate);
//            cStmt.setString(20, ScheduledTime);
            rset = cStmt.executeQuery();
            rset.close();
            cStmt.close();

            out.println("SUCCESS ");
        } catch (SQLException ex) {
            out.println("EXCEPTION 5 <BR>");
            try {
                Parsehtm Parser = new Parsehtm(req);
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(getServletContext()) + "Exceptions/Exception6.html");
            } catch (Exception var15) {
            }
            Supportive.doLog(this.getServletContext(), "Record Insertion-05", ex.getMessage(), ex);
            out.flush();
            out.close();
        }
    }
}
