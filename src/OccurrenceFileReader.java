import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is used for loading Occurrence Data into a List of OccurInfo objects
 */
public class OccurrenceFileReader {
    static List<OccurInfo> occurInfoList = new ArrayList<>();
    public static void read(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        Scanner sc = new Scanner(file);
        sc.nextLine(); //remove title
        while(sc.hasNext()) {
            String line= sc.nextLine();
            String[] array = line.split(",");
            String date = array[2], latitude = array[43], longitude = array[44];
            String temp = array[3], td = array[4], rh = array[5], ws = array[6], wg = array[7], wdir = array[8], pres = array[9], vis = array[10], precip = array[11];
            String rndays = array[12], ffmc = array[14], dmc = array[15], dc = array[16], bui = array[17], isi = array[18], fwi = array[19], dsr = array[20];
            try {
                OccurInfo curInfo = new OccurInfo(latitude, longitude, date, temp, td, rh, ws, wg, wdir, pres, vis, precip, rndays, ffmc, dmc, dc, bui, isi, fwi, dsr);
                occurInfoList.add(curInfo);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
//        System.out.println("done");//show progress
        sc.close();
    }
}


class OccurInfo {
    //column number in the csv file
    Double lat; //43
    Double lng; //44
    Date date; //2
    Double temp; //3
    Double td; //4
    Double rh; //5
    Double ws; //6
    Double wg; //7
    Integer wdir; //8 wind direction in degrees from true north
    Double pres; //9
    Double vis; //10
    Double precip; //11
    Double rndays; //12
    Double ffmc; //14
    Double dmc; //15
    Double dc; //16
    Double bui; //17
    Double isi; //18
    Double fwi; //19
    Double dsr; //20

    OccurInfo (String lat, String lng, String date, String temp, String td, String rh, String ws, String wg, String wdir, String pres, String vis, String precip, String rndays, String ffmc, String dmc, String dc, String bui, String isi, String fwi, String dsr) throws ParseException {
        this.lat = Double.parseDouble(lat);
        this.lng = Double.parseDouble(lng);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
        //2019-04-25
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        this.date  = formatter.parse( date);
        this.temp = Double.parseDouble(temp);
        this.td = Double.parseDouble(td);
        this.rh = Double.parseDouble(rh);
        this.ws = Double.parseDouble(ws);
        this.wg = Double.parseDouble(wg);
        this.wdir = Integer.parseInt(wdir);
        this.pres = Double.parseDouble(pres);
        this.vis = Double.parseDouble(vis);
        this.precip = Double.parseDouble(precip);
        this.rndays = Double.parseDouble(rndays);
        this.ffmc = Double.parseDouble(ffmc);
        this.dmc = Double.parseDouble(dmc);
        this.dc = Double.parseDouble(dc);
        this.bui = Double.parseDouble(bui);
        this.isi = Double.parseDouble(isi);
        this.fwi = Double.parseDouble(fwi);
        this.dsr = Double.parseDouble(dsr);
    }

}
