import java.util.*;

/**
 * This is the class for calculating rate, average rate, finding the date with the max fire size, center point movement direction.
 * And is responsible for saving all the calculated data into CalculatorInfo
 */
public class Calculator {

    /**
     * calculation of rate as a percentage of change
     * @param size1 yesterday's size
     * @param size2 today's size
     * @return rate
     */
    double calcRate (double size1, double size2) {
        return (size2 - size1) / size1;
    }

    /**
     * go from startDate to endDate to find the date with the max fire size, and average size
     * @param startDate
     * @param endDate
     * @param multiDaysSize
     * @param ci CalculatorInfo object to store calculated result
     * @return
     */
    Date maxSizeDate(Date startDate, Date endDate, TreeMap<Date, Double> multiDaysSize, CalculatorInfo ci) {
        double maxSize = -1;
        double sum = 0;
        int cnt = 0;
        Date curDate = startDate;
        Date maxDate = null;
        while (curDate.before(endDate)) {
            double curSize = multiDaysSize.getOrDefault(curDate, -1.0);
            cnt++;
            sum += curSize;
            if (curSize > maxSize) {
                maxDate = curDate;
                maxSize = curSize;
            }
            curDate = MyDate.getOneDayAfter(curDate);
        }
        ci.maxSize = maxSize;
        ci.peakDate = maxDate;
        double aveSize = sum / cnt;
        ci.aveSize = aveSize;
        return maxDate;
    }

    /**
     * calculate average rate of this fire
     * how to call: aveIncreaseRate: startDate to maxDate;    aveDecreaseRate: maxDate to endDate
     * @param startDate
     * @param endDate
     * @param multiDaysSize
     * @return average (increasing or decreasing) rate
     */
    //
    double aveRate(Date startDate, Date endDate, TreeMap<Date, Double> multiDaysSize) {
        double rateSum = 0.0;
        int cnt = 0;
        Date curDate = startDate;
        while (curDate.before(MyDate.getOneDayBefore(endDate))) {
            Date nextDate = MyDate.getOneDayAfter(curDate);
            double curRate = calcRate(multiDaysSize.get(curDate), multiDaysSize.get(nextDate));
            curDate = nextDate;
            cnt++;
            rateSum += curRate;
        }
        return rateSum/cnt;
    }

    /**
     * find fire center point movement direction
     * @param startDate
     * @param endDate
     * @param multiDaysSizeCenterPoint
     * @return
     */
    String movementDirection(Date startDate, Date endDate, TreeMap<Date, Double[]> multiDaysSizeCenterPoint) {
        Double lat0 = multiDaysSizeCenterPoint.get(startDate)[0];
        Double lng0 = multiDaysSizeCenterPoint.get(startDate)[1];
        Double lat1 = multiDaysSizeCenterPoint.get(endDate)[0];
        Double lng1 = multiDaysSizeCenterPoint.get(endDate)[1];
        String direction;
        if (lat0 == lat1 && lng0 == lng1) {
            direction = "";
        } else if (lat0 <= lat1 && lng0 >= lng1) {
            direction = "NW";
        } else if (lat0 <= lat1 && lng0 < lng1) {
            direction = "NE";
        } else if (lat0 > lat1 && lng0 >= lng1) {
            direction = "SW";
        } else {
            direction = "SE";
        }
        return direction;
    }

    /**
     * call all the calculator helper functions all together and store info properly in CalculatorInfo object
     * @param startDate
     * @param endDate
     * @param multiDaysSize
     * @param multiDaysSizeCenterPoint
     * @return
     */
    CalculatorInfo getCalcInfo (Date startDate, Date endDate, TreeMap<Date, Double> multiDaysSize, TreeMap<Date, Double[]> multiDaysSizeCenterPoint) {
        CalculatorInfo ci = new CalculatorInfo();
        maxSizeDate(startDate, endDate, multiDaysSize, ci);
        //aveIncreaseRate: startDate to maxDate;    aveDecreaseRate: maxDate to endDate
        double incR = aveRate(startDate, ci.peakDate,multiDaysSize);
        double decR = aveRate(ci.peakDate, endDate, multiDaysSize);
        ci.aveIncreaseRate = incR;
        ci.aveDecreaseRate = decR;
        ci.movementDirection = movementDirection(startDate, endDate, multiDaysSizeCenterPoint);
        return ci;
    }
}


class CalculatorInfo {
    Date peakDate;
    Double maxSize;
    Double aveSize;
    Double aveIncreaseRate;
    Double aveDecreaseRate;
    String movementDirection; //1: SW; 2: NW; 3: SE; 4: NE
}
