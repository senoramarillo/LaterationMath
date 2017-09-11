package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Min-Max (aka Bounding-Box) algorithm.
 *
 * @version 1.0, 2012-02-23
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class MinMax extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "MIN-MAX";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges);
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
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges) {
        // step 0: sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

	double north = anchors[0].y - ranges[0],
               west = anchors[0].x - ranges[0],
               east = anchors[0].x + ranges[0],
               south = anchors[0].y + ranges[0];
	
        for (int i = 1; i < anchors.length; ++i) {
		north = Math.max(north, anchors[i].y - ranges[i]);
		west = Math.max(west, anchors[i].x - ranges[i]);
		east = Math.min(east, anchors[i].x + ranges[i]);
		south = Math.min(south, anchors[i].y + ranges[i]);
        }
	return new Point2d(west * 0.5 + east * 0.5, north * 0.5 + south * 0.5);
    }
}
