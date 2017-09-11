package latmath.errormodel;

import latmath.distribution.ExponentialDistribution;
import latmath.distribution.NormalDistribution;

/**
 * Error model modeling LOS and NLOS errors using a Normal Distribution (LOS)
 * and an Uniform Distribution (NLOS).
 *
 * @version 1.11, 2011-12-09
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ErrorModelLosNlosUniform extends BasicErrorModel {

    private double ua, ub;
    private NormalDistribution gaussian;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    /**
     * Error model used in Nawaz paper (RML). Use 30 m as default
     * maximum error = indoor radio range of nanopan transceiver.
     */
    public static final ErrorModelLosNlosUniform EM_NAWAZ_PAPER =
            new ErrorModelLosNlosUniform(30, 0, 0.1, 0, 10);

    /**
     * Creates a new instance of <code>ErrorModelLosNlosUniform</code>.
     * 
     * @param maximumAllowedError The maximum allowed error.
     * @param mean The mean of the Normal Distribution (LOS).
     * @param sdev The standard deviation of the Normal Distribution (LOS).
     * @param ua The lower bound <code>a</code> of the uniform distribution (NLOS).
     * @param ub The upper bound <code>b</code> of the uniform distribution (NLOS).
     * @throws IllegalArgumentException If ua > ub.
     */
    public ErrorModelLosNlosUniform(double maximumAllowedError, double mean, double sdev,
            double ua, double ub) {
        super(maximumAllowedError);
        if (ua > ub) {
            throw new IllegalArgumentException("ua > ub!");
        }
        this.ua = ua;
        this.ub = ub;
        gaussian = new NormalDistribution(mean, sdev);
    }

    @Override
    public String getName() {
        return "LOS+NLOS-Uniform";
    }

    @Override
    public double getOffset(double distance, double pNlos) {
        // get random sample form normal distribution (LOS error)
        double x = gaussian.sample();
        // remove negative values if not allowed
        double y = x;
        if (!negativeOffset && x < 0) {
            y = -x;
        }
        // simulate NLOS error with uniform distribution, if pNlos > 0
        if (pNlos > 0.0 && random.nextDouble() <= pNlos) {
            // get random sample form distribution and add to LOS error
            y += ExponentialDistribution.nextUniform(random, ua, ub);
        }
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
        return getName() + " N(" + gaussian.getMean() + "," +
                gaussian.getStandardDeviation() + "), U["+ua+","+ub+"],"
                + " Bias="+bias+"";
    }

    @Override
    public Object clone() {
        ErrorModelLosNlosUniform clone = new ErrorModelLosNlosUniform(maximumAllowedError,
                gaussian.getMean(), gaussian.getStandardDeviation(), ua, ub);
        clone.setNegativeOffsetsEnabled(this.isNegativeOffsetsEnabled());
        clone.setScaleFactor(this.getScaleFactor());
        return clone;
    }

}
