package HopOn;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("Duplicates")
public class JSONTesting extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        String RequestName = request.getParameter("RequestName").trim();
        if (RequestName.equals("Test"))
            AFunction(response, request);
/*        List<City> cities = new CityService().getCities();

        JsonConverter converter = new JsonConverter();
        String output = converter.convertToJson(cities);

       */
    }

    private void AFunction(HttpServletResponse response, HttpServletRequest req) throws IOException {

        ServletOutputStream out = response.getOutputStream();

        String Testing = req.getParameter("TestVar");
        JSONArray jsonArray = new JSONArray();
        for (int i = 1; i < 5; i++) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("srcOfPhoto", "Element 1");
            jsonObj.put("username", "name" + i);
            jsonObj.put("userid", "userid" + i);

            jsonArray.add(jsonObj);
        }
        out.print("ELEMENTS ARE " + jsonArray.toJSONString());
        out.println("my Var " + Testing);
    }
}
