package latmath.weighting;

import java.awt.Frame;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Weighting method based on a given normal distribution.
 * 
 * @version 1.0, 2013-05-12
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class GaussWeigher implements Releasable, Weighable {
    
    private double mean = 2.43;
    private double sdev = 3.57;
    private double c;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public GaussWeigher() {
        init();
    }
    
    private void init() {
        c = 1 / Math.sqrt(2 * Math.PI * sdev * sdev);
    }
    
    @Override
    public String getName() {
        return "Gauss Weigher";
    }

    @Override
    public double weigh(Point2d position, Point2d[] anchors, double[] ranges) {
        double result = 1.0;
        for (int i = 0; i < anchors.length; i++) {
            double x = ranges[i] - position.distance(anchors[i]);
            double t = (x - mean);
            double probability = c * Math.exp(-(t*t) / (2 * sdev * sdev));
            result *= probability;
        }
        return result;
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
