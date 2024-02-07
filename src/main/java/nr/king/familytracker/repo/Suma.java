package nr.king.familytracker.repo;

import java.util.Arrays;
import java.util.Date;

public class Suma {
    public static void main(String[] args) {
        Date date = new Date();
        long dateInMillious = date.getTime()+(2 * 60 * 60 * 1000);
        System.out.println("args = " + dateInMillious +" \n" + System.currentTimeMillis());
    }
}
