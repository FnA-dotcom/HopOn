package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")
public class ajaxQueries extends HttpServlet {
    public void init(ServletConfig config)
            throws ServletException {
        super.init(config);
    }

    public String ErrorText() {
        return "Error In Forms Section";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        handleRequest(request, response);
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Connection conn = null;

        response.setContentType("text/html");
        PrintWriter out = new PrintWriter(response.getOutputStream());
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();
        try {
            conn = Supportive.getMysqlConn(this.getServletContext());
            if (conn == null) {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("Error", "Unable to connect. Our team is looking into it!");
                Parser.GenerateHtml(out, String.valueOf(Supportive.GetHtmlPath(this.getServletContext())) + "index.html");
                return;
            }
        } catch (Exception excp) {
            conn = null;
            out.println("Exception excp conn: " + excp.getMessage());
        }
        switch (request.getParameter("ActionID")) {
            case "getProvince":
                getProvince(request, response, out, conn);
                break;
            case "getCity":
                getCity(request, response, out, conn);
                break;
            case "getLatLon":
                getLatLon(request, response, out, conn);
                break;
            case "getStudent":
                getStudent(request, response, out, conn);
                break;

            default:
                out.println("Under Development ... ");
                break;
        }
        try {
            conn.close();
        } catch (Exception localException1) {
        }
        out.flush();
        out.close();
    }

    private void getProvince(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn) {
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";
        String CountryIndex = request.getParameter("CountryIndex");

        Query = "SELECT Id,ProvinceName FROM Province WHERE Status=0 AND CountryIndex=" + CountryIndex;
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            out.println("<option value='' selected disabled>Select Province</option>");
            while (rset.next()) {
                out.println("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            stmt.close();
        } catch (Exception localException1) {
        }
        out.flush();
        out.close();
    }

    private void getLatLon(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn) {
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";
        String SchoolIndex = request.getParameter("SchoolIndex");
        String Lat = "";
        String Lon = "";

        Query = "SELECT Latitude,Longitude FROM School WHERE Id =" + SchoolIndex;
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            if (rset.next()) {
                Lat = rset.getString(1).trim();
                Lon = rset.getString(2).trim();
            }
            rset.close();
            stmt.close();
        } catch (Exception localException1) {
        }
        out.println(Lat + "|" + Lon);
        out.flush();
        out.close();
    }

    private void getCity(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn) {
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";
        String CountryIndex = request.getParameter("CountryIndex");
        String ProvinceIndex = request.getParameter("ProvinceIndex");

        Query = "select Id,CityName from City where CountryIndex=" + CountryIndex + " AND ProvinceIndex=" + ProvinceIndex;
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            out.println("<option value='' selected disabled>Select City</option>");
            while (rset.next()) {
                out.println("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
            }
            rset.close();
            stmt.close();
        } catch (Exception localException1) {
        }
        out.flush();
        out.close();
    }

    private void getStudent(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn) {
        Statement stmt = null;
        ResultSet rset = null;
        String Query = "";
        String SchoolIndex = request.getParameter("SchoolIndex");

/*        Query = "SELECT a.ParentIndex,a.SchoolIndex,a.StudentIndex AS BSStudentIndex," +
                "b.SchoolName, c.Id As StudentIndex, c.ChildName " +
                "FROM BindSchoolStudent a " +
                "STRAIGHT_JOIN School b ON a.SchoolIndex = b.Id AND b.`Status` = 0 " +
                "STRAIGHT_JOIN Childrens c ON a.StudentIndex = c.Id AND c.`Status` = 0 " +
                "STRAIGHT_JOIN ChildMatrix d ON a.StudentIndex = d.ChildIndex AND d.`Status` = 0 " +
                "WHERE a.SchoolIndex = "+SchoolIndex+" AND a.Status = 0 ";*/
//New Query has been added as one option has been disabled
        //No need for Child Matrix table. Removing it from the query-- 16-Sept-20
        Query = "SELECT a.ParentIndex,a.SchoolIndex,a.StudentIndex AS BSStudentIndex," +
                "b.SchoolName, c.Id As StudentIndex, c.ChildName " +
                "FROM BindSchoolStudent a " +
                "STRAIGHT_JOIN School b ON a.SchoolIndex = b.Id AND b.`Status` = 0 " +
                "STRAIGHT_JOIN Childrens c ON a.StudentIndex = c.Id AND c.`Status` = 0 " +
                "WHERE a.SchoolIndex = " + SchoolIndex + " AND a.Status = 0 ";
        try {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(Query);
            out.println("<label>Student</label>");
            out.println("<select class=\"form-control m-b\" name=\"StudentIndex\" id=\"StudentIndex\"> ");
            out.println("<option value='' selected disabled>Select Student</option>");
            while (rset.next()) {
                out.println("<option value=" + rset.getInt(3) + ">" + rset.getString(6) + "</option>");
            }
            out.println("</select>");
            rset.close();
            stmt.close();
        } catch (Exception localException1) {
        }
        out.flush();
        out.close();
    }
}
