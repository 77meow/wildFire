// 1d prototype of the fire grid: no use

public class Wildfire {

    public class Interval {
        public double left;
        public double right;
        public boolean isCovered;
        public Interval() {
            this.left = 0;
            this.right = 0;
            this.isCovered = false;
        }
    }

    public class Grid {
        private int coveredCnt;
        public int length;
        public Interval[] intervals;

        public Grid(int count) {
            this.coveredCnt = 0;
            this.length = count;
            this.intervals = new Interval[count];
            for (int i = 0; i < this.length; i++) {
                this.intervals[i] = new Interval();
            }
        }

        public void addPixel(double start) {
            double end = start + 1;
            int index = (int) Math.floor(start);
            if (index >= 0) {
                if (Math.abs(start - index) < 0.00001) {
                    if (!this.intervals[index].isCovered) {
                        this.coveredCnt++;
                        this.intervals[index].isCovered = true;
                    }
                    return;

                }
                if (!this.intervals[index].isCovered) {
                    this.intervals[index].right = Math.max(this.intervals[index].right, index + 1 - start);
                    if (this.intervals[index].left + this.intervals[index].right >= 1) {
                        this.intervals[index].isCovered = true;
                        this.coveredCnt++;
                    }
                }
            }

            if (index + 1 < this.length) {
                if (!this.intervals[index + 1].isCovered) {
                    this.intervals[index + 1].left = Math.max(this.intervals[index + 1].left, end - index - 1);
                    if (this.intervals[index + 1].left + this.intervals[index + 1].right >= 1) {
                        this.intervals[index + 1].isCovered = true;
                        this.coveredCnt++;
                    }
                }
            }
        }
        public int displayCount() {
            return coveredCnt;
        }

        public boolean isCovered(double start) {
            int index = (int)Math.floor(start);
            return intervals[index].left + intervals[index].right >= 1;
        }

        public boolean isAllCovered() {
            return this.coveredCnt == this.length;
        }



    }
}
