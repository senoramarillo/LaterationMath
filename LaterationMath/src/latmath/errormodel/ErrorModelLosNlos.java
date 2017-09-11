package latmath.errormodel;

import latmath.distribution.ExponentialDistribution;
import latmath.distribution.NormalDistribution;

/**
 * Error model modeling LOS and NLOS errors using a Normal Distribution (LOS)
 * and an Exponential Distribution (NLOS).
 *
 * @version 1.11, 2011-12-09
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ErrorModelLosNlos extends BasicErrorModel {

    private double ua, ub;
    private NormalDistribution gaussian;
    private ExponentialDistribution exponential;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    /**
     * Error model used in Bahillo paper (RLSM). Use 30 m as default
     * maximum error = indoor radio range of nanopan transceiver.
     */
    public static final ErrorModelLosNlos EM_BAHILLO_PAPER =
            new ErrorModelLosNlos(30, 0, 2.3, 0, 3);

    /**
     * Creates a new instance of <code>ErrorModelLosNlos</code>.
     * 
     * @param maximumAllowedError The maximum allowed error.
     * @param mean The mean of the Normal Distribution (LOS).
     * @param sdev The standard deviation of the Normal Distribution (LOS).
     * @param ua The lower bound <code>a</code> of the uniform distribution
     *           the value Lambda (Exponential Distribution) is choosen from.
     * @param ub The upper bound <code>b</code> of the uniform distribution
     *           the value Lambda (Exponential Distribution) is choosen from.
     * @throws IllegalArgumentException If ua > ub.
     */
    public ErrorModelLosNlos(double maximumAllowedError, double mean, double sdev,
            double ua, double ub) {
        super(maximumAllowedError);
        if (ua > ub) {
            throw new IllegalArgumentException("ua > ub!");
        }
        this.ua = ua;
        this.ub = ub;
        gaussian = new NormalDistribution(mean, sdev);
        exponential = new ExponentialDistribution(1); // initialize with one for now
    }

    @Override
    public String getName() {
        return "LOS+NLOS";
    }

    @Override
    public double getOffset(double distance, double pNlos) {
        double error = 0;
        do {
            // get random sample from normal distribution (LOS error)
            double x = gaussian.sample();
            // remove negative values if not allowed
            double y = x;
            if (!negativeOffset && x < 0) {
                y = -x;
            }
            // simulate NLOS error with exponential distribution,
            // choose Lambda from interval U[a,b], if pNlos > 0
            if (pNlos > 0.0 && random.nextDouble() <= pNlos) {
                double l = ExponentialDistribution.nextUniform(random, ua, ub);
                // create new exponential distribution with mean = 1/Lambda
                exponential.setMean(1/l);
                // get random sample from distribution and add to LOS error
                double nlos = exponential.sample();
                y += nlos * bias;
            }
            // return error if below maximum error allowed
            error = y;
        } while (Math.abs(error) > maximumAllowedError);
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
                gaussian.getStandardDeviation() + "), lambda = U["+ua+","+ub+"],"
                + " Bias (NLOS)="+bias+"";
    }

    @Override
    public Object clone() {
        ErrorModelLosNlos clone = new ErrorModelLosNlos(maximumAllowedError,
                gaussian.getMean(), gaussian.getStandardDeviation(), ua, ub);
        clone.setNegativeOffsetsEnabled(this.isNegativeOffsetsEnabled());
        clone.setScaleFactor(this.getScaleFactor());
        return clone;
    }

}
