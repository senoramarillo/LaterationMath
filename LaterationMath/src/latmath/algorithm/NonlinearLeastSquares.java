package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.ArrayUtils;
import latmath.util.Matrix;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;

/**
 * Multilateration using nonlinear least squares solution.
 *
 * @version 1.0, 2011-07-26
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class NonlinearLeastSquares extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "NLLS";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        PositionEstimate pe = multilaterate(anchors, ranges);
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
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static PositionEstimate multilaterate(Point2d[] anchors, double[] ranges) {
        // sanity check
        if (anchors.length != ranges.length || anchors.length < 3) {
            return null;
        }

        // Starting point of optimization: s0 = (u0, v0)
        Point2d s0;

        // Fill weight matrix
        double[] weights = new double[anchors.length];
        ArrayUtils.fill(weights, 1);

        // Try different starting points and choose the one with the smallest
        // residual error as result
        Point2d[] sPoint = new Point2d[3];
        double minResidualError = Double.MAX_VALUE;
        int minResidualErrorIdx = -1;

        // Take the following points as starting point of optimization:
        //
        // 1. Linear least squares result
        PositionEstimate pe = LinearLeastSquares.multilaterate(anchors, ranges, weights);
        sPoint[0] = pe != null ? pe.getLocation() : null; // might be null

        // 2. Centroid
        sPoint[1] = Point2d.centerOfMass(anchors, weights);
        
        // 3. Min-Max
        sPoint[2] = MinMax.multilaterate(anchors, ranges); // might be null
        
        valueLoop:
        for (int j = 0; j < sPoint.length; j++) {

            s0 = sPoint[j];
            if (s0 == null) {
                continue;
            }

            double e0, e1;
            int iterations = 0;
            double epsilon = 0.001;
            Point2d r0 = new Point2d();

            do {
                // initial squared error
                e0 = PositionEstimate.calculateResidualError(anchors, ranges, weights, s0);

                // 2. Solve equation of form A*x = b, A is A.length x 2
                double[][] b = new double[anchors.length][1];
                double[][] a = new double[anchors.length][2];
                for (int i = 0; i < anchors.length; i++) {
                    double dist = s0.distance(anchors[i]);
                    if (dist == 0) {
                        // avoid NaN
                        continue valueLoop;
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
                if (e0 - e1 < epsilon) {
                    break;
                }

                // Set refined position for next step
                s0.x = r0.x;
                s0.y = r0.y;
                iterations++;

            } while (iterations <= 10);

            // check if new result is better than last one
            if (e0 < minResidualError) {
                minResidualError = e0;
                minResidualErrorIdx = j;
            }
        }

        if (minResidualErrorIdx != -1) {
            return new PositionEstimate(sPoint[minResidualErrorIdx],
                    minResidualError);
        } else {
            return null;
        }
    }

}
