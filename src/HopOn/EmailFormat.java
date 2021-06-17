package HopOn;

import Parsehtm.Parsehtm;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

@SuppressWarnings("Duplicates")

public class EmailFormat extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.Services(request, response);
    }

    private void Services(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");
        String Action = null;
        Action = request.getParameter("ActionID");
        ServletContext context = null;
        context = getServletContext();
        if (Action.equals("ShowFormat")) {
            ShowFormat(request, out, context);
        }
        out.flush();
        out.close();
    }

    private void ShowFormat(HttpServletRequest request, PrintWriter out, ServletContext servletContext) {
        Parsehtm Parser = new Parsehtm(request);
        try {
            Parser.GenerateHtml(out, Supportive.GetHtmlPath(servletContext) + "Forms/EmailFormatter.html");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
