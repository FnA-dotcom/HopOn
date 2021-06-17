package FalconSchedulers;

public class Haversine {
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        Double lat1 = 24.846773;
        Double lon1 = 67.031064;
        Double lat2 = 24.861596;
        Double lon2 = 66.999881;
/*        final int R = 6371; // Radious of the earth
        //Double lat1 = Double.parseDouble(24.846773);

        //Double lon1 = Double.parseDouble(args[1]);

        //Double lat2 = Double.parseDouble(args[2]);

        //Double lon2 = Double.parseDouble(args[3]);

        Double latDistance = toRad(lat2-lat1);
        Double lonDistance = toRad(lon2-lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c;*/

        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        System.out.println("The distance between two lat and long is::" + distance);

    }
    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }
}
