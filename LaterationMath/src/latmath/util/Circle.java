package latmath.util;

import java.util.ArrayList;

/**
 * A simple circle math library.
 *
 * @version 1.0, 2011-08-02
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class Circle {

    /** private constructor */
    private Circle() {}

    /**
     * Returns the intersections of two circles.
     * 
     * @param p1 The center of the first circle.
     * @param r1 The radius of the first circle.
     * @param p2 The center of the second circle.
     * @param r2 The radius of the second circle.
     *
     * @return The intersections of the two circles or <code>null</code> if
     *         there is no (exact) intersection.
     */
    public static Point2d[] getIntersection(Point2d p1, double r1, Point2d p2, double r2) {
        
        double d = Math.hypot(p2.x - p1.x, p2.y - p1.y);

        // no solutions, the circles are separate || the circles are coincident
        // => infinite number of solutions possible
        if (r1+r2 < d || d == 0) {
            return null;
        }

        // no solutions because one circle is contained within the other
        if (Math.abs(r1-r2) > d) {
            return null;
        }

        double r1r1 = r1*r1;
        double a = (r1r1 - r2*r2 + d*d) / (2*d);
        // normally r1^2 should be always greater or equal than a^2, but
        // floating point calculations might make a^2 a tiny bit larger,
        // resulting in a negative sqrt => NaN, so take abs before!!!
        double h = Math.sqrt(Math.abs(r1r1 - a*a));

        double dx = (p2.x - p1.x) / d;
        double dy = (p2.y - p1.y) / d;
        double p3x = p1.x + a * dx;
        double p3y = p1.y + a * dy;

        dx *= h;
        dy *= h;
        double p4x = p3x + dy;
        double p4y = p3y - dx;

        Point2d[] res = new Point2d[p4x == p3x && p4y == p3y ? 1 : 2];

        res[0] = new Point2d(p4x, p4y);
        if (res.length == 2) {
            p4x = p3x - dy;
            p4y = p3y + dx;
            res[1] = new Point2d(p4x, p4y);
        }

        return res;
    }

    /**
     * Returns an approximated intersection of the two circles.
     * <p>
     * This method might be helpful if the circles are separate or contained
     * within the other. Returns the intersection of the two circles when
     * equally growing both circles till they intersect in one point.
     *
     * @param p1 The center of the first circle.
     * @param r1 The radius of the first circle.
     * @param p2 The center of the second circle.
     * @param r2 The radius of the second circle.
     *
     * @return An approximated intersection of the two circles or
     *         <code>null</code> if p1 equals p2.
     */
    public static Point2d getIntersectionApprox(Point2d p1, double r1, Point2d p2, double r2) {
        // calculate distance between center of circles
        double dist = p1.distance(p2);
        // if distance is zero => infinite number of solutions
        if (dist == 0) return null;

        // calculate intersection of line through center of circles
        // with both circles => four intersection points
        double dr1 = r1/dist;
        double dr2 = r2/dist;
        double dx = (p2.x - p1.x);
        double dy = (p2.y - p1.y);
        double dxp1 = dr1 * dx;
        double dyp1 = dr1 * dy;
        double dxp2 = dr2 * dx;
        double dyp2 = dr2 * dy;

        Point2d p11 = new Point2d(p1.x + dxp1, p1.y + dyp1);
        Point2d p12 = new Point2d(p1.x - dxp1, p1.y - dyp1);
        Point2d p21 = new Point2d(p2.x + dxp2, p2.y + dyp2);
        Point2d p22 = new Point2d(p2.x - dxp2, p2.y - dyp2);
        
        // find nearest pair of intersection points belonging
        // to different circles
        dist = p11.distance(p21);
        Point2d n1 = p11, n2 = p21;
        double dt = p11.distance(p22);
        if (dt < dist) {
            dist = dt;
            n2 = p22;
        }
        dt = p12.distance(p21);
        if (dt < dist) {
            dist = dt;
            n1 = p12;
            n2 = p21;
        }
        dt = p12.distance(p22);
        if (dt < dist) {
            n1 = p12;
            n2 = p22;
        }

        // return middle of line between two nearest points as result
        return new Point2d((n1.x + n2.x) / 2, (n1.y + n2.y) / 2);
    }

    /**
     * Returns approximated intersections of the two circles.
     *
     * @param p1 The center of the first circle.
     * @param r1 The radius of the first circle.
     * @param p2 The center of the second circle.
     * @param r2 The radius of the second circle.
     *
     * @return Approximated intersections of the two circles or
     *         <code>null</code> if p1 equals p2.
     */
    public static Point2d[] getIntersectionsApprox(Point2d p1, double r1, Point2d p2, double r2) {
        // calculate distance between center of circles
        double dist = p1.distance(p2);
        // if distance is zero => infinite number of solutions
        if (dist == 0) return null;

        // calculate intersection of line through center of circles
        // with both circles => four intersection points
        double dr1 = r1/dist;
        double dr2 = r2/dist;
        double dx = (p2.x - p1.x);
        double dy = (p2.y - p1.y);
        double dxp1 = dr1 * dx;
        double dyp1 = dr1 * dy;
        double dxp2 = dr2 * dx;
        double dyp2 = dr2 * dy;

        Point2d p11 = new Point2d(p1.x + dxp1, p1.y + dyp1);
        Point2d p12 = new Point2d(p1.x - dxp1, p1.y - dyp1);
        Point2d p21 = new Point2d(p2.x + dxp2, p2.y + dyp2);
        Point2d p22 = new Point2d(p2.x - dxp2, p2.y - dyp2);
        
        // find nearest pair of intersection points belonging
        // to different circles
        dist = p11.distance(p21);
        Point2d n1 = p11, n2 = p21;
        double dt = p11.distance(p22);
        if (dt < dist) {
            dist = dt;
            n2 = p22;
        }
        dt = p12.distance(p21);
        if (dt < dist) {
            dist = dt;
            n1 = p12;
            n2 = p21;
        }
        dt = p12.distance(p22);
        if (dt < dist) {
            n1 = p12;
            n2 = p22;
        }

        // return middle of line between two nearest points as result and
        // also the two points
        return new Point2d[] {
            new Point2d((n1.x + n2.x) / 2, (n1.y + n2.y) / 2), n1, n2
        };
    }
    
    /**
     * Returns an approximated intersection of the two circles as found in
     * paper "A Low-Complexity Geometric Bilateration Method for Localization
     * in Wireless Sensor Networks and Its Comparison with Least-Squares
     * Methods" (Figure 3).
     *
     * @param p1 The center of the first circle.
     * @param r1 The radius of the first circle.
     * @param p2 The center of the second circle.
     * @param r2 The radius of the second circle.
     *
     * @return An approximated intersection of the two circles or
     *         <code>null</code> if p1 equals p2.
     */
    public static Point2d getIntersectionApprox2(Point2d p1, double r1, Point2d p2, double r2) {
        // calculate distance between center of circles
        double dist = p1.distance(p2);
        // if distance is zero => infinite number of solutions
        if (dist == 0) {
            return null;
        }

        // take first circle and calculate new radius for this circle
        // as follows, same for second circle but alleviate because of
        // floating point calculations (use some small epsilon offset)
        // and special case of zero distance:
        if (r1 == 0) {
            r1 = 0.001;
        }
        if (r2 == 0) {
            r2 = 0.001;
        }
        double r1n = Math.abs(dist - r2) + 0.000001;
        double r2n = Math.abs(dist - r1) + 0.000001;
        
        // Use CCI procedure to calculate intersection points
        Point2d n1 = getIntersection(p1, r1n, p2, r2)[0];
        Point2d n2 = getIntersection(p1, r1, p2, r2n)[0];

        // return middle of line between points as result
        return new Point2d((n1.x + n2.x) / 2, (n1.y + n2.y) / 2);
    }
    
    /**
     * Returns the intersection of the given circle and line.
     * <p>
     * There might be two points, one point or no point.
     *
     * @param m The center of the circle.
     * @param r The radius of the circle.
     * @param p1 The first point defining the line.
     * @param p2 The second point defining the line.
     * 
     * @return The intersection of the given circle and line or
     *         <code>null</code> if there is no intersection.
     */
    public static Point2d[] getLineIntersection(Point2d m, double r, Point2d p1, Point2d p2) {
        double dx = (p2.x - p1.x);
        double dy = (p2.y - p1.y);
        double a = dx * dx + dy * dy;
        double b = 2 * (dx * (p1.x - m.x) + dy * (p1.y - m.y));
        double c = m.x * m.x + m.y * m.y + p1.x * p1.x + p1.y * p1.y
                - 2 * (m.x * p1.x + m.y * p1.y) - r * r;
        double disc = b * b - 4 * a * c;

        Point2d[] res = null;
        if (disc < 0) return res; // no intersection
        if (disc == 0) {
            double u = -b / (2 * a);
            res = new Point2d[1]; // tangent
            res[0] = new Point2d(p1.x + u * dx, p1.y + u * dy);
        } else {
            double u1 = (-b + Math.sqrt(disc)) / (2 * a);
            double u2 = (-b - Math.sqrt(disc)) / (2 * a);
            res = new Point2d[2]; // intersection
            res[0] = new Point2d(p1.x + u1 * dx, p1.y + u1 * dy);
            res[1] = new Point2d(p1.x + u2 * dx, p1.y + u2 * dy);
        }

        return res;
    }

    /**
     * Tests if a given point lies in all circles defined by <code>m</code>
     * and <code>r</code>.
     *
     * @param m The center of the circles.
     * @param r The radius of the circles.
     * @param p The point to be tested.
     *
     * @return <code>true</code> if the given point lies in all circles;
     *         <code>false</code> otherwise.
     */
    public static boolean pointInCircles(Point2d[] m, double[] r, Point2d p) {
        for (int i = 0; i < m.length; i++) {
            if (m[i].distance(p) > (r[i] + 0.01)) return false;
        }
        return true;
    }

    /**
     * Only keeps points which are contained in <code>min</code> circles or
     * points which are approximated.
     *
     * @param m The center of the circles.
     * @param r The radius of the circles.
     * @param pts The points to be tested.
     * @param min The number of minimum contained circles for a point.
     * @param useApprox Also test approximated points?
     * 
     * @return A filtered list of points.
     */
    public static IntersectionPoint2d[] minimumCircleContainment(Point2d[] m,
            double[] r, IntersectionPoint2d[] pts, int min, boolean useApprox) {
        int minApprox = Math.max(min - 2, 0);
        ArrayList<IntersectionPoint2d> v = new ArrayList<>();
        for (int i = 0; i < pts.length; i++) {
            int minC = 0;
            for (int j = 0; j < m.length; j++) {
                if (m[j].distance(pts[i]) <= r[j] + 0.01) {
                    minC++;
                }
            }
            if (pts[i].isRealIntersection()) {
                if (minC >= min) {
                    v.add(pts[i]);
                }
            } else if (!useApprox || minC >= minApprox) {
                v.add(pts[i]);
            }
        }
        return v.toArray(new IntersectionPoint2d[0]);
    }

}
