package FieldForce;

import java.net.MalformedURLException;
import java.net.URL;

public class TestingClass {
    public static void main(String[] args) {
        URL aURL = null;
        try {
            aURL = new URL("http://203.130.0.239:83/FieldForce/FieldForce.TestingClassAES?PwJdx3QSVY52PIjQ7gOHR6g%3D%3D=wJdx3QSVY52PIjQ7gOHR6g%3D%3D%0A&Pv1=w4zbEr8Gq0wZQ7kXwipA%2BKahY79viRrhTv6zNewDs6U%3D%0A&Pv2=WxfYBMy%2BIWus2UdUuf9OgNlqw%2BXWnkGvsFQ2Go%2BxIbTKq4Q%2B0XtLthOZYzuLHZ0ZPa1oj04Pe%2FfF%0AjhjQKJe%2Bvw%3D%3D%0A");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        System.out.println("protocol = " + aURL.getProtocol() + "<br>"); //http
        System.out.println("authority = " + aURL.getAuthority() + "<br>"); //example.com:80
        System.out.println("host = " + aURL.getHost() + "<br>"); //example.com
        System.out.println("port = " + aURL.getPort() + "<br>"); //80
        System.out.println("path = " + aURL.getPath() + "<br>"); //  /docs/books/tutorial/index.html
        System.out.println("query = " + aURL.getQuery() + "<br>"); //name=networking
        System.out.println("filename = " + aURL.getFile() + "<br>"); ///docs/books/tutorial/index.html?name=networking
        System.out.println("ref = " + aURL.getRef() + "<br>"); //DOWNLOADING
    }
}
