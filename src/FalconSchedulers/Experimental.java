package FalconSchedulers;

public class Experimental {
    /**
     * @param args
     */
    public static void main(String[] args) {

/*        HashMap<Integer, String> hmap = new HashMap<Integer, String>();
        hmap.put(5, "A");
        hmap.put(11, "C");
        hmap.put(4, "Z");
        hmap.put(77, "Y");
        hmap.put(9, "P");
        hmap.put(66, "Q");
        hmap.put(0, "R");

        System.out.println("Before Sorting:");
        Set set = hmap.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry me = (Map.Entry)iterator.next();
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
        }
        Map<Integer, String> map = new TreeMap<Integer, String>(hmap);
        System.out.println("After Sorting:");
        Set set2 = map.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()) {
            Map.Entry me2 = (Map.Entry)iterator2.next();
            System.out.print(me2.getKey() + ": ");
            System.out.println(me2.getValue());
        }*/


/*
        HashMap<String, Integer> hm = new HashMap<String, Integer>();
        HashMap<String, Integer> hm1 = new HashMap<String, Integer>();


        hm.put("Naveen", 2);
        hm.put("Santosh", 3);
        hm.put("Ravi", 4);
        hm.put("Hasnain", 10);
        hm.put("Uzair", 8);
        hm.put("Tabish", -8);
        hm.put("Pramod", 1);
        Set<Entry<String, Integer>> set = hm.entrySet();
        List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(set);
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        for (Entry<String, Integer> entry : list) {
            System.out.println(entry.getValue());
            hm1.put(entry.getKey(), entry.getValue());

        }*/
//int a = 5;
//int b = 9;
//int c = 3;
//
//int Ans = (a == 1 ? -1 : (b == 2 ? c : -2));
//System.out.println(Ans);
//    }
        String DOB = "['code':'0', 'desc':'User Subscribed.']";
        DOB = "['code':'-1', 'desc':'Subscriber Already Subscribed.']";
        System.out.println(DOB.substring(2, 6));
        System.out.println(DOB.substring(9, 10));
        System.out.println(DOB.substring(9, 11));
        System.out.println(DOB.substring(9, 11).length());
        System.out.println(DOB.length());
    }
}
