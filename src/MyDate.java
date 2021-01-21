import java.util.Calendar;
import java.util.Date;

public class MyDate {
    /**
     * Get one day before the input date
     * @param curDate
     * @return Date object which represents one day before the input date
     */
    static Date getOneDayBefore (Date curDate) {
        Calendar c = Calendar.getInstance();
        c.setTime(curDate);
        c.add(Calendar.DATE, -1);
        Date oneDayBefore = c.getTime();
        return oneDayBefore;
    }
    /**
     * Get one day after the input date
     * @param curDate
     * @return Date object which represents one day after the input date
     */
    static Date getOneDayAfter (Date curDate) {
        Calendar c = Calendar.getInstance();
        c.setTime(curDate);
        c.add(Calendar.DATE, 1);
        Date oneDayAfter = c.getTime();
        return oneDayAfter;
    }
    /**
     * Get N day before the input date where N is the user input (day)
     * @param curDate
     * @param day
     * @return Date object which represents N day before the input date
     */
    static Date getNDaysBefore(Date curDate, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(curDate);
        c.add(Calendar.DATE, -day);
        Date NDayBefore = c.getTime();
        return NDayBefore;
    }
    /**
     * Get N day after the input date where N is the user input (day)
     * @param curDate
     * @param day
     * @return Date object which represents N day after the input date
     */
    static Date getNDaysAfter(Date curDate, int day) {
        Calendar c = Calendar.getInstance();
        c.setTime(curDate);
        c.add(Calendar.DATE, day);
        Date NDayAfter = c.getTime();
        return NDayAfter;
    }

}
