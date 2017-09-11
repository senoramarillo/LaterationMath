package latmath.filter;

import java.util.Arrays;
import java.util.ArrayList;

public class MedianFilter {

    private int size;
    private int flush;
    private int flushLimit;
    private ArrayList list;

    public MedianFilter(int size, int flushLimit) {
        this.size = size;
        this.flushLimit = flushLimit;
        list = new ArrayList(size);
    }

    public void add(double d) {
        list.add(d);
        flush = 0;
        if (list.size() > size) {
            list.remove(0);
        }
    }

    public void incFlush() {
        flush++;
        if (flush > flushLimit) {
            flush = 0;
            list.clear();
        }
    }

    public double getMedian() {
        if (list.isEmpty()) {
            return -1.0;
        }
        Double[] values = (Double[]) ((ArrayList) list.clone()).toArray(new Double[0]);
        Arrays.sort(values);
        return values[values.length/2];
    }

    public double getMedian4() {
        if (list.isEmpty()) {
            return -1.0;
        }
        Double[] values = (Double[]) ((ArrayList) list.clone()).toArray(new Double[0]);
        Arrays.sort(values);
        return values[values.length/4];
    }

}
