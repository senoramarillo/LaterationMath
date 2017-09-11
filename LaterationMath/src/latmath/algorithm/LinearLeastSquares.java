package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.ArrayUtils;
import latmath.util.Matrix;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;

/**
 * Multilateration using linear least squares solution.
 *
 * @version 1.0, 2011-07-26
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class LinearLeastSquares extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "LLS";
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
     * @param weights The weight of each anchor/reference node.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static PositionEstimate multilaterate(Point2d[] anchors, double[] ranges) {
        double[] weights = new double[anchors.length];
        ArrayUtils.fill(weights, 1);
        return multilaterate(anchors, ranges, weights);
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
     * @param weights The weight of each anchor/reference node.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static PositionEstimate multilaterate(Point2d[] anchors, double[] ranges, double[] weights) {
        // Solve equation of form W*A*x = W*b
        double e0;
        int m = anchors.length - 1;
        Point2d e = new Point2d();
        double[][] b = new double[m][1];
        double[][] a = new double[m][2];
        double[][] w = new double[m][m];
        for (int i = 0; i < m; i++) {
            a[i][0] = (anchors[i].x - anchors[m].x);
            a[i][1] = (anchors[i].y - anchors[m].y);
            b[i][0] = 0.5 * (anchors[i].x * anchors[i].x - anchors[m].x * anchors[m].x +
                    anchors[i].y * anchors[i].y - anchors[m].y * anchors[m].y +
                    ranges[m] * ranges[m] - ranges[i] * ranges[i]);
            for (int j = 0; j < anchors.length - 1; j++) {
                w[i][j] = (i == j) ? (weights[i] * weights[i]) : 0;
            }
        }

        // Solve with closed form solution: x = (A^T*W^2*A)^-1 * A^T * W^2 * b
        Matrix mA = new Matrix(a);
        Matrix mB = new Matrix(b);
        Matrix mW2 = new Matrix(w);
        Matrix mAT = mA.transpose();
        Matrix tmp = mAT.times(mW2);
        tmp = tmp.times(mA);
        tmp = tmp.inverse();
        if (tmp == null) {
            // Matrix was singular => no inverse
            return null;
        }
        tmp = tmp.times(mAT);
        tmp = tmp.times(mW2);
        tmp = tmp.times(mB);
        e.x = tmp.cell(0, 0);
        e.y = tmp.cell(1, 0);

        e0 = PositionEstimate.calculateResidualError(anchors, ranges, weights, e);
        return new PositionEstimate(e, e0);
    }

}
