package latmath.errormodel;

import latmath.distribution.NormalDistribution;

/**
 * Error model modeling LOS and NLOS errors using two-mode Gaussian mixture
 * model (GMM).
 * <p>
 * In non-LOS situation, the measurement will get a positive bias <code>y</code>
 * and probably another (larger) variance <code>o</code>, N_nlos = N(y,o).
 *
 * @version 1.1, 2011-09-19
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ErrorModelLosNlosGMM extends BasicErrorModel {

    private NormalDistribution gaussianLOS;
    private NormalDistribution gaussianNLOS;

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance of <code>ErrorModelLosNlosGMM</code>.
     *
     * @param maximumAllowedError The maximum error allowed.
     * @param sdevLOS The standard deviation of the Normal Distribution (LOS).
     * @param meanNLOS The mean of the Normal Distribution (NLOS).
     * @param sdevNLOS The standard deviation of the Normal Distribution (NLOS).
     */
    public ErrorModelLosNlosGMM(double maximumAllowedError, double sdevLOS,
            double meanNLOS, double sdevNLOS) {
        super(maximumAllowedError);
        gaussianLOS = new NormalDistribution(0, sdevLOS);
        gaussianNLOS = new NormalDistribution(meanNLOS, sdevNLOS);
    }

    @Override
    public String getName() {
        return "LOS+NLOS-GMM";
    }

    @Override
    public double getOffset(double distance, double pNlos) {
        // get random sample form normal distributions
        double x1 = negativeOffset ? gaussianLOS.sample() : Math.abs(gaussianLOS.sample());
        double x2 = negativeOffset ? gaussianNLOS.sample() : Math.abs(gaussianNLOS.sample());
        // calculate error as y = (1-pNlos) * x1 + pNlos * x2
        double y = (1 - pNlos) * x1 + pNlos * x2;
        y *= bias;
        // return error, cut off at maximum error allowed
        double error = y > maximumAllowedError ?
            maximumAllowedError : y < -maximumAllowedError ?
                -maximumAllowedError : y;
        accountOffset(error);
        return error;
    }

    /**
     * Returns a string representation of this error model.
     *
     * @return A string representation of this error model.
     */
    @Override
    public String toString() {
        return getName() + " N_los(" + gaussianLOS.getMean() + "," +
                gaussianLOS.getStandardDeviation() + "), N_nlos(" +
                gaussianNLOS.getMean() + "," +
                gaussianNLOS.getStandardDeviation() + ")";
    }

    @Override
    public Object clone() {
        ErrorModelLosNlosGMM clone = new ErrorModelLosNlosGMM(maximumAllowedError,
                gaussianLOS.getStandardDeviation(), gaussianNLOS.getMean(),
                gaussianNLOS.getStandardDeviation());
        clone.setNegativeOffsetsEnabled(this.isNegativeOffsetsEnabled());
        clone.setScaleFactor(this.getScaleFactor());
        return clone;
    }
    
}
