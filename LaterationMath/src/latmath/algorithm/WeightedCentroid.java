package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.ArrayUtils;
import latmath.util.Circle;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.Releasable;
import latmath.weighting.GammaWeigher;
import latmath.weighting.Weighable;

/**
 * Centroid algorithm using a weigher for the final points.
 * <p>
 * This algorithm calculates all intersection points of all circles and takes
 * the center of mass of the weighted points as the final result.
 *
 * @version 1.0, 2013-05-12
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class WeightedCentroid extends BasicLaterationAlgorithm implements Releasable {

    /**
     * Selected weighting method.
     */
    private Weighable weigher;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public WeightedCentroid() {
        weigher = new GammaWeigher();
    }
    
    @Override
    public String getName() {
        String s = "CENTROID";
        if (weigher != null) {
            s += " (" + weigher.getName()+ ")";
        }
        return s;
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, weigher);
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
     * @param weigher Weighting method for final points.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            Weighable weigher) {
        // step 0: sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

        // step 1: calculate circle intersections
        int n = anchors.length;
        int k = 2;
        int[] permutations = new int[k];
        int binom = LMath.binom(n, k);
        Point2d[][] intersections = new Point2d[binom][];

        // initialisation for calculating k-permutations
        for (int i = 0; i < permutations.length; i++) {
            permutations[i] = i;
        }

        // build all k-permutations
        for (int i = 0; i < binom; i++) {
            intersections[i] =
                    Circle.getIntersection(anchors[permutations[0]],
                    ranges[permutations[0]], anchors[permutations[1]],
                    ranges[permutations[1]]);
            // build next permutation
            if (i == binom - 1) break;
            int j = k - 1;
            while (j >= 0) {
                if (!LMath.incCounter(permutations, j, n, k)) break;
                j--;
            }
            for (int l = j+1; l < k; l++) {
                permutations[l] = permutations[l-1] + 1;
            }
        }

        // copy intersections into one array
        int totalLength = 0;
        for (int i = 0; i < intersections.length; i++) {
            if (intersections[i] == null) continue;
            totalLength += intersections[i].length;
        }

        // check if any intersections available
        if (totalLength == 0) {
            return null;
        }

        // execute the copying
        int currentLength = 0;
        Point2d[] points = new Point2d[totalLength];
        for (int i = 0; i < intersections.length; i++) {
            if (intersections[i] == null) continue;
            for (int j = 0; j < intersections[i].length; j++) {
                points[currentLength++] = intersections[i][j];
            }
        }
        
        // calculate weight for points
        double[] masses = new double[points.length];
        boolean allZero = true;
        for (int i = 0; i < masses.length; i++) {
            masses[i] = weigher.weigh(points[i], anchors, ranges);
            if (masses[i] != 0.0) {
                allZero = false;
            }
        }
        // if all weights would be zero, keep all points with equal weight
        if (allZero) {
            ArrayUtils.fill(masses, 1.0);
        }

        // return center of mass as result
        return Point2d.centerOfMass(points, masses);
    }

}
