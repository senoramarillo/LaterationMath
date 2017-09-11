package latmath.algorithm;

import java.util.ArrayList;
import java.util.List;
import latmath.errormodel.ErrorModel;
import latmath.util.Circle;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Bilateration.
 * <p>
 * Based on the pseudo-code from paper "A Low-Complexity Geometric Bilateration
 * Method for Localization in Wireless Sensor Networks and Its Comparison with
 * Least-Squares Methods" (Juan Cota-Ruiz).
 *
 * @version 1.0, 2011-09-03
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class Bilateration extends BasicLaterationAlgorithm implements Releasable {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    /**
     * Create a new instance of <code>Bilateration</code>.
     */
    public Bilateration() {}

    @Override
    public String getName() {
        return "Bilateration";
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

        // step 1: calculate circle intersections with approximation
        int n = anchors.length;
        int binom = LMath.binom(n, 2);
        Point2d[][] intersections = new Point2d[binom][];

        // build all k-permutations
        int num = 0;
        for (int i = 0; i < anchors.length-1; i++) {
            for (int j = i+1; j < anchors.length; j++) {
                intersections[num] = Circle.getIntersection(anchors[i],
                        ranges[i], anchors[j], ranges[j]);
                // no intersection => try to get approximated intersection
                if (intersections[num] == null) {
                    Point2d pft = Circle.getIntersectionApprox2(
                            anchors[i], ranges[i],
                            anchors[j], ranges[j]);
                    if (pft != null) {
                        intersections[num] = new Point2d[] {pft};
                    }
                }
                if (intersections[num] != null) {
                    num++;
                }
            }
        }

        // List of points finally used for localization
        List<Point2d> listT = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            // No decision on single intersections
            Point2d[] currentI = intersections[i];
            if (currentI.length == 1) {
                listT.add(currentI[0]);
                continue;
            }
            double psi = 0;
            double phi = 0;
            for (int j = 0; j < num; j++) {
                if (i != j) {
                    double deltaPsi;
                    double deltaPhi;
                    Point2d[] compareI = intersections[j];
                    if (compareI.length == 1) {
                        deltaPsi = currentI[0].distanceSquared(compareI[0]);
                        deltaPhi = currentI[1].distanceSquared(compareI[0]);
                    } else {
                        deltaPsi = Math.min(currentI[0].distanceSquared(compareI[0]),
                                currentI[0].distanceSquared(compareI[1]));
                        deltaPhi = Math.min(currentI[1].distanceSquared(compareI[0]),
                                currentI[1].distanceSquared(compareI[1]));
                    }
                    psi += deltaPsi;
                    phi += deltaPhi;
                }
            }
            if (psi < phi) {
                listT.add(currentI[0]);
            } else {
                listT.add(currentI[1]);
            }
        }
        
        // Final position is centroid of all left points
        return Point2d.centerOfMass(listT.toArray(new Point2d[0]));
    }

}
