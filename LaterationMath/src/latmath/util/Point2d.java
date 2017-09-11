package latmath.util;

import java.io.Serializable;

/**
 * A two dimensional point with double precision.
 * 
 * @version 1.0, 2011-07-26
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class Point2d implements Serializable, Comparable<Point2d> {

    /** v1.0 serialVersionUID */
    private static final long serialVersionUID = 8951964407763709727L;

    /** The x coordinate in R^2 */
    public double x;

    /** The y coordinate in R^2 */
    public double y;

    /**
     * Create a new <code>Point2d</code> with x and y
     * coordinates set to zero.
     */
    public Point2d() {
        this(0, 0);
    }

    /**
     * Create a new <code>Point2d</code> with x and y
     * coordinates set to the given values.
     *
     * @param x The value of the x coordinate.
     * @param y The value of the y coordinate.
     */
    public Point2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Get the value of the x coordinate.
     *
     * @return The value of the x coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * Get the value of the y coordinate.
     *
     * @return The value of the y coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * Set the value of the x and y coordinates to a new value.
     * 
     * @param x The new value of the x coordinate.
     * @param y The new value of the y coordinate.
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns a string representation of this Point2d.
     *
     * @return A string representation of this Point2d.
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof Point2d) {
            Point2d tmp = (Point2d) other;
            return this.x == tmp.x && this.y == tmp.y;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        long bitsX = Double.doubleToLongBits(this.x);
        long bitsY = Double.doubleToLongBits(this.y);
        long code = bitsX ^ (bitsX >>> 48) ^ (bitsY >>> 16) ^ (bitsY >>> 32);
	return (int) code;
    }

    @Override
    public int compareTo(Point2d other) {
        if (this.x < other.x) {
	    return -1;
        } else if (this.x > other.x) {
            return 1;
        } else if (this.y < other.y) {
            return -1;
        } else if (this.y > other.y) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Calculates the euclidean distance between this point and point
     * <code>p</code>.
     *
     * @param p A point.
     *
     * @return The euclidean distance between this point and point <code>p</code>.
     */
    public final double distance(Point2d p) {
        double dx, dy;
        dx = this.x - p.x;
        dy = this.y - p.y;
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    /**
     * Calculates the squared euclidean distance between this point and point
     * <code>p</code>.
     *
     * @param p A point.
     *
     * @return The squared euclidean distance between this point and point <code>p</code>.
     */
    public final double distanceSquared(Point2d p) {
        double dx, dy;
        dx = this.x - p.x;
        dy = this.y - p.y;
        return dx*dx + dy*dy;
    }

    /**
     * Returns the center of mass of a given set of points.
     * 
     * @param pts The points, each with equal mass.
     *
     * @return The center of mass of the given points or <code>null</code>
     *         if the given array of points is empty.
     */
    public static Point2d centerOfMass(Point2d[] pts) {
        double[] mass = new double[pts.length];
        ArrayUtils.fill(mass, 1);
        return centerOfMass(pts, mass);
    }

    /**
     * Returns the center of mass of a given set of points.
     *
     * @param pts The points.
     * @param mass The mass of each point.
     *
     * @return The center of mass of the given points or <code>null</code>
     *         if the given array of points is empty.
     */
    public static Point2d centerOfMass(Point2d[] pts, double[] mass) {
        if (pts.length == 0) {
            return null;
        }
        double M = 0;
        double x = 0, y = 0;
        for (int i = 0; i < pts.length; i++) {
            x += pts[i].x * mass[i];
            y += pts[i].y * mass[i];
            M += mass[i];
        }
        return new Point2d(x/M, y/M);
    }

    /**
     * Calculate the geometric median of a discrete set of sample points.
     * <p>
     * The geometric median is the point minimizing the sum of distances to the
     * sample points. It is also known as the Fermat–Weber point or 1-median.
     * <p>
     * This method calculates an approximation to the geometric median using
     * Weiszfeld's algorithm.
     *
     * @param pts The set of sample points.
     *
     * @return The geometric median of a discrete set of sample points.
     */
    public static Point2d geometricMedian(Point2d[] pts) {
        double[] weights = new double[pts.length];
        ArrayUtils.fill(weights, 1);
        return geometricMedian(pts, weights);
    }

    /**
     * Calculate the geometric median of a discrete set of sample points.
     * <p>
     * The geometric median is the point minimizing the sum of distances to the
     * sample points. It is also known as the Fermat–Weber point or 1-median.
     * <p>
     * This method calculates an approximation to the geometric median using
     * Weiszfeld's algorithm.
     *
     * @param pts The set of sample points.
     * @param weights The weight of each point.
     *
     * @return The geometric median of a discrete set of sample points.
     */
    public static Point2d geometricMedian(Point2d[] pts, double[] weights) {
        // Step 1:
        for (int i = 0; i < pts.length; i++) {
            int result = weiszfeldTestOptimum(pts, weights, i);
            if (result != -1) {
                return pts[i];
            }
        }

        // Step 2:
        Point2d x = centerOfMass(pts, weights);

        // Step 3+4:
        double e0, e1;
        double epsilon = 0.000001;
        double hyperbolaE = 0.001;
        Point2d xNew = new Point2d();
        int iterations = 0;

        do {
            double xt = 0;
            double yt = 0;
            double id = 0;
            for (int i = 0; i < pts.length; i++) {
                double dist = x.distance(pts[i]);
                xt += weights[i] * (pts[i].x / dist);
                yt += weights[i] * (pts[i].y / dist);
                id += weights[i] * (1 / dist);
            }
            xNew.x = xt / id;
            xNew.y = yt / id;

            e0 = weiszfeldDistanceSum(pts, x, hyperbolaE);
            e1 = weiszfeldDistanceSum(pts, xNew, hyperbolaE);
            if (e1 >= e0) break;
            if (((e0 - e1) / e0) < epsilon) break;

            x.x = xNew.x;
            x.y = xNew.y;
            iterations++;

        } while (iterations <= 100);

        return x;
    }

    private static double weiszfeldDistanceSum(Point2d[] pts, Point2d x, double epsilon) {
        double sum = 0;
        for (int i = 0; i < pts.length; i++) {
            sum += Math.sqrt((x.x - pts[i].x) * (x.x - pts[i].x) + (x.y - pts[i].y) * (x.y - pts[i].y) + epsilon);
        }
        return sum;
    }

    private static int weiszfeldTestOptimum(Point2d[] pts, double[] weights, int i) {
        double result;
        double sumX = 0;
        double sumY = 0;
        for (int m = 0; m < pts.length; m++) {
            if (m != i) {
                double dist = pts[i].distance(pts[m]);
                // hack: test if distance is zero => same point (continue)
                if (dist == 0) {
                    continue;
                }
                sumX += weights[m] * ((pts[i].x - pts[m].x) / dist);
                sumY += weights[m] * ((pts[i].y - pts[m].y) / dist);
            }
        }
        result = Math.sqrt((sumX * sumX) + (sumY * sumY));
        return (result <= weights[i]) ? i : -1;
    }
    
}
