package latmath.errormodel;

/**
 * Error model modeling no ranging error.
 *
 * @version 1.0, 2011-09-19
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ErrorModelNone extends BasicErrorModel {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance of <code>ErrorModelNone</code>.
     *
     * @param maximumAllowedError The maximum allowed error.
     */
    public ErrorModelNone(double maximumAllowedError) {
        super(maximumAllowedError);
    }

    @Override
    public String getName() {
        return "No Error";
    }

    @Override
    public double getOffset(double distance, double pNlos) {
        double error = 0;
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
        return getName();
    }

    @Override
    public Object clone() {
        ErrorModelNone clone = new ErrorModelNone(maximumAllowedError);
        clone.setNegativeOffsetsEnabled(this.isNegativeOffsetsEnabled());
        clone.setScaleFactor(this.getScaleFactor());
        return clone;
    }

}
