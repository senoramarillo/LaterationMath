package latmath.anchorselection;

import latmath.util.Point2d;

/**
 * Anchor selection result, e.g. anchors and measured distances for lateration.
 *
 * @version 1.0, 2012-02-07
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class AnchorSelectionResult {

    /** Anchor locations */
    private Point2d[] anchors;

    /** Measured distances to anchors */
    private double[] measuredDistances;

    /**
     * Creates a new instance of <code>AnchorSelectionResult</code>.
     *
     * @param anchors Anchor locations.
     * @param measuredDistances Measured distances to anchors.
     */
    public AnchorSelectionResult(Point2d[] anchors, double[] measuredDistances) {
        this.anchors = anchors;
        this.measuredDistances = measuredDistances;
    }

    public Point2d[] getAnchors() {
        return anchors;
    }

    public double[] getMeasuredDistances() {
        return measuredDistances;
    }

    /**
     * Return a string representation of the anchor selection result.
     */
    @Override
    public String toString() {
	StringBuilder b = new StringBuilder();
	for (int i = 0; i < anchors.length; ++i) {
	    b.append(anchors[i].toString());
	    b.append(' ');
	    b.append(measuredDistances[i]);
	    if (i + 1 < anchors.length)
		b.append("; ");
	}
	return b.toString();
    }
}
