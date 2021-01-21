import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Combiner {
    DataFinder df = new DataFinder();
    Calculator calculator = new Calculator();
    /**
     * Merge Modis info with Occurrence info
     * Step1: Find corresponding pixel info from start date to end date by calling getStartToEndFirePixelsByOccurance with
     *        given latitude, longitude and acq_data
     * Step2: If the pixel info is not null, then we calculate everyday fire size by calling calcSize() and save them in a TreeMap
     *        where key is the date and the value is the fire size
     * Step3: Calculate peak date, max size, average size, average increasing rate and average decreasing rate with
     *        start date, end date and the TreeMap storing daily fire size (generated in Step2)
     * @param occurInfo
     * @return MergedInfo
     */
    MergedInfo mergeOneOccurrence (OccurInfo occurInfo) {
        TreeMap<Date, Double> multiDaysSize = new TreeMap<>();
        TreeMap<Date, Double[]> multiDaysSizeCenterPoint = new TreeMap<>();
        // Get the pixel info from start date to end date with given latitude, longitude and acq_data
        StartEndDatePixelInfo startEndDatePixelInfo = df.getStartToEndFirePixelsByOccurrence(occurInfo.lat, occurInfo.lng, occurInfo.date);
        if (startEndDatePixelInfo == null) return null;
        // If startEndDatePixelInfo is null, it means there is no fire pixel detected with given latitude, longitude and acq_data
        for (Date d: startEndDatePixelInfo.multipleDaysSelectData.keySet()) {
            List<ModisInfo> modisInfoList = startEndDatePixelInfo.multipleDaysSelectData.get(d);
            if (modisInfoList == null || modisInfoList.size() == 0) continue;
            FireGrid fg = new FireGrid();
            // Calculate everyday fire size
            double size = fg.calcSize(modisInfoList);
            Double[] centerpoint = fg.centerPoint;
            multiDaysSize.put(d, size);
            multiDaysSizeCenterPoint.put(d,centerpoint);
        }
        if (multiDaysSize == null || multiDaysSize.size() == 0) {
            return null;
        }
        if (startEndDatePixelInfo.startDate.equals(startEndDatePixelInfo.endDate)) {
            return null;
        }
        // Calculate peak date, max size, average size, average increasing rate and average decreasing rate
        CalculatorInfo calculatorInfo = calculator.getCalcInfo(startEndDatePixelInfo.startDate, startEndDatePixelInfo.endDate, multiDaysSize, multiDaysSizeCenterPoint);
        // Create new MergedInfo object to load the result
        MergedInfo res = new MergedInfo(occurInfo.lat, occurInfo.lng, occurInfo.date, startEndDatePixelInfo.startDate, startEndDatePixelInfo.endDate,
        calculatorInfo.peakDate, calculatorInfo.maxSize, calculatorInfo.aveSize, calculatorInfo.aveIncreaseRate, calculatorInfo.aveDecreaseRate, calculatorInfo.movementDirection, occurInfo);
        return res;
    }

    /**
     * Merge Modis Data and Canadian Dataset and write the result into csv file
     */
    void mergeAndWriteToCSV() {
        try {
            ModisFileReader modisFileReader = new ModisFileReader();
            // Read Modis Data
            modisFileReader.read("data/modis_2009_2018_Canada.csv");
            // Read Occurrence Data
            OccurrenceFileReader.read("data/final_2009_2018_all_columns.csv");
            List<MergedInfo> mergedInfoList = new ArrayList<>();
            Date prev = OccurrenceFileReader.occurInfoList.get(0).date;
            // Merge data: iterating OccurInfo and calling mergeOneOccurrence()
            for (OccurInfo occurInfo : OccurrenceFileReader.occurInfoList) {
                MergedInfo mergedInfo = mergeOneOccurrence(occurInfo);
                mergedInfoList.add(mergedInfo);
                if (mergedInfo == null) continue;
                if (!occurInfo.date.equals(prev)) {
                    System.out.println("merged: " + mergedInfo.reportDate.toString());
                    prev = occurInfo.date;
                }
            }
            // Write merged output to csv file
            // csv column name: same as the order of MergedInfo and OccurInfo
            FileWriter csvWriter = new FileWriter("output/merged.csv");
            csvWriter.append("lat");
            csvWriter.append(",");
            csvWriter.append("lng");
            csvWriter.append(",");
            csvWriter.append("reportDate");
            csvWriter.append(",");
            csvWriter.append("startDate");
            csvWriter.append(",");
            csvWriter.append("endDate");
            csvWriter.append(",");
            csvWriter.append("peakDate");
            csvWriter.append(",");
            csvWriter.append("duration");
            csvWriter.append(",");
            csvWriter.append("maxSize");
            csvWriter.append(",");
            csvWriter.append("aveSize");
            csvWriter.append(",");
            csvWriter.append("increaseSpreadRate");
            csvWriter.append(",");
            csvWriter.append("decreaseSpreadRate");
            csvWriter.append(",");
            csvWriter.append("movementDirection");
            csvWriter.append(",");
            csvWriter.append("movementDirectionNum");

            csvWriter.append(",");
            csvWriter.append("temp");
            csvWriter.append(",");
            csvWriter.append("td");
            csvWriter.append(",");
            csvWriter.append("rh");
            csvWriter.append(",");
            csvWriter.append("ws");
            csvWriter.append(",");
            csvWriter.append("wg");
            csvWriter.append(",");
            csvWriter.append("wdir");
            csvWriter.append(",");
            csvWriter.append("pres");
            csvWriter.append(",");
            csvWriter.append("vis");
            csvWriter.append(",");
            csvWriter.append("precip");
            csvWriter.append(",");
            csvWriter.append("rndays");
            csvWriter.append(",");
            csvWriter.append("ffmc");
            csvWriter.append(",");
            csvWriter.append("dmc");
            csvWriter.append(",");
            csvWriter.append("dc");
            csvWriter.append(",");
            csvWriter.append("bui");
            csvWriter.append(",");
            csvWriter.append("isi");
            csvWriter.append(",");
            csvWriter.append("fwi");
            csvWriter.append(",");
            csvWriter.append("dsr");
            csvWriter.append("\n");


            for (MergedInfo mergedInfo : mergedInfoList) {
                if (mergedInfo == null) continue;
                csvWriter.append(mergedInfo.lat.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.lng.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.reportDate.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.startDate.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.endDate.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.peakDate.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.duration.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.maxSize.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.aveSize.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.increaseSpreadRate.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.decreaseSpreadRate.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.movementDirection);
                csvWriter.append(",");
                if (mergedInfo.movementDirection == "NW") {
                    csvWriter.append("0");
                } else if (mergedInfo.movementDirection == "NE") {
                    csvWriter.append("1");
                } else if (mergedInfo.movementDirection == "SW") {
                    csvWriter.append("2");
                } else {
                    csvWriter.append("3");
                }

                //write weather data from occurInfo
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.temp.toString());

                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.td.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.rh.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.ws.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.wg.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.wdir.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.pres.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.vis.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.precip.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.rndays.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.ffmc.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.dmc.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.dc.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.bui.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.isi.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.fwi.toString());
                csvWriter.append(",");
                csvWriter.append(mergedInfo.occurInfo.dsr.toString());

                csvWriter.append("\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        Combiner combiner = new Combiner();
        combiner.mergeAndWriteToCSV();
    }
}

/**
 * Create MergedInfo Class to contain merged data with 11 attributes.
 */
class MergedInfo {
    Double lat;
    Double lng;
    Date reportDate;
    Date startDate;
    Date endDate;
    Date peakDate;
    Integer duration;
    Double maxSize;
    Double aveSize;
    Double increaseSpreadRate;
    Double decreaseSpreadRate;
    String movementDirection;
    OccurInfo occurInfo;
    // lat and lng need to be changed to the center of the fire
    MergedInfo(Double lt, Double lg, Date rd, Date sd, Date ed, Date pd, Double ms, Double as, Double ir, Double dr, String md, OccurInfo oi) {
        lat = lt;
        lng = lg;
        reportDate = rd;
        startDate = sd;
        endDate = ed;
        peakDate = pd;
        long difference_In_Time = endDate.getTime() - startDate.getTime();

        duration = (int)Math.ceil(difference_In_Time / (1000d * 60d * 60d * 24d)) % 365;
        maxSize = ms;
        aveSize = as;
        increaseSpreadRate = ir;
        decreaseSpreadRate = dr;
        movementDirection = md;
        occurInfo = oi;
    }
}
