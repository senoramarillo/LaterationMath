package latmath.errormodel;

import java.io.Serializable;
import latmath.util.Configurable;

/**
 * Methods all distance based error models must implement.
 *
 * @version 1.1, 2011-09-19
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public interface ErrorModel extends Cloneable, Configurable, Serializable {

    /**
     * Returns the name of the error model.
     * <p>
     * The name should not be too long, e.g. less than 20 chars.
     *
     * @return The name of the error model.
     */
    String getName();

    /**
     * Clone this error model (e.g. for performance reasons).
     *
     * @return A copy of this error model or <code>null</code> if
     *         cloning is not supported by error model.
     */
    Object clone();

    /**
     * Gets the maximum error this model is allowed to produce.
     *
     * @return The maximum error this model is allowed to produce.
     */
    double getMaximumAllowedError();

    /**
     * Sets the maximum error this model is allowed to produce.
     * <p>
     * This might be useful to cut off too large values produces by the
     * error model, e.g. values larger than the radio range.
     *
     * @param max The new maximum error.
     */
    void setMaximumAllowedError(double max);

    /**
     * Tests if negative distance offsets are enabled.
     * 
     * @return {@code true} if negative distance offsets are enabled;
     *         {@code false} otherwise.
     */
    boolean isNegativeOffsetsEnabled();

    /**
     * Enables or disables negative distance offsets.
     * <p>
     * Note that the error model still must support negative offsets,
     * not all do.
     *
     * @param b {@code true} if negative distance offsets should be used;
     *          {@code false} otherwise.
     */
    void setNegativeOffsetsEnabled(boolean b);

    /**
     * Get the scale factor (bias).
     *
     * @return The scale factor (bias).
     */
    double getScaleFactor();

    /**
     * Set the scale factor (bias).
     * <p>
     * Default value is <code>1</code>.
     *
     * @param bias The new bias to set.
     */
    void setScaleFactor(double bias);

    /**
     * Gets the distance offset in meters from this error model.
     * <p>
     * The real distance between reference node and mobile node might be used
     * by a model to model some sort of NLOS probability if second parameter
     * is not used by the model.
     *
     * @param distance The real distance between reference node and mobile node
     *                 corresponding to the current measurement. Will be ignored
     *                 by most error models.
     * @param pNlos The NLOS probability in range (0,1).
     *
     * @return The distance offset in meters.
     */
    double getOffset(double distance, double pNlos);

    /**
     * Gets the average error this model produced until this point in time.
     *
     * @return The average error this model produced until this point in time.
     */
    double getAverageError();

    /**
     * Gets the maximum error this model produced until this point in time.
     *
     * @return The maximum error this model produced until this point in time.
     */
    double getMaximumError();

    /**
     * Reset the error model to initial state (statistics).
     */
    void reset();

}
