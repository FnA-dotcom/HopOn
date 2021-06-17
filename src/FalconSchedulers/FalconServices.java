package FalconSchedulers;

import HopOn.Supportive;

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

@SuppressWarnings("Duplicates")
public class FalconServices extends HttpServlet {
    private String Query = "";
    private CallableStatement cStmt = null;
    private Statement stmt = null;
    private ResultSet rset = null;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
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
            switch (Action) {
                case "MarkCancel":
                    MarkCancel(req, out, conn);
                    break;
                case "UpdateLocation":
                    UpdateLocation(req, out, conn);
                    break;
                default:
                    out.println("Under Development ... " + Action);
                    break;
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

    private void MarkCancel(HttpServletRequest req, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = "";
        cStmt = null;


    }

    private void UpdateLocation(HttpServletRequest req, PrintWriter out, Connection conn) {
        stmt = null;
        rset = null;
        Query = "";
        cStmt = null;


    }
}
