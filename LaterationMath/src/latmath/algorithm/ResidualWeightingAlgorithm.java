package latmath.algorithm;

import java.util.LinkedList;
import java.util.List;
import latmath.errormodel.ErrorModel;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;

/**
 * Residual weighting algorithm (Rwgh).
 * <p>
 * Rwgh is described in "A non-line-of-sight error mitigation algorithm in
 * location estimation", Pi-Chun Chen, 1999.
 *
 * @version 1.0, 2011-08-03
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class ResidualWeightingAlgorithm extends BasicLaterationAlgorithm implements Releasable {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "Rwgh";
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
        int s = 3;
        int M = anchors.length;
        List<PositionEstimate> intermediatePositions = new LinkedList<>();

        // calculate all k-permutations for k = s to M
        for (int k = s; k <= M; k++) {

            int binom = LMath.binom(M, k);
            int[] permutations = new int[k];
            double[] tmpRanges = new double[k];
            Point2d[] tmpAnchors = new Point2d[k];

            // initialisation for calculating k-permutations
            for (int i = 0; i < permutations.length; i++) {
                permutations[i] = i;
            }

            // build all k-permutations
            for (int i = 0; i < binom; i++) {

                // calculate intermediate position estimate using non-linear
                // least squares multilateration
                for (int h = 0; h < k; h++) {
                    tmpAnchors[h] = anchors[permutations[h]];
                    tmpRanges[h] = ranges[permutations[h]];
                }

                PositionEstimate iRes = NonlinearLeastSquares.multilaterate(tmpAnchors, tmpRanges);
                if (iRes != null) {
                    // normalize residual error by number of anchors
                    iRes.setResidualError(iRes.getResidualError()/k);
                    intermediatePositions.add(iRes);
                }

                // build next permutation
                if (i == binom - 1) {
                    break;
                }
                int j = k - 1;
                while (j >= 0) {
                    if (!LMath.incCounter(permutations, j, M, k)) {
                        break;
                    }
                    j--;
                }
                for (int l = j+1; l < k; l++) {
                    permutations[l] = permutations[l-1] + 1;
                }
            }
        }

        double res = 0;
        Point2d x = new Point2d(0, 0);
        PositionEstimate[] pts = intermediatePositions.toArray(new PositionEstimate[0]);
        for (int i = 0; i < pts.length; i++) {
            x.x += pts[i].getLocation().x * (1/pts[i].getResidualError());
            x.y += pts[i].getLocation().y * (1/pts[i].getResidualError());
            res += 1/pts[i].getResidualError();
        }
        x.x /= res;
        x.y /= res;
        return x;
    }

}
