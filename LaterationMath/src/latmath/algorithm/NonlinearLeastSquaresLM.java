package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Matrix;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Multilateration using nonlinear least squares solution based on the
 * Levenberg–Marquardt algorithm (LMA).
 * <p>
 * Based on the pseudo-code from paper "A Low-Complexity Geometric Bilateration
 * Method for Localization in Wireless Sensor Networks and Its Comparison with
 * Least-Squares Methods" (Juan Cota-Ruiz).
 *
 * @version 1.0, 2012-08-31
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class NonlinearLeastSquaresLM extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "NLLS-LM";
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
        // sanity check
        if (anchors.length != ranges.length || anchors.length < 3) {
            return null;
        }

        // Starting point of optimization: s0 = (u0, v0)
        Point2d s0 = Point2d.centerOfMass(anchors);
        
        // Define some parameters
        int k = 0;              // number of iterations
        double mu_k;
        double tau = 0.001;     // threshold in position change between steps
        double rho = 0.05;
        Point2d r0 = new Point2d();
        
        iterLoop:
        do {
            // Calculate vector R(p_k) and Jacobian J(p_k):
            double[][] r = new double[anchors.length][1];
            double[][] j = new double[anchors.length][2];
            for (int i = 0; i < anchors.length; i++) {
                double dist = s0.distance(anchors[i]);
                if (dist == 0) {
                    break iterLoop;
                }
                j[i][0] = (s0.x - anchors[i].x) / dist;
                j[i][1] = (s0.y - anchors[i].y) / dist;
                r[i][0] = ranges[i] - dist;
            }
            
            // Build matrices out of arrays:
            Matrix mR = new Matrix(r);  // residual error vector
            Matrix mJ = new Matrix(j);  // estimate of the Jacobian
            Matrix mJT = mJ.transpose();
            
            // Update mu_k:
            Matrix tmp = mJT.times(mR);             // J^T * R
            mu_k = rho * Math.sqrt(tmp.cell(0, 0) * tmp.cell(0, 0) + tmp.cell(1, 0) * tmp.cell(1, 0));
            
            // Calculate Levenberg-Marquardt direction:
            Matrix tmp2 = mJT.times(mJ);            // J^T * J
            Matrix mI = new Matrix(tmp2.cols());    // will be 2x2 identity matrix
            mI = mI.times(mu_k);                    // mul with scalar mu_k
            Matrix deltaLM = tmp2.add(mI);
            deltaLM = deltaLM.inverse();            // (J^T * J + mu_k * I)^-1
            deltaLM = deltaLM.times(tmp);           // (J^T * J + mu_k * I)^-1 * J^T * R
            
            // Find sufficient decrease (Armijo rule):
            int t = 0;
            double alpha;
            Matrix mMF = mR.transpose().times(0.5).times(mR); // merit function
            while (true) {
                alpha = Math.pow(0.5, t);
                r0.x = s0.x + alpha * deltaLM.cell(0, 0);
                r0.y = s0.y + alpha * deltaLM.cell(1, 0);
                
                // Calculate left side of equation:
                r = new double[anchors.length][1];
                for (int i = 0; i < anchors.length; i++) {
                    r[i][0] = ranges[i] - r0.distance(anchors[i]);
                }
                mR = new Matrix(r);
                Matrix left = mR.transpose().times(0.5);
                left = left.times(mR);  // 1x1 matrix
                
                // Calculate right side of equation:
                Matrix right = tmp.transpose().times(0.0001 * alpha).times(deltaLM);
                right = mMF.add(right); // 1x1 matrix
                
                // Break loop if (left <= right) => alpha found
                if (left.cell(0, 0) <= right.cell(0, 0)) {
                    break;
                }
                t++;
            }
            
            // Update position if change is over threshold:
            if (s0.distance(r0) < tau) {
                break;
            }
            s0.x = r0.x;
            s0.y = r0.y;
            k++;
        } while (k <= 10);
        return s0;
    }

}
