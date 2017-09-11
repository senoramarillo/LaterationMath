package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Circle;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Adapted Multi-Lateration (AML).
 * <p>
 * AML is described in "Localization in Wireless Sensor Networks with
 * Range Measurement Errors", Gulnur Selda Kuruoglu, Melike Erol and
 * Sema Oktug, 2009.
 *
 * @version 1.2, 2012-01-28
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class AdaptedMultilateration extends BasicLaterationAlgorithm implements Releasable {

    private int firstStepMode;

    public static final int MODE_ARBITRARY = 1;
    public static final int MODE_RANDOM    = 2;

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public AdaptedMultilateration() {
        this(MODE_RANDOM);
    }

    public AdaptedMultilateration(int firstStepMode) {
        this.firstStepMode = firstStepMode;
    }

    @Override
    public String getName() {
        return firstStepMode == MODE_RANDOM ? "AML-RAND" : "AML-ARBTRY";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, firstStepMode);
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
     * @param firstStepMode Arbitrary or random choice for the first step of
     *                      the AML algorithm.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            int firstStepMode) {

        int i = -1, j = -1;
        Point2d[] intersection = null;
        int[] refinement = new int[anchors.length-2];

        // This is test code which uses random anchor selection in the first
        // step of the AML algorithm (naive implementation, use instead of
        // the find loop):
        if (firstStepMode == MODE_RANDOM) {
            java.util.Random rand = new java.util.Random();
            int c = 0;
            while (intersection == null && c < 100) {
                i = rand.nextInt(anchors.length);
                j = rand.nextInt(anchors.length);
                while (j == i) {
                    j = rand.nextInt(anchors.length);
                }
                intersection = Circle.getIntersection(anchors[i], ranges[i],
                                anchors[j], ranges[j]);
                c++;
            }
        } else {
            // find two circles (arbitrary), which intersect in one or two points
            findLoop: for (i = 0; i < anchors.length-1; i++) {
                for (j = i+1; j < anchors.length; j++) {
                    intersection = Circle.getIntersection(anchors[i], ranges[i],
                            anchors[j], ranges[j]);
                    if (intersection != null) break findLoop;
                }
            }
        }

        // no intersection => exit
        if (intersection == null) {
            return null;
        }

        // store unused anchors for refinement step
        int idx = 0;
        for (int k = 0; k < anchors.length; k++) {
            if (k != i && k != j) refinement[idx++] = k;
        }

        Point2d pIntersect = intersection[0];
        // if  more than one intersection point, do elimination step
        if (intersection.length > 1) {
            Point2d p1 = intersection[0];
            Point2d p2 = intersection[1];
            int a3 = refinement[0];
            double deltaP1 = p1.distance(anchors[a3]) - ranges[a3];
            double deltaP2 = p2.distance(anchors[a3]) - ranges[a3];
            if (Math.abs(deltaP1) < Math.abs(deltaP2)) {
                pIntersect = p1;
            } else {
                pIntersect = p2;
            }
        }

        // do first estimation step and refinement
        for (int k = 0; k < refinement.length; k++) {
            int ki = refinement[k];
            // case 1: point outside of circle
            if (pIntersect.distance(anchors[ki]) > ranges[ki]) {
                double deltaPI = pIntersect.distance(anchors[ki]) - ranges[ki];
                double rr = (deltaPI/2) / (deltaPI + ranges[ki]);
                double deltaXout = (anchors[ki].x - pIntersect.x) * rr;
                double deltaYout = (anchors[ki].y - pIntersect.y) * rr;
                pIntersect.x += deltaXout;
                pIntersect.y += deltaYout;
            }
            // case 2: point inside of circle
            else {
                double deltaPI = pIntersect.distance(anchors[ki]) - ranges[ki];
                double rr = (Math.abs(deltaPI) / 2) / ranges[ki];
                double div = 1 - 2 * rr;
                double deltaXin = ((pIntersect.x - anchors[ki].x) * rr) / div;
                double deltaYin = ((pIntersect.y - anchors[ki].y) * rr) / div;
                pIntersect.x += deltaXin;
                pIntersect.y += deltaYin;
            }
        }

        return pIntersect;
    }

}
