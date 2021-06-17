package FieldForce;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

@SuppressWarnings("Duplicates")
public class TestingClassAES extends HttpServlet {

    private static final String key = "aesEncryptionKey";
    private static final String initVector = "encryptionIntVec";

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.handleRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        this.handleRequest(request, response);
    }

    public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = new PrintWriter(response.getOutputStream());
        response.setContentType("text/html");

        out.println("IN METHOD <br>");
/*        String url = request.getRequestURL().toString();
        out.println("THIS --> " + url + "<br>");
        String URI = request.getRequestURI();
        out.println("getMethod --> " + request.getMethod() + "<br>");
        out.println("getQueryString --> " + request.getQueryString() + "<br>");
        out.println("getProtocol --> " + request.getProtocol() + "<br>");
        out.println("getPathInfo --> " + request.getPathInfo() + "<br>");
        out.println("getServerName --> " + request.getServerName() + "<br>");
        out.println("getHeaderNames --> " + request.getHeaderNames() + "<br>");
        out.println("getAttributeNames --> " + request.getAttributeNames() + "<br>");
        out.println("getServerPort --> " + request.getServerPort() + "<br>");
        out.println("getPathTranslated --> " + request.getPathTranslated() + "<br>");*/

        if (request.getQueryString().startsWith("P")) {
            String ActionID = request.getQueryString();
            ActionID = URLEncoder.encode(ActionID, "UTF-8");
            out.println("ACTION ID " + ActionID + "<br>");
            out.println("CUT ACTION ID " + ActionID.substring(1, 36) + "<br>");
            ActionID = ActionID.substring(1, 36);
            ActionID = decrypt(ActionID);
            out.println("NEW ACTION ID " + ActionID + "<br>");
           /* if (ActionID.equals("GETINPUT")) {
                GetInput(request, out);
            } else {
                out.println("UNKNOWN METHOD RESPONSE !! ");
            }*/
        }
/*        out.println("THIS --> " + URI + "<br>");
        URL aURL = new URL("http://203.130.0.239:83/FieldForce/FieldForce.TestingClassAES?PwJdx3QSVY52PIjQ7gOHR6g%3D%3D=wJdx3QSVY52PIjQ7gOHR6g%3D%3D%0A&Pv1=w4zbEr8Gq0wZQ7kXwipA%2BKahY79viRrhTv6zNewDs6U%3D%0A&Pv2=WxfYBMy%2BIWus2UdUuf9OgNlqw%2BXWnkGvsFQ2Go%2BxIbTKq4Q%2B0XtLthOZYzuLHZ0ZPa1oj04Pe%2FfF%0AjhjQKJe%2Bvw%3D%3D%0A");
        out.println("protocol = " + aURL.getProtocol() + "<br>"); //http
        out.println("authority = " + aURL.getAuthority() + "<br>"); //example.com:80
        out.println("host = " + aURL.getHost() + "<br>"); //example.com
        out.println("port = " + aURL.getPort() + "<br>"); //80
        out.println("path = " + aURL.getPath() + "<br>"); //  /docs/books/tutorial/index.html
        out.println("query = " + aURL.getQuery() + "<br>"); //name=networking
        out.println("filename = " + aURL.getFile() + "<br>"); ///docs/books/tutorial/index.html?name=networking
        out.println("ref = " + aURL.getRef() + "<br>"); //DOWNLOADING*/
  /*      String Action = request.getParameter("ActionID");
        out.println("ORG ACTION " + Action);
        Action = decrypt(Action);
        out.println("ENC ACTION " + Action);
        if (Action.startsWith("P")) {
            Action = Action.substring(1);
            if (Action.equals("GETINPUT")) {
                GetInput(request, out);
            }
        } else{
            out.println("UNKNOWN METHOD RESPONSE !! ");
        }*/

        out.close();
        out.flush();
    }

    private void GetInput(HttpServletRequest req, PrintWriter out) {
        String Param1 = req.getParameter("Pv1").trim();
        out.println("Param1 ORG --> " + Param1 + "<br>");
        Param1 = decrypt(Param1);
        out.println("Param1 DEC --> " + Param1 + "<br>");
        String Param2 = req.getParameter("Pv2").trim();
        out.println("Param2 ORG --> " + Param2 + "<br>");
        decrypt(Param2);
        Param2 = decrypt(Param2);
        out.println("Param2 DEC --> " + Param2 + "<br>");
    }
}
