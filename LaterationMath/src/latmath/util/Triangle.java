package latmath.util;

/**
 * A simple triangle math library.
 *
 * @version 1.0, 2011-08-08
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class Triangle {

    /** private constructor */
    private Triangle() {}

    /**
     * Returns the perimeter of a triangle.
     * 
     * @param a Point A of the triangle.
     * @param b Point B of the triangle.
     * @param c Point C of the triangle.
     *
     * @return The perimeter of the triangle.
     */
    public static double perimeter(Point2d a, Point2d b, Point2d c) {
        return a.distance(b) + a.distance(c) + b.distance(c);
    }

    /**
     * Returns the area of a triangle.
     *
     * @param a Point A of the triangle.
     * @param b Point B of the triangle.
     * @param c Point C of the triangle.
     *
     * @return The area of the triangle.
     */
    public static double area(Point2d a, Point2d b, Point2d c) {
        // using Heron's formula
        double sa = b.distance(c);
        double sb = a.distance(c);
        double sc = a.distance(b);
        double s = 0.5 * (sa + sb + sc);
        double t = s * (s - sa) * (s - sb) * (s - sc);
        return Math.sqrt(t);
    }

    /**
     * Returns the minimum height of the triangle, which is min(h_a, h_b, h_c).
     *
     * @param a Point A of the triangle.
     * @param b Point B of the triangle.
     * @param c Point C of the triangle.
     *
     * @return The minimum height of the triangle.
     */
    public static double minHeight(Point2d a, Point2d b, Point2d c) {
        double twoA = 2 * area(a, b, c);
        double ha = twoA / b.distance(c);
        double hb = twoA / a.distance(c);
        double hc = twoA / a.distance(b);
        return Math.min(Math.min(ha, hb), hc);
    }

    /**
     * Returns the maximum height of the triangle, which is max(h_a, h_b, h_c).
     *
     * @param a Point A of the triangle.
     * @param b Point B of the triangle.
     * @param c Point C of the triangle.
     *
     * @return The maximum height of the triangle.
     */
    public static double maxHeight(Point2d a, Point2d b, Point2d c) {
        double twoA = 2 * area(a, b, c);
        double ha = twoA / b.distance(c);
        double hb = twoA / a.distance(c);
        double hc = twoA / a.distance(b);
        return Math.max(Math.max(ha, hb), hc);
    }

}
