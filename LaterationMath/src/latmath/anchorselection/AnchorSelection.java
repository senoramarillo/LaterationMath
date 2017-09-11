package latmath.anchorselection;

import java.io.Serializable;
import latmath.algorithm.LaterationAlgorithm;
import latmath.location.filter.LocationFilter;
import latmath.util.Configurable;
import latmath.util.Point2d;

/**
 * Methods all anchor selection algorithms must implement.
 *
 * @version 1.0, 2012-02-07
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public interface AnchorSelection extends Cloneable, Configurable, Serializable {

    /**
     * Gets a readable name of the anchor selection algorithm for display reasons.
     *
     * @return A readable name of the anchor selection algorithm.
     */
    String getName();

    /**
     * Sets information to derive a unique log filename.
     * <p>
     * The file name can consist of the UID only or may be extended by the
     * algorithm and filter names.
     *
     * @param uid Unique ID for the log file.
     * @param algorithm Algorithm assigned with the UID.
     * @param filter Location filter assigned with the UID or {@code null} if
     *               no filter is set.
     */
    void deriveLogFilename(int uid, LaterationAlgorithm algorithm, LocationFilter filter);

    /**
     * Do anchor selection.
     * <p>
     * The algorithm may not alter the input arrays!
     *
     * @param anchors Available anchors.
     * @param measuredDistances Measured distances to anchors.
     * @param lastLocation The last calculated position or <code>null</code>
     *                     if not available.
     *
     * @return The selected anchors and distances for the lateration process.
     */
    AnchorSelectionResult select(Point2d[] anchors, double[] measuredDistances, Point2d lastLocation);

    /**
     * Do anchor selection.
     * <p>
     * The algorithm may not alter the input arrays!
     *
     * @param anchors Available anchors.
     * @param measuredDistances Measured distances to anchors.
     * @param lastLocation The last calculated position or <code>null</code>
     *                     if not available.
     * @param timestamp Timestamp in milliseconds.
     *
     * @return The selected anchors and distances for the lateration process.
     */
    AnchorSelectionResult select(Point2d[] anchors, double[] measuredDistances, Point2d lastLocation, long timestamp);

    /**
     * Resets the anchor selection to initial state.
     */
    void reset();

}
