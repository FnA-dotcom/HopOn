package HopOn;

import javax.servlet.http.HttpServletRequest;

public class TestingClass {

    public static Object[] getDetails() {
        String name = "Ryan";
        int age = 25;
        char gender = 'M';
        long income = 100000;

        return new Object[]{name, age, gender, income};
    }

    private static String getClientIp(HttpServletRequest request) {

        String remoteAddr = "";

        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }

        return remoteAddr;
    }

    public static void main(String[] args) {
/*        try {
//            Object[] person = getDetails();
//            System.out.println(Arrays.toString(person));
//            System.out.println("Name " + person[0]);
//            System.out.println("age " + person[1]);
        }
        catch (Exception Ex){
            System.out.println("Error " + Ex.getMessage());
        }*/

        String Action = "GetParent";
        switch (Action) {
            case "GetParent": {
                String SystemUserType = "P";
                if (SystemUserType.equals("A"))
                    System.out.println("GetParent");
                else
                    System.out.println("GetInfo");
            }
            break;
            case "GetInfo": {
                System.out.println("GetInfo");
            }
            break;
            default:
                System.out.println("Under Development ... " + Action);
                break;
        }
    }
}
