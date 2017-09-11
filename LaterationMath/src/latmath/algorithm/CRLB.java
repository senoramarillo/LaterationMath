package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.errormodel.ErrorModelLos;
import latmath.util.Matrix;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Cramer-Rao Lower Bound (CRLB).
 * <p>
 * Works only if measurement errors are zero - mean Gaussian distributed.
 *
 * @version 1.0, 2012-10-20
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class CRLB extends BasicLaterationAlgorithm implements LowerBoundEstimator, Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
   
    @Override
    public String getName() {
        return "CRLB";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, actualPosition, errorModel);
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
     * @param actualPosition The real position.
     * @param errorModel The error model, must be Gaussian!
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel) {
        
        if (!(errorModel instanceof ErrorModelLos)) {
            return null;
        }
        
        double variance = ((ErrorModelLos)errorModel).getVariance();
        double[][] a = new double[2][2];
        for (int l = 0; l < anchors.length; l++) {
            double xDiff = (actualPosition.x - anchors[l].x);
            double yDiff = (actualPosition.y - anchors[l].y);
            double dSquare = xDiff * xDiff + yDiff * yDiff;
            a[0][0] += (xDiff * xDiff) / (variance * dSquare);
            a[0][1] += (xDiff * yDiff) / (variance * dSquare);
            a[1][0] += (xDiff * yDiff) / (variance * dSquare);
            a[1][1] += (yDiff * yDiff) / (variance * dSquare);
        }
        
        Matrix mA = new Matrix(a);
        mA = mA.inverse();
        double crlb = mA.cell(0, 0) + mA.cell(1, 1);
        return new Point2d(crlb, crlb);
    }

}
