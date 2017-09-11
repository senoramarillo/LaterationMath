package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Residual brute force algorithm.
 *
 * @version 1.0, 2013-05-02
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class LikelihoodGamma extends BasicLaterationAlgorithm implements Releasable {

    private double shape = 3.3;
    private double rate = 0.576;
    private double offset = 3.31060119642765;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "LikelihoodGamma";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
                            Point2d actualPosition, ErrorModel errorModel,
                            int width, int height) {
        return multilaterate(anchors, ranges, actualPosition, this.shape, this.rate, this.offset);
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
     * @param actuak The actual position of the node.
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
                                        Point2d actual, double shape, double rate, double offset) {
        // step 0: sanity check
        if (anchors.length != ranges.length || actual == null) {
            return null;
        }

        MLEGamma.LikelihoodFunction likelihoodFunction =
            new MLEGamma.LikelihoodFunction(anchors, ranges, shape, rate, offset);
        double[] a = new double[] { actual.x, actual.y };
        double likelihood = likelihoodFunction.value(a);
        if (Double.isNaN(likelihood)) {
            System.out.println("Failed to compute likelihood at " + actual);
            return null;
        }
        if (likelihood <= 0.0) {
            System.out.println("Likelihood zero at " + actual);
            return null;
        }

        return actual;
    }

}
