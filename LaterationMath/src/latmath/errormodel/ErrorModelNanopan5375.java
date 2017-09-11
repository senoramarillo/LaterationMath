package latmath.errormodel;

/**
 * Error model modeling ranging error of Nanotron's Nanopan5375 radio chip.
 *
 * @version 1.12, 2012-06-22
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ErrorModelNanopan5375 extends BasicErrorModel {

    /**
     * Predefined error model for Nanopan5375 radio chip with default maximum
     * error of 30 m = expected indoor radio range.
     */
    public static final ErrorModelNanopan5375 EM_NANOPAN_5375
            = new ErrorModelNanopan5375(30);

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance of <code>ErrorModelNanopan5375</code>.
     *
     * @param maximumAllowedError The maximum allowed error.
     */
    public ErrorModelNanopan5375(double maximumAllowedError) {
        super(maximumAllowedError);
    }

    @Override
    public String getName() {
        return "Nanopan5375";
    }

    @Override
    public double getOffset(double distance, double pNlos) {
        double x = random.nextDouble() * random.nextDouble() * random.nextDouble();
        x = Math.log(x) / -0.5228819579;
        x -= 3.31060119642765;
        double y = x;
        if (!negativeOffset && x < 0) {
            y = -x;
        }
        y *= bias;
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
        return getName() + " [Bias="+bias+"]";
    }

    @Override
    public Object clone() {
        ErrorModelNanopan5375 clone = new ErrorModelNanopan5375(this.getMaximumAllowedError());
        clone.setNegativeOffsetsEnabled(this.isNegativeOffsetsEnabled());
        clone.setScaleFactor(this.getScaleFactor());
        return clone;
    }

}
