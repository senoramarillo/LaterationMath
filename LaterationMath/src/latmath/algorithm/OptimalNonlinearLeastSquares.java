package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.ArrayUtils;
import latmath.util.Matrix;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;

/**
 * Multilateration using nonlinear least squares solution.
 * <p>
 * This version uses the real location of the mobile node as a starting point
 * for the iterative optimization process (if known).
 *
 * @version 1.0, 2012-03-25
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class OptimalNonlinearLeastSquares extends BasicLaterationAlgorithm implements Releasable {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
        
    @Override
    public String getName() {
        return "NLLS-O";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        PositionEstimate pe = multilaterate(anchors, ranges, actualPosition);
        return pe != null ? pe.getLocation() : null;
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
     * @param actualPosition The actual location of the mobile node to be located.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static PositionEstimate multilaterate(Point2d[] anchors, double[] ranges, Point2d actualPosition) {
        // Starting point of optimization: s0 = (u0, v0)
        Point2d s0 = new Point2d();

        // 1a. Set starting point of optimization to linear least squares result
        double[] weights = new double[anchors.length];
        ArrayUtils.fill(weights, 1);
        if (actualPosition == null) {
            PositionEstimate pe = LinearLeastSquares.multilaterate(anchors, ranges, weights);
            if (pe == null) {
                // Linear least squares failed => couldn't calc inverse of matrix.
                // 1b. Set different starting point, the choice is somewhat
                //     arbitrary but the centroid is a good one.
                for (int i = 0; i < anchors.length; i++) {
                    s0.x += anchors[i].x;
                    s0.y += anchors[i].y;
                }
                s0.x /= anchors.length;
                s0.y /= anchors.length;
            } else {
                s0 = pe.getLocation();
            }
        } else {
            s0.x = actualPosition.x; // make real copy else
            s0.y = actualPosition.y; // evaluation will fail
        }

        double e0, e1;
        int iterations = 0;
        double epsilon = 0.001;
        Point2d r0 = new Point2d();

        iterLoop:
        do {
            // initial squared error
            e0 = PositionEstimate.calculateResidualError(anchors, ranges, weights, s0);

            // 2. Solve equation of form A*x = b, A is A.length x 2
            double[][] b = new double[anchors.length][1];
            double[][] a = new double[anchors.length][2];
            for (int i = 0; i < anchors.length; i++) {
                double dist = s0.distance(anchors[i]);
                if (dist == 0) {
                    break iterLoop;
                }
                a[i][0] = (s0.x - anchors[i].x) / dist;
                a[i][1] = (s0.y - anchors[i].y) / dist;
                b[i][0] = (ranges[i] - dist) + (a[i][0] * s0.x + a[i][1] * s0.y);
            }

            // Solve with closed form solution: x = (A^T*A)^-1 * A^T * b
            Matrix mA = new Matrix(a);
            Matrix mB = new Matrix(b);
            Matrix mAT = mA.transpose();
            Matrix tmp = mAT.times(mA);
            tmp = tmp.inverse();
            if (tmp == null) {
                // Matrix was singular => no inverse
                break;
            }
            tmp = tmp.times(mAT);
            tmp = tmp.times(mB);
            r0.x = tmp.cell(0, 0);
            r0.y = tmp.cell(1, 0);

            // new squared error
            e1 = PositionEstimate.calculateResidualError(anchors, ranges, weights, r0);
            if (e0 - e1 < epsilon) break;

            // Set refined position for next step
            s0.x = r0.x;
            s0.y = r0.y;
            iterations++;

        } while (iterations <= 10);

        return new PositionEstimate(s0, e0);
    }

}
