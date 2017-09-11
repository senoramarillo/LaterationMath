package latmath.anchorselection;

import java.awt.Frame;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Hashtable;
import latmath.algorithm.LaterationAlgorithm;
import latmath.filter.MedianFilter;
import latmath.location.filter.LocationFilter;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Simple implementation of anchor selection algorithm based on measured distances.
 *
 * @version 1.0, 2012-02-24
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class MedianBasedAnchorSelection implements AnchorSelection, Releasable {

    private transient Hashtable<Point2d, AnchorSpeedHistory> lastValues;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    private class AnchorSpeedHistory implements Comparable<AnchorSpeedHistory> {
        public Point2d anchor;
        public double speed;
        public double lastDistance;
        public MedianFilter filter;

        public AnchorSpeedHistory(Point2d anchor) {
            this.anchor = anchor;
            this.speed = 0;
            this.lastDistance = 0;
            this.filter = new MedianFilter(100, 100);
        }

        @Override
        public int compareTo(AnchorSpeedHistory o) {
            double median1 = filter.getMedian();
            double median2 = o.filter.getMedian();
            return Double.compare(Math.abs(median1-speed), Math.abs(median2-o.speed));
        }
    }

    public MedianBasedAnchorSelection() {
        lastValues = new Hashtable<>();
    }
    
    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();
            lastValues = new Hashtable<>();
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "MED-SELECT";
    }

    @Override
    public void deriveLogFilename(int uid, LaterationAlgorithm algorithm, LocationFilter filter) {
        // no logging needed
    }

    @Override
    public AnchorSelectionResult select(Point2d[] anchors, double[] measuredDistances, Point2d lastLocation) {
        AnchorSpeedHistory[] tmp = new AnchorSpeedHistory[anchors.length];
        for (int i = 0; i < anchors.length; i++) {
            AnchorSpeedHistory ash = lastValues.get(anchors[i]);
            if (ash == null) {
                ash = new AnchorSpeedHistory(anchors[i]);
                ash.lastDistance = measuredDistances[i];
                lastValues.put(anchors[i], ash);
            } else {
                double dist = Math.abs(ash.lastDistance - measuredDistances[i]);
                ash.speed = dist;
                ash.filter.add(dist);
                ash.lastDistance = measuredDistances[i];
            }
            tmp[i] = ash;
        }

        Arrays.sort(tmp);
        int anchorLimit = tmp.length - 2;
        int length = anchorLimit == 0 ? tmp.length : Math.max(anchorLimit, Math.min(5, anchors.length));
        Point2d[] a = new Point2d[length];
        double[] md = new double[length];
        for (int i = 0; i < length; i++) {
            a[i] = tmp[i].anchor;
            md[i] = tmp[i].lastDistance;
        }
        return new AnchorSelectionResult(a, md);
    }

    @Override
    public AnchorSelectionResult select(Point2d[] anchors, double[] measuredDistances, Point2d lastLocation, long timestamp) {
        return select(anchors, measuredDistances, lastLocation);
    }

    @Override
    public void reset() {
        lastValues.clear();
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public boolean configure(Frame parent) {
        return false;
    }

}
