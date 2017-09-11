package latmath.weighting;

import java.awt.Frame;
import latmath.util.Point2d;
import latmath.util.PreciseStandardDeviation;
import latmath.util.Releasable;
import latmath.util.StandardDeviation;

/**
 * Weighting method based on membership function (MF).
 * 
 * @version 1.0, 2013-05-12
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class MFWeigher implements Releasable, Weighable {
    
    /**
     * The following four attributes (implemented as array) characterise
     * the fuzzy membership function, which is specified to be a triangle.
     */
    private double[] mfAttrs;
    
    /** For accessing the four attributes in the array. */
    private static final int LOW_RATE = 0;
    private static final int MEAN_LOW = 1;
    private static final int MEAN_UP  = 2;
    private static final int UP_RATE  = 3;
    
    private static final double EPSILON = Math.pow(2, -24);
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public MFWeigher() {
        init();
    }
    
    private void init() {
        mfAttrs = new double[4];
        mfAttrs[LOW_RATE] = -2.1610;
        mfAttrs[MEAN_LOW] =  1.6362;
        mfAttrs[MEAN_UP] =  1.6362;
        mfAttrs[UP_RATE] = 16.0428;
    }
    
    @Override
    public String getName() {
        return "MF Weigher";
    }

    @Override
    public double weigh(Point2d position, Point2d[] anchors, double[] ranges) {
        StandardDeviation stddev = new PreciseStandardDeviation();
        for (int k = 0; k < anchors.length; k++) {
            double tmp = ranges[k] - position.distance(anchors[k]);
            // Is this what we mean?
            double up = (tmp - mfAttrs[LOW_RATE]) / (mfAttrs[MEAN_LOW] - mfAttrs[LOW_RATE]);
            double down = (mfAttrs[UP_RATE] - tmp) / (mfAttrs[UP_RATE] - mfAttrs[MEAN_UP]);
            stddev.addSample(clamp(Math.min(up, down), EPSILON, 1.0));
        }
        double mean = stddev.mean();
        double sdev = stddev.standardDeviation();
        return  mean / (sdev > EPSILON ? sdev : EPSILON);
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    private static double clamp(double value, double low, double high) {
        return value > high ? high : (value < low ? low : value);
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
