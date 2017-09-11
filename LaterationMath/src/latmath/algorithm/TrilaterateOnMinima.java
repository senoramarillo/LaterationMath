package latmath.algorithm;

import latmath.anchorselection.AnchorSelectionResult;
import latmath.anchorselection.MinimumAnchorSelection;
import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Trilaterate on Minima (ToM).
 * <p>
 * ToM is described in "Evaluating LaterationBased Positioning Algorithms for
 * FineGrained Tracking", Andrew Rice, Robert Harle, 2005.
 *
 * @version 1.0, 2013-07-01
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class TrilaterateOnMinima extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "Trilaterate on Minima (ToM)";
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
        if (anchors == null || ranges == null || anchors.length < 3 ||
                anchors.length != ranges.length) {
            return null;
        }
        
        // default value of count in MMS is 3, that is what we need
        MinimumAnchorSelection selection = new MinimumAnchorSelection();
        AnchorSelectionResult res = selection.select(anchors, ranges, null);
        Point2d[] a = res.getAnchors();
        double[] d = res.getMeasuredDistances();
        
        return Trilateration.trilaterate(
                a[0], d[0],
                a[1], d[1],
                a[2], d[2]);
    }
    
}
