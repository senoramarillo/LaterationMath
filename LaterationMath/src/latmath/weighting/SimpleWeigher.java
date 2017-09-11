package latmath.weighting;

import java.awt.Frame;
import latmath.util.Point2d;
import latmath.util.PreciseStandardDeviation;
import latmath.util.Releasable;
import latmath.util.StandardDeviation;

/**
 * Weighting method based on a given normal distribution.
 * 
 * @version 1.0, 2013-05-12
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class SimpleWeigher implements Releasable, Weighable {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public SimpleWeigher() {}

    @Override
    public String getName() {
        return "Simple Weigher";
    }

    @Override
    public double weigh(Point2d position, Point2d[] anchors, double[] ranges) {
        StandardDeviation stddev = new PreciseStandardDeviation();
        for (int k = 0; k < anchors.length; k++) {
            double tmpWeight = position.distance(anchors[k]) / (ranges[k] > 0 ? ranges[k] : 0.001);
            stddev.addSample(tmpWeight);
        }
        double mean = stddev.mean();
        double sdev = stddev.standardDeviation();
        return (1 / (mean > 0 ? mean : 0.00001)) / (sdev > 0 ? sdev : 0.00001);
    }
    
    @Override
    public String toString() {
        return getName();
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
