package latmath.algorithm;

import java.io.Serializable;
import latmath.errormodel.ErrorModel;
import latmath.util.Configurable;
import latmath.util.Point2d;

/**
 * Basic interface all lateration algorithms have to implement.
 *
 * @version 1.1, 2011-09-19
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public interface LaterationAlgorithm extends Cloneable, Configurable, Serializable {

    /**
     * Returns the name of the algorithm.
     * <p>
     * The name should not be too long, e.g. less than 20 chars.
     *
     * @return The name of the algorithm.
     */
    String getName();

    /**
     * Tests if this algorithm handles safe localization on its own.
     * <p>
     * Normally a call to the {@code localize()} method must use at least 3
     * anchors and ranges. If this method returns {@code true}, you can pass
     * in any number of anchors and ranges as the algorithms takes care of
     * the parameters coming in.
     * 
     * @return {@code true} if this algorithm handles safe localization on
     *         its own; {@code false} otherwise.
     */
    boolean canSafelyLocalize();
    
    /**
     * Runs the localization algorithm and returns the estimated position.
     * <p>
     * The size of the anchor and measured ranges array must be at least 3.
     * <p>
     * The actual position of the mobile node is passed to this method
     * to allow the development of some sort of "cheating algorithm" which does
     * some sort of optimum selection along a set of different algorithms.
     * <p>
     * The error model can be used by algorithms to determine the current
     * average and maximum distance measurement error.
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     * @param actualPosition The actual location of the mobile node to be located.
     * @param errorModel The current error model used for simulation or
     *                   <code>null</code> if running with real data (no error
     *                   model available).
     * @param width The width of the playing field used for simulation or
     *              <code>-1</code> if running with real data.
     * @param height The height of the playing field used for simulation or
     *              <code>-1</code> if running with real data.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    Point2d localize(Point2d[] anchors, double[] ranges, Point2d actualPosition,
            ErrorModel errorModel, int width, int height);
 
}
