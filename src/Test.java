import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

//just for testing some helper functions
public class Test {

    public static void main(String[] args) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        // Get a Date object from the date string
        Date myDate = dateFormat.parse("20201110");

        // this calculation may skip a day (Standard-to-Daylight switch)...
        //oneDayBefore = new Date(myDate.getTime() - (24 * 3600000));

        // if the Date->time xform always places the time as YYYYMMDD 00:00:00
        //   this will be safer.
        Calendar c = Calendar.getInstance();
        c.setTime(myDate);
        c.add(Calendar.DATE, -1);
        Date oneDayBefore = c.getTime();
        System.out.println("cur date: " + myDate.toString() + "; one day before: " +oneDayBefore.toString() );

        String result = dateFormat.format(oneDayBefore);
        System.out.println(result);
        System.out.println(myDate.getTime() - oneDayBefore.getTime());

        Date d1 = new Date(Calendar.DATE);
        Date d2 = new Date(Calendar.DATE);
        TreeMap<Date, Integer> map = new TreeMap<>();
        map.put(d1,100);
        System.out.println(map.get(d2));

    }
}
