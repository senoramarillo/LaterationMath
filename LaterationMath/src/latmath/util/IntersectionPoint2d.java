package latmath.util;

/**
 * A two dimensional point with double precision which represents the
 * intersection of two circles.
 *
 * @version 1.0, 2011-08-30
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class IntersectionPoint2d extends Point2d {

    /** Is the point derived by a real intersection or approximated */
    private boolean realIntersection;

    /** The weight of this intersection point, normally one */
    private double weight;

    /**
     * Create a new <code>IntersectionPoint2d</code> with x and y
     * coordinates set to the given values of the <code>Point2d</code>
     * and the realIntersection value set to <code>true</code>.
     *
     * @param p The value of the x and y coordinate.
     */
    public IntersectionPoint2d(Point2d p) {
        this(p.x, p.y, true);
    }

    /**
     * Create a new <code>IntersectionPoint2d</code> with x and y
     * coordinates set to the given values and the realIntersection
     * value set to <code>true</code>.
     *
     * @param x The value of the x coordinate.
     * @param y The value of the y coordinate.
     */
    public IntersectionPoint2d(double x, double y) {
        this(x, y, true);
    }

    /**
     * Create a new <code>IntersectionPoint2d</code>.
     *
     * @param x The value of the x coordinate.
     * @param y The value of the y coordinate.
     * @param realIntersection Whether this is a real intersection or an
     *                         approximated one.
     */
    public IntersectionPoint2d(double x, double y, boolean realIntersection) {
        super(x, y);
        this.realIntersection = realIntersection;
        this.weight = 1;
    }

    /**
     * Tests if this intersection point is derived from a real
     * circle intersection.
     *
     * @return {@code true} if this point is derived from a real
     *         circle intersection; {@code false} otherwise.
     */
    public boolean isRealIntersection() {
        return realIntersection;
    }

    /**
     * Set if this intersection point is derived from a real
     * circle intersection.
     *
     * @param realIntersection {@code true} if this point is derived from
     *                         a real circle intersection; {@code false}
     *                         otherwise.
     */
    public void setRealIntersection(boolean realIntersection) {
        this.realIntersection = realIntersection;
    }

    /**
     * Get the weight of this intersection point, normally one.
     *
     * @return The weight of this intersection point.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets the weight of this intersection point.
     *
     * @param weight The new weight of the intersection point.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Convert a normal <code>Point2d</code> to a <code>IntersectionPoint2d</code>.
     *
     * @param p The point to be converted.
     * @param weight The weight to be assigned to the point.
     *
     * @return A new <code>IntersectionPoint2d</code>.
     */
    public static IntersectionPoint2d convert(Point2d p, double weight) {
        return convert(p, true, weight);
    }

    /**
     * Convert a normal <code>Point2d</code> to a <code>IntersectionPoint2d</code>.
     *
     * @param p The point to be converted.
     * @param realIntersection {@code true} if this point is derived from
     *                         a real circle intersection; {@code false}
     *                         otherwise.
     * @param weight The weight to be assigned to the point.
     *
     * @return A new <code>IntersectionPoint2d</code>.
     */
    public static IntersectionPoint2d convert(Point2d p,
            boolean realIntersection, double weight) {
        IntersectionPoint2d tmp = new IntersectionPoint2d(p);
        tmp.setRealIntersection(realIntersection);
        tmp.setWeight(weight);
        return tmp;
    }

    /**
     * Convert an arrays of normal <code>Point2d</code>'s to an array of
     * <code>IntersectionPoint2d</code>'s.
     *
     * @param pa The point array to be converted.
     * @param weight The weight to be assigned to the points.
     *
     * @return A new array of <code>IntersectionPoint2d</code>'s.
     */
    public static IntersectionPoint2d[] convertA(Point2d[] pa, double weight) {
        if (pa == null) {
            return null;
        }
        IntersectionPoint2d[] tmp = new IntersectionPoint2d[pa.length];
        for (int i = 0; i < pa.length; i++) {
            tmp[i] = convert(pa[i], weight);
        }
        return tmp;
    }
    
}
