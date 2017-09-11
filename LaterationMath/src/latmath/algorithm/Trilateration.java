package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Standard trilateration formula.
 *
 * @version 1.0, 2011-07-27
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class Trilateration extends BasicLaterationAlgorithm implements Releasable {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "Trilateration";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return trilaterate(anchors, ranges);
    }

    /**
     * Returns a string representation of this lateration algorithm.
     *
     * @return A string representation of this lateration algorithm.
     */
    @Override
    public String toString() {
        return getName();
    }
    
    /**
     * Static call to this lateration algorithm.
     * <p>
     * This might be useful if one is only interested in the result and not
     * the additional features of the <code>BasicLaterationAlgorithm</code>
     * class.
     * <p>
     * If more than 3 anchor nodes are passed to this method, the first
     * three anchors will be taken for localization.
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     * 
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d trilaterate(Point2d[] anchors, double[] ranges) {
        if (anchors == null || ranges == null || anchors.length < 3 ||
                anchors.length != ranges.length) {
            return null;
        }
        return trilaterate(anchors[0], ranges[0], anchors[1], ranges[1],
                anchors[2], ranges[2]);
    }

    /**
     * Static call to this lateration algorithm.
     * <p>
     * This might be useful if one is only interested in the result and not
     * the additional features of the <code>BasicLaterationAlgorithm</code>
     * class.
     *
     * @param v1 The first anchor/reference node.
     * @param r1 The measured distance to the first anchor/reference node.
     * @param v2 The second anchor/reference node.
     * @param r2 The measured distance to the second anchor/reference node.
     * @param v3 The third anchor/reference node.
     * @param r3 The measured distance to the third anchor/reference node.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d trilaterate(Point2d v1, double r1, Point2d v2,
            double r2, Point2d v3, double r3) {
        // copy values because we might swap coordinates
        double v1x = v1.x;
        double v1y = v1.y;
        double v2x = v2.x;
        double v2y = v2.y;
        double v3x = v3.x;
        double v3y = v3.y;
        if (v2x == v3x) {
            double tmpX = v2x;
            double tmpY = v2y;
            double tmpR = r2;
            v2x = v1x;
            v2y = v1y;
            r2 = r1;
            v1x = tmpX;
            v1y = tmpY;
            r1 = tmpR;
        }
        // still equal, than exit => all have same x-coordinate
        if (v2x == v3x) {
            return null;
        }
        double r2Squared = r2*r2;
        double v2xSquared = v2x*v2x;
        double v2ySquared = v2y*v2y;
        double s = (v3x*v3x - v2xSquared + v3y*v3y - v2ySquared + r2Squared - r3*r3) / 2.0;
        double t = (v1x*v1x - v2xSquared + v1y*v1y - v2ySquared + r2Squared - r1*r1) / 2.0;
        double div = (((v1y - v2y)*(v3x - v2x)) - ((v3y - v2y)*(v1x - v2x)));
        if (div == 0) {
            return null;
        }
        double y = ((t * (v3x - v2x) - s * (v1x - v2x))) / div;
        double x =  (s - y * (v3y - v2y)) / (v3x - v2x);
        return new Point2d(x, y);
    }

}
