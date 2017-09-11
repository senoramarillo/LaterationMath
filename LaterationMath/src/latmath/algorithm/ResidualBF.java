package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;

/**
 * Residual brute force algorithm.
 *
 * @version 1.0, 2012-11-06
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ResidualBF extends BasicLaterationAlgorithm implements Releasable {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "ResidualBF";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, width, height);
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
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     * @param width The width of the playing field used for simulation or
     *              <code>-1</code> if running with real data.
     * @param height The height of the playing field used for simulation or
     *              <code>-1</code> if running with real data.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges, int width, int height) {
        // step 0: sanity check
        if (anchors.length != ranges.length || width == -1 || height == -1) {
            return null;
        }

        double finalX = 0, finalY = 0;
        double minRes = Double.MAX_VALUE;
        Point2d p = new Point2d();
        for (double y = 0; y < height; y++) {
            for (double x = 0; x < width; x++) {
                p.x = x;
                p.y = y;
                double res = PositionEstimate.calculateResidualError(anchors, ranges, p);
                if (res < minRes) {
                    finalX = x;
                    finalY = y;
                    minRes = res;
                }
            }
        }
        return new Point2d(finalX, finalY);
    }

}
