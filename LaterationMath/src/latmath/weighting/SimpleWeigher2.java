package latmath.weighting;

import java.awt.Frame;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Weighting method based on a given normal distribution.
 * 
 * @version 1.0, 2013-05-29
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class SimpleWeigher2 implements Releasable, Weighable {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public SimpleWeigher2() {}

    @Override
    public String getName() {
        return "Simple Weigher 2";
    }

    @Override
    public double weigh(Point2d position, Point2d[] anchors, double[] ranges) {
        double sum = 0.0;
        for (int k = 0; k < anchors.length; k++) {
            if (ranges[k] <= 0) continue;
            double tmpWeight = position.distance(anchors[k]) / ranges[k] - 1;
            sum += tmpWeight * tmpWeight;
        }
        return 1/sum;
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
