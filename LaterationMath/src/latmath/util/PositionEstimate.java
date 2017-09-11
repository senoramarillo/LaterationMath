package latmath.util;

/**
 * A position estimate calculated by some lateration algorithms. Contains the
 * estimated position of the node being localized and the residual error which
 * is the sum of the squared residuals to each anchor node.
 *
 * @version 1.0, 2011-07-26
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class PositionEstimate {

    private Point2d location;
    private double residualError;

    /**
     * Creates a new instance of <code>PositionEstimate</code>.
     *
     * @param location The estimated position of the node being localized.
     * @param residualError The residual error.
     */
    public PositionEstimate(Point2d location, double residualError) {
        this.location = location;
        this.residualError = residualError;
    }

    /**
     * Get the estimated position of the node.
     *
     * @return The estimated position of the node.
     */
    public Point2d getLocation() {
        return location;
    }

    /**
     * Get the residual error associated with the estimated position.
     *
     * @return The residual error associated with the estimated position.
     */
    public double getResidualError() {
        return residualError;
    }

    /**
     * Set the residual error associated with the estimated position.
     *
     * @param residualError The new residual error.
     */
    public void setResidualError(double residualError) {
        this.residualError = residualError;
    }

    /**
     * Calculate residual error which is the sum of the squared residuals
     * to each anchor node.
     *
     * @param anchors The anchor nodes.
     * @param ranges The measured distances.
     * @param e The estimated location of the mobile node.
     *
     * @return The residual error.
     */
    public static double calculateResidualError(Point2d[] anchors, double[] ranges, Point2d e) {
        double error = 0;
        for (int i = 0; i < anchors.length; i++) {
            double residual = anchors[i].distance(e) - ranges[i];
            error += residual * residual;
        }
        return error;
    }

    /**
     * Calculate residual error which is the sum of the squared residuals
     * to each anchor node.
     *
     * @param anchors The anchor nodes.
     * @param ranges The measured distances.
     * @param weights The anchor weights.
     * @param e The estimated location of the mobile node.
     *
     * @return The residual error.
     */
    public static double calculateResidualError(Point2d[] anchors, double[] ranges, double[] weights, Point2d e) {
        double error = 0;
        for (int i = 0; i < anchors.length; i++) {
            double residual = anchors[i].distance(e) - ranges[i];
            error += weights[i] * residual * residual;
        }
        return error;
    }

}
