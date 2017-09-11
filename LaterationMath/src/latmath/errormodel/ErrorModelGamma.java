package latmath.errormodel;

import org.apache.commons.math3.distribution.GammaDistribution;


/**
 * Error model modeling ranging error of Nanotron's Nanopan5375 radio chip.
 *
 * @version 1.0, 2013-09-10
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class ErrorModelGamma extends BasicErrorModel {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    private GammaDistribution gammaDistribution;

    private double shape = 3.0;

    private double rate = 2.35;

    private double offset = 3.31060119642765;
    
    /**
     * Creates a new instance of <code>ErrorModelGamma</code>.
     *
     * @param maximumAllowedError The maximum allowed error.
     */
    public ErrorModelGamma(double shape, double rate, double offset,
                           double maximumAllowedError) {
        super(maximumAllowedError);
        this.shape = shape;
        this.rate = rate;
        this.offset = offset;

        this.gammaDistribution = new GammaDistribution(shape, 1.0 / rate);
    }

    @Override
    public String getName() {
        return "Gamma";
    }

    @Override
    public double getOffset(double distance, double pNlos) {
        double x = gammaDistribution.sample() - this.offset;
        double error = x > maximumAllowedError ?
            maximumAllowedError : x < -maximumAllowedError ?
                -maximumAllowedError : x;

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
        return getName() + " [Bias="+bias+"]";
    }

    @Override
    public Object clone() {
        ErrorModelGamma clone = new ErrorModelGamma(this.shape, this.rate, this.offset, this.getMaximumAllowedError());
        clone.setNegativeOffsetsEnabled(this.isNegativeOffsetsEnabled());
        clone.setScaleFactor(this.getScaleFactor());
        return clone;
    }

}
