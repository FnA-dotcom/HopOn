package FalconSchedulers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import FalconSchedulers.NotificationData;
import FalconSchedulers.NotificationRequestModel;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;

public class NotificationFalcon {
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to Developine");


        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(
                "https://fcm.googleapis.com/fcm/send");

        // we already created this model class.
        // we will convert this model class to json object using google gson library.

        NotificationRequestModel notificationRequestModel = new NotificationRequestModel();
        NotificationData notificationData = new NotificationData();

        notificationData.setDetail("this is firebase push notification from java client (server)");
        notificationData.setTitle("Hello Firebase Push Notification");
        notificationRequestModel.setData(notificationData);
        notificationRequestModel.setTo("cJhxnOYVs5A:APA91bEft2n4tx7kjEu83ZsShAMzzEzXk0_6bMcyWYVX1qKJlfQXZbaABL6kMCO4Owsh89bPmaIIqSmBS80IyEPDaHB1MEeIlJWKHvIcmwAdL9lMUoFM5WmzELUY5OSRfzVEa5YdSiZ5B_OZ3mviC_QIrHfMwstTQQ");


        Gson gson = new Gson();
        Type type = new TypeToken<NotificationRequestModel>() {
        }.getType();

        String json = gson.toJson(notificationRequestModel, type);

        StringEntity input = new StringEntity(json);
        input.setContentType("application/json");

        // server key of your firebase project goes here in header field.
        // You can get it from firebase console.

        postRequest.addHeader("Authorization", "key=AAAAelEk6E8:APA91bFnuyiQhKIevOQTPHBARSWINI9C7YyUtOBAdHw2TksLwG9j59pOmDAZpfmSrlfZmqT5glJGR6cWiPYtFYQyOcDw1f5a0RqjXejDERs_x9Urd07yrF3w71AvnP_ErTjMlgqVfzRPVbAmTYsNj9w9QEUM91mqZw");
        postRequest.setEntity(input);

        System.out.println("reques:" + json);

        HttpResponse response = httpClient.execute(postRequest);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatusLine().getStatusCode());
        } else if (response.getStatusLine().getStatusCode() == 200) {

            System.out.println("response:" + EntityUtils.toString(response.getEntity()));

        }
    }
}
