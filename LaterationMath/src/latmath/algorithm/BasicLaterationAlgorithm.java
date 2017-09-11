package latmath.algorithm;

import java.awt.Frame;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.PreciseStandardDeviation;

/**
 * Basic lateration algorithm implementation.
 * <p>
 * Offers evaluation method for localization accuracy and some statistical
 * metrics to compare different algorithms.
 *
 * @version 1.4, 2013-07-02
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public abstract class BasicLaterationAlgorithm implements LaterationAlgorithm {

    /** The number of runs (calls to evaluate() method) */
    private transient int n;

    /** Number of failed localizations */
    private transient int localizationFailCount;

    /** The absolute error for calculating MAE */
    private transient double absoluteError;

    /** The maximum error */
    private transient double maximumError;

    /** The square error for calculating MSE */
    private transient double squareError;

    /** The square error for MMSE for calculating MSE */
    private transient double squareErrorMMSE;
    private transient double maxSquareErrorMMSE; // internal only

    /** For securely calculating standard deviation */
    private transient PreciseStandardDeviation sdev;
    
    /** Estimated positions (for later evaluation, e.g. painting or logging) */
    private transient List<Point2d> estimatedPositions;
    
    /** Bias of estimator (e.g., if repeatedly positioning at same location) */
    private transient int estimationBiasCnt;
    private transient double estimationBiasX;
    private transient double estimationBiasY;
        
    /** Runtime counter for this algorithm */
    private transient long runtime;

    /** MAE per anchor count data */
    private transient Hashtable<Integer, MAEInfo> maeV;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    private class MAEInfo {
        private int count = 0;
        private double error = 0;
        public void add(double e) {
            error += e;
            count++;
        }
        public double get() {
            return count > 0 ? error / count : 0;
        }
    }

    public BasicLaterationAlgorithm() {
        init();
    }
    
    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();
            init();
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private void init() {
        maeV = new Hashtable<>();
        sdev = new PreciseStandardDeviation();
        estimatedPositions = new ArrayList<>();
        reinit();
    }

    protected void reinit() {
        n = localizationFailCount = 0;
        maximumError = maxSquareErrorMMSE = Double.MIN_VALUE;
        absoluteError = squareError = squareErrorMMSE = 0;
        estimationBiasCnt = 0;
        estimationBiasX = estimationBiasY = 0;
        runtime = 0;
        maeV.clear();
        sdev.reset();
        estimatedPositions.clear();
    }

    /**
     * By default no algorithm can safely localize.
     * <p>
     * Therefore call safeLocalize() for this task which uses default
     * guarding code.
     * <p>
     * Override this method for your algorithm if neccessary.
     * 
     * @return Always {@code false}.
     */
    @Override
    public boolean canSafelyLocalize() {
        return false;
    }
    
    /**
     * Safely runs the localization algorithm and returns the estimated position.
     * <p>
     * The anchor and range array are checked before executing the localization
     * algorithm so that they can be passed to any algorithm (e.g. checks if the
     * array size is greater or equal to 3 and if sizes are equal).
     * <p>
     * The actual position of the mobile node is passed to this method
     * to allow the development of some sort of "cheating algorithm" which does
     * some sort of optimum selection along a set of different algorithms.
     * <p>
     * The error model can be used by algorithms to determine the current
     * average and maximum distance measurement error.
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     * @param actualPosition The actual location of the mobile node to be located.
     * @param errorModel The current error model used for simulation or
     *                   <code>null</code> if running with real data (no error
     *                   model available).
     * @param width The width of the playing field used for simulation or
     *              <code>-1</code> if running with real data.
     * @param height The height of the playing field used for simulation or
     *              <code>-1</code> if running with real data.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public Point2d safeLocalize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        if (anchors != null && ranges != null && anchors.length == ranges.length
                && anchors.length > 2) {
            return localize(anchors, ranges, actualPosition, errorModel, width,
                    height);
        }
        return null;
    }
    
    /**
     * Evaluates the accuracy of the localization algorithm.
     * <p>
     * Also updates different accuracy metrics like mean absolute error (MAE),
     * maximum error, root mean square error (RMSE) and the localization fail
     * count.
     *
     * @param anchors The anchor/reference node locations.
     * @param ranges The measured distances to the anchor/reference nodes.
     * @param estimatedPosition The estimated position of the mobile node or
     *                          <code>null</code> if position unknown (e.g.
     *                          calculation failed).
     * @param actualPosition The actual position of the mobile node or
     *                       <code>null</code> if position unknown.
     * 
     * @return The distance between estimated and real position in meters
     *         or <code>-1</code> on error.
     */
    public double evaluate(Point2d[] anchors, double[] ranges,
            Point2d estimatedPosition, Point2d actualPosition) {
        double tmpError;
        double tmpError2;
        
        // also add null's to list (to reflect failed calculations
        // and to stay in sync with actual position list)
        estimatedPositions.add(estimatedPosition);
            
        // account for failed localizations
        if (estimatedPosition == null) {
            localizationFailCount++;
            return -1;
        }
        // in simulation this won't happen, but maybe in real world
        // deployments because actual position isn't available
        if (actualPosition == null) {
            return -1;
        }
        
        // if estimated position is available, then calculate errors
        tmpError = actualPosition.distance(estimatedPosition);
        tmpError2 = PositionEstimate.calculateResidualError(anchors,
                ranges, estimatedPosition) / anchors.length;
        if (tmpError > maximumError) {
            maximumError = tmpError;
        }
        if (tmpError2 > maxSquareErrorMMSE) {
            maxSquareErrorMMSE = tmpError2;
        }
        
        // update MAE per anchor count
        int c = anchors.length;
        MAEInfo maeI = maeV.get(c);
        if (maeI == null) {
            maeI = new MAEInfo();
            maeV.put(c, maeI);
        }
        maeI.add(tmpError);

        // update accuracy metric counters
        absoluteError += tmpError;
        squareError += (tmpError * tmpError);
        squareErrorMMSE += tmpError2;
        estimationBiasCnt++;
        estimationBiasX += estimatedPosition.x - actualPosition.x;
        estimationBiasY += estimatedPosition.y - actualPosition.y;
        sdev.addSample(tmpError);
        n++;
        return tmpError;
    }

    /**
     * Get a list with all estimated positions in order of evaluate() calls.
     * 
     * @return A list with all estimated positions including {@code null}'s
     *         for failed calculations.
     */
    public List<Point2d> getEstimatedPositions() {
        return estimatedPositions;
    }
    
    /**
     * Returns the number of failed localizations, e.g. estimated position
     * was <code>null</code>.
     *
     * @return The number of failed localizations.
     */
    public int getLocalizationFailCount() {
        return localizationFailCount;
    }

    /**
     * Returns the mean absolute error (MAE) also known as the
     * average positional error.
     *
     * @return The mean absolute error (MAE).
     */
    public double getMeanAbsoluteError() {
        return n > 0 ? absoluteError/n : 0;
    }

    /**
     * Returns the maximum error.
     *
     * @return The maximum error.
     */
    public double getMaximumError() {
        return maximumError != Double.MIN_VALUE ? maximumError : 0;
    }

    /**
     * Returns the standard deviation (SDEV).
     *
     * @return The standard deviation (SDEV).
     */
    public double getStandardDeviation() {
        return sdev.standardDeviation();
    }

    /**
     * Returns the mean square error (MSE). MSE measures the average of the
     * squares of the "errors". The error is the amount by which the value
     * implied by the estimator differs from the quantity to be estimated.
     *
     * @return The mean square error (MSE).
     */
    public double getMeanSquareError() {
        return n > 0 ? squareError/n : 0;
    }

    /**
     * Returns the root mean square error (RMSE), a commonly used calculation
     * to measure the difference (or residual) between predicted and observed
     * values.
     *
     * @return The root mean square error (RMSE).
     */
    public double getRootMeanSquareError() {
        return n > 0 ? Math.sqrt(squareError/n) : 0;
    }

    /**
     * Returns the mean square error (MSE) of the distance measurements
     * based on the estimated location. Useful for all Minimum Mean Square
     * Estimation (MMSE) methods because they estimate a sensor node’s location
     * by (approximately) minimizing this mean square error.
     *
     * @return The mean square error (MSE) for MMSE.
     */
    public double getMeanSquareErrorMMSE() {
        return n > 0 ? squareErrorMMSE/n : 0;
    }

    /**
     * Returns the root mean square error (RMSE) of the distance measurements
     * based on the estimated location. Useful for all Minimum Mean Square
     * Estimation (MMSE) methods because they estimate a sensor node’s location
     * by (approximately) minimizing the mean square error.
     *
     * @return The root mean square error (RMSE) for MMSE.
     */
    public double getRootMeanSquareErrorMMSE() {
        return n > 0 ? Math.sqrt(squareErrorMMSE/n) : 0;
    }

    /**
     * Returns the estimation bias of this algorithm which
     * is {@code 0} if no calculation has been performed.
     * 
     * @return The estimation bias of this algorithm.
     */
    public double getEstimationBias() {
        if (estimationBiasCnt > 0) {
            double dx = estimationBiasX / estimationBiasCnt;
            double dy = estimationBiasY / estimationBiasCnt;
            return Math.sqrt(dx * dx + dy * dy);
        }
        return 0;
    }
    
    /**
     * Returns the position errors.
     * 
     * @return The position errors.
     */
    public Double[] getPositionErrors() {
        return sdev.getSamples().toArray(new Double[0]);
    }
    
    /**
     * Returns the position errors as primitive double type.
     *
     * @return The position errors.
     */
    public double[] getHistogramData() {
        ArrayList<Double> histogram = sdev.getSamples();
        double[] da = new double[histogram.size()];
        for (int i = 0; i < da.length; i++) {
            da[i] = histogram.get(i);
        }
        return da;
    }

    /**
     * Gets the MAE per anchor where the index represents the amount of anchors.
     * 
     * @return The MAE per anchor.
     */
    public double[] getMeanAbsoluteErrorPerAnchor() {
        int maxKey = -1;
        Integer[] keys = maeV.keySet().toArray(new Integer[0]);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] > maxKey) {
                maxKey = keys[i];
            }
        }
        double[] values = new double[maxKey+1];
        for (int i = 0; i < values.length; i++) {
            values[i] = maeV.get(i) != null ? maeV.get(i).get() : 0;
        }
        return values;
    }

    /**
     * Returns the average runtime of this algorithm.
     *
     * @return The average runtime of this algorithm in nanoseconds.
     */
    public long getAverageRuntime() {
        return n > 0 ? runtime/n : 0;
    }

    /**
     * Adds the given runtime to the accumulated runtime.
     * 
     * @param runtime The runtime of the last run in nanoseconds.
     */
    public void addRuntime(long runtime) {
        this.runtime += runtime;
    }

    /**
     * Resets all statistical metrics, counters and collected data.
     */
    public void reset() {
        reinit();
    }
    
    /**
     * Reset bias only.
     */
    public void resetEstimationBias() {
        estimationBiasCnt = 0;
        estimationBiasX = 0;
        estimationBiasY = 0;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public boolean configure(Frame parent) {
        return false;
    }

}
