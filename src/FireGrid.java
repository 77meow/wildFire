import java.io.FileNotFoundException;
import java.util.List;


/**
 * This class is responsible for calculating fire size.
 * A Grid is a rectangle grid with 4 corners initialized by the real latitude and longitude of a fire's boarder.
 * The grid contains a 2-D array with object type: Cell
 * Each cell has data about how much has been covered on each side of the cell (left, right, up, down)
 * When the cell is (almost) fully covered, the cell will be marked as "fully covered"
 * which ensures that this cell can only be counted once when calculating the fire size.
 * For more details, please refer to the visualized documentation of this method.
 */
public class FireGrid {
    final static int RADIUS = 6371; //earth's radius in KM
//    final static double RADIUS = 16989.33; //earth's radius in 375M unit
    //1 km grid unit: 4 corners in degree
    double map0Lat;
    double map0Lng;
    double mapMaxLat;
    double mapMaxLng;
    Double[] centerPoint;
    Grid grid;
    FireGrid() {}

    /**
     * Initialization of FireGrid and the Grid inside of it. With the 4 corners of the fire, a grid's size is defined.
     * @param lat0 the smallest latitude of the pixels appeared in the current fire
     * @param lng0 the smallest longitude of the pixels appeared in the current fire
     * @param latMax the largest latitude of the pixels appeared in the current fire
     * @param lngMax the largest longitude of the pixels appeared in the current fire
     */
    FireGrid(double lat0, double lng0, double latMax, double lngMax) {
        map0Lat = lat0;
        map0Lng = lng0;
        mapMaxLat = latMax;
        mapMaxLng = lngMax;
        int rowEnd = (int) Math.ceil(convertLngToY(mapMaxLng)); //convert lng into coordinate Y
        int colEnd = (int) Math.ceil(convertLatToX(mapMaxLat)); //convert lat into coordinate X
        // initialize the Grid with 4 corners in KM (not degree)
        grid = new Grid(0, rowEnd, 0, colEnd);
    }

    /**
     * Conversion of a latitude to a coordinate X (in KM) using the difference of current lat and boarder lat (map0Lat)
     * @param lat: current pixel's latitude
     * @return the coordinate X (in KM)
     */
    double convertLatToX(double lat) {
        double x = (lat - map0Lat) * 2 * Math.PI * RADIUS / 360;
        return x;
    }
    /**
     * Conversion of a longitude to a coordinate Y (in KM) using the difference of current lat and boarder lng (map0Lng)
     * @param lng: current pixel's longitude
     * @return the coordinate Y (in KM)
     */
    double convertLngToY(double lng) {
        double y = (lng - map0Lng) * 2 * Math.PI * RADIUS / 360;
        return y;
    }


    public class Grid {
        private int coveredCnt;
        public int rlength;  //map height in KM
        public int clength;  //map width in KM
        public int rowStart;
        public int colStart;
        public int rowEnd;
        public int colEnd;
        public Cell[][] cells; //a 2-D array that stores cells
        public double countTotal;

        /**
         * Initialization of the Grid given 4 corners of it, each cell in the 2-D array is a 1*1 (KM) square.
         * @param rowStart 0
         * @param rowEnd the lower boarder's Y coordinate
         * @param colStart 0
         * @param colEnd the right boarder's X coordinate
         */
        public Grid(int rowStart, int rowEnd, int colStart, int colEnd) {
            this.rowStart = rowStart;
            this.rowEnd = rowEnd;
            this.colStart = colStart;
            this.colEnd = colEnd;
            this.coveredCnt = 0;
            countTotal = 0;
            int rCount = rowEnd - rowStart + 1;
            int cCount = colEnd - colStart + 1;
            this.rlength = rCount;
            this.clength = cCount;
            //initialization of the 2-D Cell array
            this.cells = new Cell[rCount][cCount];
            for (int i = 0; i < rlength; i++) {
                for (int j = 0; j < clength; j++) {
                    cells[i][j] = new Cell();
                }
            }
        }

        /**
         * Calculation of the covered part's size in KM^2
         * @return the covered part's size in KM^2
         */
        public double countTotalCovered() {
            for (int i = rowStart; i <= rowEnd; i++) {
                for (int j = colStart; j <= colEnd; j++) {
                    if (cells[i][j].isCoveredHorizontally && cells[i][j].isCoveredVertically) { //the cell is fully covered by fire pixels
                        countTotal++;
                    } else if (cells[i][j].isCoveredHorizontally) {
                        countTotal += (cells[i][j].up + cells[i][j].down);
                    } else if (cells[i][j].isCoveredVertically) {
                        countTotal += (cells[i][j].left + cells[i][j].right);
                    } else {
                        countTotal += (cells[i][j].up + cells[i][j].down) * (cells[i][j].left + cells[i][j].right);
                    }
                }
            }
            return countTotal; // 1km^2 unit
//            return countTotal * 0.140625; //375m unit
        }

        /**
         * Grid visualization: most of the time too big to be shown on a screen
         */
        public void printGrid() {
            for (int i = rowStart; i <= rowEnd; i++) {
                for (int j = colStart; j <= colEnd; j++) {
                    if (cells[i][j].isCoveredHorizontally && cells[i][j].isCoveredVertically) {
                        System.out.print('*');
                    } else if (cells[i][j].isCoveredHorizontally) {
                        System.out.print('@');
                    } else if (cells[i][j].isCoveredVertically) {
                        System.out.print('@');
                    } else {
                        System.out.print('.');
                    }
                }
                System.out.println();
            }
        }

        /**
         * adding a pixel to the current gird
         * @param lat latitude of the pixel
         * @param lng longitude of the pixel
         * @param clen the Scan of the pixel: the actual width of the pixel (in km)
         * @param rlen the Track of the pixel: the actual height of the pixel (in km)
         */
        public void addPixel(double lat, double lng, double clen, double rlen) {
            Coordinate coor = new Coordinate(lat, lng); //  transforming
            // in grid position (relative position): transforming from latitude and longitude
            double c = coor.c;
            double r = coor.r;

            //these are to determine the other dimension (the row num and col num) when adding the pixel in one dimension
            int rowStart = (int) Math.floor(r - rlen/2) - this.rowStart; //row start
            int rowEnd = (int) Math.floor(r + rlen/2) - this.rowStart; //include
            int colStart = (int) Math.floor(c - clen/2) - this.colStart; //col start
            int colEnd = (int) Math.floor(c + clen/2) - this.colStart; //include

            //these are actual location of the pixel
            double ccs = c - clen/2 - this.colStart;
            double cce = c + clen/2 - this.colStart;
            double rrs = r - rlen/2 - this.rowStart;
            double rre = r + rlen/2 - this.rowStart;

            //process column - horizontal:
            for (int i = rowStart; i <= rowEnd; i++) {
                addPixelHorizontally(ccs, cce, i);
            }
            //process row - vertical:
            for (int i = colStart; i <= colEnd; i++) {
                addPixelVertically(rrs, rre, i);
            }
        }

        /**
         * Adding the pixel horizontally: only consider the starting and ending point in the current row
         * @param start starting coordinate of the pixel
         * @param end ending coordinate of the pixel
         * @param row the current row
         */
        public void addPixelHorizontally(double start, double end, int row) {
            if (row < 0 || row >= rlength) return;
            int index = (int) Math.floor(start);
            if (index >= 0) {
                if (Math.abs(start - index) < 0.00001) {
                    if (!this.cells[row][index].isCoveredHorizontally) {
                        this.cells[row][index].isCoveredHorizontally = true; //this cell is fully covered horizontally
                    }
                    if (this.cells[row][index].isCoveredVertically && this.cells[row][index].isCoveredHorizontally) {
                        coveredCnt++; //ignore coveredCnt
                    }
                    return;

                }
                if (!this.cells[row][index].isCoveredHorizontally) {
                    this.cells[row][index].right = Math.max(this.cells[row][index].right, index + 1 - start);
                    if (this.cells[row][index].left + this.cells[row][index].right >= 1) {
                        this.cells[row][index].isCoveredHorizontally = true;  //this cell is fully covered horizontally
                        if (this.cells[row][index].isCoveredVertically && this.cells[row][index].isCoveredHorizontally) {
                            coveredCnt++;  //ignore coveredCnt
                        }
                    }
                }
            }

            if (index >= 0 && index + 1 < this.clength) {
                if (!this.cells[row][index + 1].isCoveredHorizontally) {
                    this.cells[row][index + 1].left = Math.max(this.cells[row][index + 1].left, end - index - 1);
                    if (this.cells[row][index + 1].left + this.cells[row][index + 1].right >= 1) {
                        this.cells[row][index + 1].isCoveredHorizontally = true;  //this cell is fully covered horizontally
                        if (this.cells[row][index].isCoveredVertically && this.cells[row][index].isCoveredHorizontally) {
                            coveredCnt++;
                        }
                    }
                }
            }
        }

        public void addPixelVertically(double start, double end, int col) {
            if (col < 0 || col >= clength) return;
            int index = (int) Math.floor(start);
            if (index >= 0) {
                if (Math.abs(start - index) < 0.00001) {
                    if (!this.cells[index][col].isCoveredVertically) {
                        this.cells[index][col].isCoveredVertically = true;  //this cell is fully covered vertically
                    }
                    if (this.cells[index][col].isCoveredVertically && this.cells[index][col].isCoveredHorizontally) {
                        coveredCnt++;
                    }
                    return;
                }
                if (!this.cells[index][col].isCoveredVertically) {
                    this.cells[index][col].up = Math.max(this.cells[index][col].up, index + 1 - start);
                    if (this.cells[index][col].down + this.cells[index][col].up >= 1) {
                        this.cells[index][col].isCoveredVertically = true; //this cell is fully covered vertically
                        if (this.cells[index][col].isCoveredVertically && this.cells[index][col].isCoveredHorizontally) {
                            coveredCnt++;
                        }
                    }
                }
            }

            if (index + 1 < this.rlength) {
                if (!this.cells[index + 1][col].isCoveredVertically) {
                    this.cells[index + 1][col].down = Math.max(this.cells[index + 1][col].down, end - index - 1);
                    if (this.cells[index + 1][col].down + this.cells[index + 1][col].up >= 1) {
                        this.cells[index + 1][col].isCoveredVertically = true; //this cell is fully covered vertically
                        if (this.cells[index][col].isCoveredVertically && this.cells[index][col].isCoveredHorizontally) {
                            coveredCnt++;
                        }
                    }
                }
            }
        }
        public int displayCount() {
            return coveredCnt;
        }

        /**
         * helper method to see if this cell is fully covered horizontally
         * @param row
         * @param start
         * @return true: fully covered, false: not fully covered
         */
        public boolean isCoveredHorizontally(int row, double start) {
            int index = (int)Math.floor(start);
            return cells[row][index].left + cells[row][index].right >= 1;
        }
        /**
         * helper method to see if this cell is fully covered vertically
         * @param col
         * @param start
         * @return true: fully covered, false: not fully covered
         */
        public boolean isCoveredVertically(double start, int col) {
            int index = (int)Math.floor(start);
            return cells[index][col].up + cells[index][col].down >= 1;
        }

        /**
         * This class is responsible for lat and lng conversion to coordinate in KM and store the r and c (row col)
         */
        class Coordinate {
            double r;
            double c;
            Coordinate() {}
            Coordinate(double lat, double lng) {
                r = convertLngToRow(lng);
                c = convertLatToCol(lat);
            }
        }
        double convertLatToCol(double lat) {
            double col = (lat - map0Lat) * 2 * Math.PI * RADIUS / 360;
            return col;
        }
        double convertLngToRow(double lng) {
            double y = (lng - map0Lng) * 2 * Math.PI * RADIUS / 360;
            return rlength - y;
        }
        // 1km per cell
        public class Cell {
            public double left;
            public double right;
            public double up;
            public double down;
            public boolean isCoveredHorizontally;
            public boolean isCoveredVertically;
            public boolean isCovered;
            public Cell() {
                this.left = 0;
                this.right = 0;
                this.up = 0;
                this.down = 0;
                this.isCoveredHorizontally = false;
                isCoveredVertically = false;
                isCovered = false;
            }
        }
    }


    /**
     * call this function to calculate the size of a fire given a list of modis information
     * @param modisInfoList
     * @return the size of the fire formed by the input pixels
     */
    public double calcSize( List<ModisInfo> modisInfoList) {
        if (modisInfoList == null || modisInfoList.size() == 0) return 0.0;
        double minLatitude = Double.MAX_VALUE;
        double maxLatitude = -(Double.MAX_VALUE-1);
        double minLongitude = Double.MAX_VALUE;
        double maxLongitude = -(Double.MAX_VALUE-1);

        //finding the 4 corners of the rectangle
        for (ModisInfo mi : modisInfoList) {
            double lat = mi.lat;
            double lon = mi.lng;
            if (lat < minLatitude) minLatitude = lat;
            if (lat > maxLatitude) maxLatitude = lat;
            if (lon < minLongitude) minLongitude = lon;
            if (lon > maxLongitude) maxLongitude = lon;
        }
        //initialize the fire grid
        FireGrid fg = new FireGrid(minLatitude, minLongitude, maxLatitude, maxLongitude);
        centerPoint = new Double[] {(minLatitude + maxLatitude)/2, (minLongitude + maxLongitude)/2};
        //add each pixel into the grid
        for (int i = 0; i < modisInfoList.size(); i++) {
            fg.grid.addPixel(modisInfoList.get(i).lat, modisInfoList.get(i).lng, modisInfoList.get(i).scan, modisInfoList.get(i).track);
        }
//        System.out.println("fire count1: " + fg.grid.coveredCnt);
//        System.out.println("fire total count: " + fg.grid.countTotalCovered());

        //calculate total covered size
        return fg.grid.countTotalCovered();
    }

    //testing
    public static void main(String[] args) {
        try {
//            FileReader.read("MODIS_sample.csv");      MODIS_C6_Southern_Africa_7d.csv
//            FileReader.read("MODIS_C6_USA_contiguous_and_Hawaii_7d.csv");
            ModisFileReader.read("data/MODIS_C6_Southern_Africa_7d.csv");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("min lat: "+ ModisFileReader.minLatitude + "; max lat: "+ ModisFileReader.maxLatitude);
        System.out.println("min lng: "+ ModisFileReader.minLongitude + "; max lng: "+ ModisFileReader.maxLongitude);
        FireGrid fg = new FireGrid(ModisFileReader.minLatitude, ModisFileReader.minLongitude, ModisFileReader.maxLatitude, ModisFileReader.maxLongitude);

        List<List<String>> rawData = ModisFileReader.rawData;
        for (int i = 1; i < rawData.get(0).size(); i++) {
            List<String> record = rawData.get(i);
            double lat = Double.parseDouble(record.get(0));
            double lng = Double.parseDouble(record.get(1));
            double scan = Double.parseDouble(record.get(3));
            double track = Double.parseDouble(record.get(4));
            fg.grid.addPixel(lat, lng, scan, track);
        }
        System.out.println("fire count1: " + fg.grid.coveredCnt);
        System.out.println("fire total count: " + fg.grid.countTotalCovered());
    }


}
