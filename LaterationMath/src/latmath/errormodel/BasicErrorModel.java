package latmath.errormodel;

import java.awt.Frame;
import java.util.Random;

/**
 * Basic error model implementation.
 *
 * @version 1.01, 2011-12-09
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public abstract class BasicErrorModel implements ErrorModel {

    /** The average error so far */
    private transient double averageError;

    /** Offset call counter */
    private transient long offsetCalls;

    /** The maximum error so far */
    private transient double maximumError;

    /**
     * Random number generator (use single one for each error
     * model instead of Math.random() to avoid thread issues)
     */
    protected Random random;

    /** The maximum allowed error */
    protected double maximumAllowedError;

    /** Enable negative offsets? */
    protected boolean negativeOffset;

    /** The scale factor (bias) */
    protected double bias;

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance of <code>BasicErrorModel</code>.
     *
     * @param maximumAllowedError The maximum allowed error.
     */
    public BasicErrorModel(double maximumAllowedError) {
	this.random = new Random();
	this.maximumAllowedError = maximumAllowedError;
        this.maximumError = Double.MIN_VALUE;
        this.negativeOffset = false;
        this.bias = 1.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaximumAllowedError() {
        return maximumAllowedError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaximumAllowedError(double max) {
        maximumAllowedError = max;
    }

    @Override
    public boolean isNegativeOffsetsEnabled() {
        return negativeOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNegativeOffsetsEnabled(boolean b) {
        negativeOffset = b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAverageError() {
        return offsetCalls > 0 ? averageError/offsetCalls : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaximumError() {
        return maximumError != Double.MIN_VALUE ? maximumError : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getScaleFactor() {
        return bias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScaleFactor(double bias) {
        this.bias = bias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        offsetCalls = 0;
        averageError = 0;
        maximumError = Double.MIN_VALUE;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigurable() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(Frame parent) {
        return false;
    }

    /**
     * Must be called by each error model to account for average and maximum
     * error.
     *
     * @param offset The last offset value produced by the error model in meters.
     */
    protected void accountOffset(double offset) {
        offsetCalls++;
        offset = offset < 0 ? -offset : offset;
        averageError += offset;
        if (maximumError < offset) {
            maximumError = offset;
        }
    }

}
