package latmath.errormodel;

import latmath.distribution.ExponentialDistribution;

/**
 * Error model modeling LOS errors using a uniform distribution.
 *
 * @version 1.11, 2011-12-09
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ErrorModelUniform extends BasicErrorModel {

    private double ua, ub;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>ErrorModelUniform</code>.
     * 
     * @param maximumAllowedError The maximum allowed error.
     * @param ua The lower bound <code>a</code> of the uniform distribution.
     * @param ub The upper bound <code>b</code> of the uniform distribution.
     * @throws IllegalArgumentException If ua > ub.
     */
    public ErrorModelUniform(double maximumAllowedError, double ua, double ub) {
        super(maximumAllowedError);
        if (ua > ub) {
            throw new IllegalArgumentException("ua > ub!");
        }
        this.ua = ua;
        this.ub = ub;
    }

    @Override
    public String getName() {
        return "LOS-Uniform";
    }

    @Override
    public double getOffset(double distance, double pNlos) {
        // get random sample form interval U[a,b]
        double y = ExponentialDistribution.nextUniform(random, ua, ub);
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
        return getName() + " U["+ua+","+ub+"]";
    }

    @Override
    public Object clone() {
        ErrorModelUniform clone = new ErrorModelUniform(maximumAllowedError, ua, ub);
        clone.setNegativeOffsetsEnabled(this.isNegativeOffsetsEnabled());
        clone.setScaleFactor(this.getScaleFactor());
        return clone;
    }

}
