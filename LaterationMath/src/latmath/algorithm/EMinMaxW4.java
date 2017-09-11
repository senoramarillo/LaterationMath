package latmath.algorithm;

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Extended Min-Max algorithm.
 * <p>
 * E-Min-Max is described in "Extended Min-Max Algorithm for Position
 * Estimation in Sensor Networks", Jorge Juan Robles , Javier Superva
 * Pola and Ralf Lehnert, 2012.
 *
 * @version 1.0, 2012-05-16
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class EMinMaxW4 extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "E-MIN-MAX (W4)";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges);
    }

    /**
     * Returns a string representation of this lateration algorithm.
     *
     * @return A string representation of this lateration algorithm.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Static call to this lateration algorithm.
     * <p>
     * This might be useful if one is only interested in the result and not
     * the additional features of the <code>BasicLaterationAlgorithm</code>
     * class.
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges) {
        // step 0: sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

	Point2d[] p = new Point2d[4];
	p[0] = new Point2d(anchors[0].x + ranges[0], anchors[0].y + ranges[0]);
	p[1] = new Point2d(anchors[0].x - ranges[0], anchors[0].y + ranges[0]);
	p[2] = new Point2d(anchors[0].x + ranges[0], anchors[0].y - ranges[0]);
	p[3] = new Point2d(anchors[0].x - ranges[0], anchors[0].y - ranges[0]);
	
        for (int i = 1; i < anchors.length; ++i) {
		p[0].set(Math.min(p[0].x, anchors[i].x + ranges[i]),
			 Math.min(p[0].y, anchors[i].y + ranges[i]));
		p[1].set(Math.max(p[1].x, anchors[i].x - ranges[i]),
			 Math.min(p[1].y, anchors[i].y + ranges[i]));
		p[2].set(Math.min(p[2].x, anchors[i].x + ranges[i]),
			 Math.max(p[2].y, anchors[i].y - ranges[i]));
		p[3].set(Math.max(p[3].x, anchors[i].x - ranges[i]),
			 Math.max(p[3].y, anchors[i].y - ranges[i]));
        }

	// use W-4 weight
	double[] weights = new double[4];
	for (int j = 0; j < 4; j++) {
		for (int i = 0; i < anchors.length; i++) {
			weights[j] += Math.abs((p[j].distance(anchors[i]) * p[j].distance(anchors[i])) - (ranges[i] * ranges[i])); // W-4 (best for internal zone)
		}
		weights[j] = 1.0 / weights[j];
	}

	return Point2d.centerOfMass(p, weights);
    }

}
