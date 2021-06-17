package FalconSchedulers;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class indexof{
    public static void main(String args[]) {


        String mainAA="{\"distance\":{\"text\":\"189.09 m\",\"value\":0},\"duration\":{\"text\":\"1 min\",\"value\":0},\"status\":\"OK\"}";

        String [] ddata=mainAA.split(":");
        System.out.println(ddata[2]);

        String [] ddata1=ddata[2].split(",");
        System.out.println(ddata1[0]);
        String FinalDistance=ddata1[0];
        Double FinalDistancekm=0.0d;
        FinalDistance=FinalDistance.replace("\"","");
        String [] DistancewithUnit=FinalDistance.split(" ");
        Double Distance=Double.parseDouble(DistancewithUnit[0]);
        String DistanceUnit=DistancewithUnit[1];

        System.out.println(DistanceUnit);
        if(DistanceUnit.compareTo("m")==0)
        {
            Distance=Distance/1000;
        }
        System.out.println(Distance);


        HashMap<Integer,Double> words=new HashMap<Integer,Double>();
        words.put(1, 2.0);
        words.put(2, 0.0014);
        words.put(3, 1.0);
        words.put(5, 11.0);

        int minKey = 0;
        Double minValue = Double.MAX_VALUE;
        for (Integer key : words.keySet()) {
            Double value = words.get(key);
            if (value < minValue) {
                minValue = value;
                minKey = key;
            }
        }

        System.out.println("Value = "+minValue);
        System.out.println("key ="+minKey);
    }




}