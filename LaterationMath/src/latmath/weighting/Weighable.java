package latmath.weighting;

import java.io.Serializable;
import latmath.util.Configurable;
import latmath.util.Point2d;

/**
 * Basic interface all weighting methods have to implement. 
 * 
 * @version 1.0, 2013-05-12
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public interface Weighable extends Cloneable, Configurable, Serializable {
    
    /**
     * Returns the name of the weighting method.
     * <p>
     * The name should not be too long, e.g. less than 20 chars.
     *
     * @return The name of the weighting method.
     */
    String getName();
    
    /**
     * Perform a weighting for a given position, e.g. for calculating the
     * weighted centroid for a given set of positions.
     * <p>
     * Note that the returned weight might be {@code 0}.
     * 
     * @param position A position that should be weighted.
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     * 
     * @return A weight that should be used for the given position.
     */
    double weigh(Point2d position, Point2d[] anchors, double[] ranges);
    
}
