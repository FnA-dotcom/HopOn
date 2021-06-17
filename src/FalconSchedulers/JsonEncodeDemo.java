package FalconSchedulers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class JsonEncodeDemo {
    public static void main(String[] args){

/*        JSONObject obj = new JSONObject();
        JSONArray list = new JSONArray();
       // for(int i =1;i < 5;i++){

            obj.put("name", "mkyong.com");
            obj.put("age", new Integer(100));
            list.add(obj);

            //list.add("msg " + i);
        //}



        obj.put("messages", list);

        try (FileWriter file = new FileWriter("d:\\test.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.print(obj);*/
        JSONArray jsonArray = new JSONArray();
        for(int i =1;i < 5;i++){
            JSONObject jsonObj= new JSONObject();
            jsonObj.put("srcOfPhoto", "Element 1");
            jsonObj.put("username", "name"+i);
            jsonObj.put("userid", "userid"+i);

            jsonArray.add(jsonObj);
        }
        JSONObject parameters = new JSONObject();
        parameters.put("action", "remove");
        parameters.put("datatable", jsonArray );

        try (FileWriter file = new FileWriter("d:\\test.json")) {

            file.write(jsonArray.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print(jsonArray);
        System.out.print(parameters);
    }
}
