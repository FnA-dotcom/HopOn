package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
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
public class RoutingServices extends HttpServlet {
    private Statement stmt = null;
    private ResultSet rset = null;
    private String Query = "";

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServicesRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServicesRequest(request, response);
    }

    public void ServicesRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Connection conn = null;
        String Action = null;
        String UserId = "";
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();

        try {
            ServletContext context = null;
            context = this.getServletContext();

            try {
                conn = Supportive.getMysqlConn(this.getServletContext());
                if (conn == null) {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("Error", "Unable to connect. Our team is looking into it!");
                    Parser.GenerateHtml(out, String.valueOf(Supportive.GetHtmlPath(this.getServletContext())) + "index.html");
                    return;
                }
            } catch (Exception var11) {
                conn = null;
                out.println("Exception excp conn: " + var11.getMessage());
            }

            if (request.getParameter("Action") == null) {
                Action = "Home";
                return;
            }

            Action = request.getParameter("Action");
            if (Action.compareTo("DirectionServices") == 0) {
                DirectionServices(request, response, out, conn, context);
            }

            conn.close();

            out.close();
            out.flush();
        } catch (Exception Ex) {
            out.println("Exception in Routing Services " + Ex.getMessage());
            out.flush();
            out.close();
            return;
        }
    }

    private void DirectionServices(HttpServletRequest request, HttpServletResponse response, PrintWriter out, Connection conn, ServletContext servletContext) {
        String JobLat = request.getParameter("JobLat").trim();
        String JobLon = request.getParameter("JobLon").trim();
        String TechLat = request.getParameter("TechLat").trim();
        String TechLon = request.getParameter("TechLon").trim();
        String JobLocation = "";
        String TechLocation = "";
        try {
            //JobLocation = JobLat + "," + JobLon;
            //TechLocation = TechLat + "," + TechLon;
            Parsehtm Parser = new Parsehtm(request);
            //Parser.SetField("JobLocation",JobLocation);
            //Parser.SetField("TechLocation",TechLocation);
            Parser.SetField("JobLat", JobLat);
            Parser.SetField("JobLon", JobLon);
            Parser.SetField("TechLat", TechLat);
            Parser.SetField("TechLon", TechLon);
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Reports/RoutingServices.html");
        } catch (Exception var14) {
            out.println("Unable to process the request..." + var14.getMessage());
        }
    }

}
