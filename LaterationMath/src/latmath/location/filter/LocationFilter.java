package latmath.location.filter;

import java.io.Serializable;
import latmath.util.Configurable;
import latmath.util.Point2d;

/**
 * Methods all location filters must implement.
 *
 * @version 1.0, 2012-01-29
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public interface LocationFilter extends Cloneable, Configurable, Serializable {

    /**
     * Gets a readable name of the location filter for display reasons.
     *
     * @return A readable name of the location filter.
     */
    String getName();

    /**
     * Adds a position to the location filter.
     * <p>
     * The position may be <code>null</code> if the lateration algorithm
     * couldn't calculate the position.
     *
     * @param location The position to add.
     * @param timestamp The timestamp of the position in milliseconds
     *                  or {@code -1} if no timestamp is available.
     */
    void add(Point2d location, long timestamp);

    /**
     * Gets the next position from the location filter.
     *
     * @return The next position from the location filter.
     */
    Point2d get();

    /**
     * Resets the filter to initial state.
     */
    void reset();

}
