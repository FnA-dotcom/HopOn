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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@SuppressWarnings("Duplicates")

public class OutSide extends HttpServlet {
    String Query = "";
    PreparedStatement pStmt = null;
    Statement stmt = null;
    ResultSet rset = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        myServices(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        myServices(request, response);
    }

    private void myServices(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        String Action = null;
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        Supportive supp = new Supportive();
        //String connect_string = supp.GetConnectString();
        ServletContext context = null;
        context = this.getServletContext();
        try {
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

            if (request.getParameter("ActionID") == null) {
                Action = "Home";
                return;
            }
            Action = request.getParameter("ActionID");
            if (Action.equals("o0ljqWzZ")) {
                AingTing(request, out, conn, context);
            } else {
                out.println("Under Development ... " + Action);
            }
            out.println("3");
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

    private void AingTing(HttpServletRequest request, PrintWriter out, Connection conn, ServletContext servletContext) {
        stmt = null;
        rset = null;
        Query = "";
        StringBuilder Country = new StringBuilder();
        StringBuilder CarType = new StringBuilder();
        String UserType = request.getParameter("UserType").trim();

        try {

            try {
                Query = "SELECT Id,CountryName FROM Country WHERE Status=0 ORDER BY CountryName ";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                Country.append("<option value='' selected >Select Country</option>");
                while (rset.next()) {
                    Country.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
                }

                rset.close();
                stmt.close();
            } catch (Exception var17) {
                Supportive.DumException("OutSide", "Country Query -- GetInfo--001", request, var17, getServletContext());
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "OutSide");
                    Parser.SetField("ActionID", "AingTing");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var171) {
                }
            }
            try {
                Query = "SELECT Id,CarType FROM CarType WHERE Status=0 ORDER BY CarType ";
                stmt = conn.createStatement();
                rset = stmt.executeQuery(Query);
                CarType.append("<option value='' selected >Select Car Type</option>");
                while (rset.next()) {
                    CarType.append("<option value=" + rset.getInt(1) + ">" + rset.getString(2) + "</option>");
                }

                rset.close();
                stmt.close();
            } catch (Exception var17) {
                Supportive.DumException("AddFamily", "Car Type Query -- GetInfo--002", request, var17, getServletContext());
                try {
                    Parsehtm Parser = new Parsehtm(request);
                    Parser.SetField("FormName", "AddFamily");
                    Parser.SetField("ActionID", "GetInfo");
                    Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
                } catch (Exception var171) {
                }
            }
            Parsehtm Parser = new Parsehtm(request);

            Parser.SetField("Country", Country.toString());
            if (UserType.equals("P"))
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/AingTing.html");
            else {
                Parser.SetField("CarType", CarType.toString());
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/CreateDriver.html");
            }
        } catch (Exception Ex) {
            Supportive.DumException("OutSide", "First Method -- GetInfo--MAIN", request, Ex, getServletContext());
            try {
                Parsehtm Parser = new Parsehtm(request);
                Parser.SetField("FormName", "OutSide");
                Parser.SetField("ActionID", "AingTing");
                Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Exceptions/Error.html");
            } catch (Exception var17) {
            }
        }
    }
}
