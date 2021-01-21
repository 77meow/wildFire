import java.awt.font.TextMeasurer;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is used for loading Modis Data into a Map where key is the date and value is the list of corresponding ModisInfo
 */
public class ModisFileReader {
	static  List<ModisInfo> modisInfoList = new ArrayList<>();
	/**
	 * Load Modis data in to a List of List<String>
	 */
	static List<List<String>> rawData = new ArrayList<List<String>>();
	static double minLongitude = Double.MAX_VALUE;
	static double maxLongitude = -(Double.MAX_VALUE-1);
	static double minLatitude = Double.MAX_VALUE;
	static double maxLatitude = -(Double.MAX_VALUE-1);
	/**
	 * Load Modis data in a map where key is the date and value is the list of corresponding ModisInfo objects
	 */
	static Map<Date, List<ModisInfo>> dateModisInfoMap = new HashMap<>();

	/**
	 * Load Modis Data from the input file.
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public static void read(String fileName) throws FileNotFoundException {
		File file = new File(fileName);
		Scanner sc = new Scanner(file);
		sc.nextLine();
		while(sc.hasNext()) {
			String line= sc.nextLine();
			String[] array = line.split(",");
			// save current modis info in a list
			List<String> temp = Arrays.asList(array);
			rawData.add(temp);
			try {
				// Create new ModisInfo object to store current piece of Modis info.
				ModisInfo curInfo = new ModisInfo(temp.get(0), temp.get(1), temp.get(2), temp.get(3), temp.get(4), temp.get(5), temp.get(6), temp.get(9), temp.get(12), temp.get(13));
				// Update dateModisInfoMap when reading raw data from input file
				modisInfoList.add(curInfo);
				List<ModisInfo> l = dateModisInfoMap.getOrDefault(curInfo.date, new ArrayList<>());
				l.add(curInfo);
				dateModisInfoMap.put(new Date(curInfo.date.getTime()), l);

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		sc.close();
	}
}

/**
 * Create ModisInfo Class to contain modis data with 10 attributes.
 */
class ModisInfo {
	Double lat;
	Double lng;
	Double brightness;
	Double scan;
	Double track;
	Date date;
	Integer time; //1805-> 18:05
	Integer confidence;
	Boolean isDay; // day - true; night - false
	Double frp; //fire radiative power
	ModisInfo(){}
	/**
	 * Constructot for ModisInfo object with 10 arguments.
	 * Please see "feature engineering" document for meaning of each argument
	 * @throws ParseException
	 */
	public ModisInfo(String lt, String lg, String br, String sc, String tr, String dt, String tm, String cf, String fr, String dn ) throws ParseException {
		lat = Double.parseDouble(lt);
		lng = Double.parseDouble(lg);
		brightness = Double.parseDouble(br);
		scan = Double.parseDouble(sc);
		track = Double.parseDouble(tr);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		this.date  = formatter.parse(dt);
		time = Integer.parseInt(tm);
		confidence = Integer.parseInt(cf);
		if (dn.equals("D")) {
			isDay = true;
		} else {
			isDay = false;
		}
		frp = Double.parseDouble(fr);
	}


}