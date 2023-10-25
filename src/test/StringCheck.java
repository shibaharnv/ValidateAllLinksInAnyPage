package linkscheck;

import java.util.ArrayList;

public class StringCheck {

    public static void main(String[] args) {

        String fullString="[\"https://adviseur.test.portal.nn.insim.biz/va/Ondersteuning-betalingsproblemen-1.htm\", 404],[\"https://adviseur.test.portal.nn.insim.biz/va/Samen-bereik-jij-meer.htm\", 404]";
        ArrayList<String> resultlist=urlStatusCodeSplit(fullString);

        System.out.println(resultlist.toString());
//        String[] strarr= fullString.split(",");
//
//        //System.out.println(strarr[0]);
//
//        for(String test:strarr)
//        {
//           // System.out.println(test);
//
//            if(test.contains("http"))
//            {
//                test= test.replace("[","");
//                test = test.replaceAll("^\"|\"$", "");
//
//            }
//            else
//            {
//                test= test.replace("]","");
//            }
//
//            System.out.println(test);
//        }


    }


    public static  ArrayList<String> urlStatusCodeSplit(String fullString)
    {

        ArrayList<String> al = new ArrayList<>();

        String[] strarr= fullString.split(",");

        //System.out.println(strarr[0]);

        for(String test:strarr)
        {
            // System.out.println(test);

            if(test.contains("http"))
            {
                test= test.replace("[","");
                test = test.replaceAll("^\"|\"$", "");

            }
            else
            {
                test= test.replace("]","");
            }
            al.add(test);
            System.out.println(test);
        }

    return al;

    }
}
