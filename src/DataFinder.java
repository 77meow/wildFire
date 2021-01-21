import java.util.*;

public class DataFinder {
    List<List<String>> selectedData;
    TreeMap<Date, List<ModisInfo>> multipleDaysSelectDataMap = new TreeMap<>();
    int duration = -1;

    /**
     * call this to get fire pixels by using default thresholds
     * @param lat occurrence lat
     * @param lng occurrence lng
     * @param date occurrence date
     * @return list of ModisInfo
     */
    List<ModisInfo> getFirePixelsByOccurrence(double lat, double lng, Date date) {
        double firstRecThreshold = 1.1;
        double adjacentRecThreshold = 0.2;
        return getFirePixelsByOccurrence(lat, lng, firstRecThreshold, adjacentRecThreshold, date);
    }


    /**
     * given a fire occurrence, find all the pixels in the same area by using thresholds
     * @param lat
     * @param lng
     * @param firstRecThreshold first record in the list should be within the firstRecThreshold from the occurrence lat and lng
     * @param adjacentRecThreshold 2 consecutive records in a list should be within adjacentRecThreshold to be considered in the same area
     * @param date
     * @return
     */
    List<ModisInfo> getFirePixelsByOccurrence(double lat, double lng, double firstRecThreshold, double adjacentRecThreshold, Date date) {

        List<ModisInfo> rawData = ModisFileReader.dateModisInfoMap.get(new Date(date.getTime()));
        //no raw data
        if (rawData == null) {
            return null;
        }
        //no raw data
        if (rawData.size() == 0) {
            return null;
        }
        double curLat = rawData.get(0).lat;
        double curLng = rawData.get(0).lng;
        int i = 0;
        boolean found = false;
        boolean enteredWhileLoop = false;
        //find the starting record. first record within the firstRecThreshold from the occurrence lat and lng
        while (i < rawData.size() - 1 && (Math.abs(curLat - lat) > firstRecThreshold || Math.abs(curLng - lng) > firstRecThreshold)) {
            enteredWhileLoop = true;
            i++;
            curLat = rawData.get(i).lat;
            curLng = rawData.get(i).lng;
        }

        int start=0;
        if (enteredWhileLoop) {
            start = --i;
        } else {
            start = i;
        }
        double prevLat = curLat;
        double prevLng = curLng;
        //find all the data in the same area by using adjacentRecThreshold
        // (2 consecutive records in a list should be within adjacentRecThreshold)
        while (i < rawData.size() - 1 && (Math.abs(curLat - prevLat) <= adjacentRecThreshold && Math.abs(curLng - prevLng) <= adjacentRecThreshold)) {
            found = true;
            prevLat = curLat;
            prevLng = curLng;
            i++;
            curLat = rawData.get(i).lat;
            curLng = rawData.get(i).lng;
        }
        //no starting pixel found
        if (!found) {
            return null;
        }
        int end = i - 1;
        //no starting pixel found
        if (start == end) {
            return null;  //no record
        }
        return rawData.subList(start, end+1);
    }

    //get fire pixels for a fire occurrence for each day in its duration
    StartEndDatePixelInfo getMultipleDaysFirePixelsByOccurrence(double lat, double lng, double firstRecThreshold, double adjacentRecThreshold, Date date, int elapsedDays) {
        Date startDate = MyDate.getNDaysBefore(date, elapsedDays);
        Date endDate = MyDate.getNDaysAfter(date, elapsedDays);

        Date currDate = startDate;
        TreeMap<Date, List<ModisInfo>> multipleDaysSelectData = new TreeMap<>();
        //get everyday's fire pixels
        while (currDate.before(endDate)) {
            multipleDaysSelectData.put(currDate, getFirePixelsByOccurrence(lat, lng, firstRecThreshold, adjacentRecThreshold, date));
            currDate = MyDate.getOneDayAfter(currDate);
        }
        StartEndDatePixelInfo res = new StartEndDatePixelInfo(multipleDaysSelectData, startDate, endDate);
        return res;
    }

    //get fire pixels for a fire occurrence for each day in its duration using default thresholds
    StartEndDatePixelInfo getMultipleDaysFirePixelsByOccurrence(double lat, double lng, Date date) {
        return getMultipleDaysFirePixelsByOccurrence(lat, lng, 1.1, 0.2, date, 7);
    }
    //general use:
    StartEndDatePixelInfo getStartToEndFirePixelsByOccurrence(double lat, double lng, Date date) {
        //1.1 0.2
        //2, 1.5
        return getStartToEndFirePixelsByOccurrence(lat, lng, 2, 1.5, date);
    }

    /**
     * Since the date in the fire occurrence data is not the actual fire start date nor the end date,
     * we need to go into the MODIS dataset to find the start and end date of the fire.
     * We go back in time one day at a time to repeat the previous method (getFirePixelsByOccurrence: that match fire pixels of a fire on a given date)
     * and put all the pixels into the data structure and stop on the date when no pixels are found in MODIS dataset,
     * the day following is the actual start date of the fire.
     * Similarly, by going forward in time,
     * we can store all the pixels on the following days that belong to the same fire and find the actual end date of the fire.
     * And we can calculate the duration of the fire.
     * @param lat
     * @param lng
     * @param firstRecThreshold
     * @param adjacentRecThreshold
     * @param date
     * @return
     */
    StartEndDatePixelInfo getStartToEndFirePixelsByOccurrence(double lat, double lng, double firstRecThreshold, double adjacentRecThreshold, Date date) {
        multipleDaysSelectDataMap = new TreeMap<>();
        Date currDate = new Date(date.getTime());
        Date prevDate = new Date(date.getTime());

        List<ModisInfo> curList = getFirePixelsByOccurrence(lat, lng, firstRecThreshold, adjacentRecThreshold, currDate);
        if (curList == null || curList.size() == 0) return null;
        //go back in time
//        go back in time one day at a time to repeat the previous method
//        (getFirePixelsByOccurrence: that match fire pixels of a fire on a given date)
//       and put all the pixels into the curList and stop on the date when no pixels are found in MODIS dataset,

        while (curList != null) {
            multipleDaysSelectDataMap.put(new Date(currDate.getTime()), curList);
            prevDate = currDate;
            currDate = MyDate.getOneDayBefore(currDate);

            curList = getFirePixelsByOccurrence(lat, lng, firstRecThreshold, adjacentRecThreshold, currDate);
        }
        // the prevDate in this loop is the actual start date of the fire.
        Date startDate = new Date(prevDate.getTime());
        currDate = new Date(date.getTime());
        prevDate = new Date(date.getTime());
        //go forward in time same process as finding the start date
        curList = getFirePixelsByOccurrence(lat, lng, firstRecThreshold, adjacentRecThreshold, currDate);
        while (curList != null) {
            multipleDaysSelectDataMap.put(new Date(currDate.getTime()), curList);
            prevDate = currDate;
            currDate = MyDate.getOneDayAfter(currDate);

            curList = getFirePixelsByOccurrence(lat, lng, firstRecThreshold, adjacentRecThreshold, currDate);
        }
        Date endDate = prevDate;
        duration = (int)(endDate.getTime() - startDate.getTime()) / Calendar.DATE; //can add 1 to it
        StartEndDatePixelInfo res = new StartEndDatePixelInfo(multipleDaysSelectDataMap, startDate, endDate);
        return res;
    }
}

class StartEndDatePixelInfo {
    TreeMap<Date, List<ModisInfo>> multipleDaysSelectData;
    Date startDate;
    Date endDate;
    StartEndDatePixelInfo(TreeMap<Date, List<ModisInfo>> md, Date sd, Date ed) {
        multipleDaysSelectData = md;
        startDate = sd;
        endDate = ed;
    }
}
