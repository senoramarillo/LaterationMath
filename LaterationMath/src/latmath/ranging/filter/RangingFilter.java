package latmath.ranging.filter;

import java.io.Serializable;
import latmath.util.Configurable;

/**
 * Methods all ranging filters must implement.
 *
 * @version 1.1, 2012-05-10
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public interface RangingFilter extends Cloneable, Configurable, Serializable {

    /**
     * Gets a readable name of the ranging filter for display reasons.
     *
     * @return A readable name of the ranging filter.
     */
    String getName();

    /**
     * Filter measured distances to anchors.
     * <p>
     * Entries in the measured distances might be {@code -1} if measurement to
     * the corresponding anchor failed!
     * <p>
     * The filter must also return {@code -1} if measurement stayed in failed
     * status!
     *
     * @param measuredDistances The measured distances to the anchor nodes in
     *                          meters or {@code -1} if measurement failed.
     * @param realDistances The real distances to the anchor nodes in meters or
     *                     {@code null} if not available.
     * @param timestamp Timestamp in milliseconds or {@code -1} if no timestamp
     *                  is available.
     *
     * @return The filtered distances to the anchor nodes.
     */
    double[] filter(double[] measuredDistances, double[] realDistances,
            long timestamp);

    /**
     * Resets the ranging to initial state.
     */
    void reset();
  
}
