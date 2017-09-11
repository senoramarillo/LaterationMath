package latmath.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

/**
 * A point and a range.
 * 
 * @version 1.0, 2012-02-29
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class PointAndRange implements Serializable, Comparable<PointAndRange> {

    public final Point2d point;

    public final double range;

    /**
     * Create a new PointAndRange from a Point2d and a range.
     */
    public PointAndRange(Point2d point, double range) {
        this.point = point;
	this.range = range;
    }

    /**
     * Create a new <code>Point2d</code> with x and y
     * coordinates set to the given values.
     *
     * @param x The value of the x coordinate.
     * @param y The value of the y coordinate.
     */
    public PointAndRange(double x, double y, double r) {
        this.point = new Point2d(x, y);
        this.range = r;
    }

    private static class RangeComparator implements Comparator<PointAndRange> {
        @Override
        public int compare(PointAndRange pr1, PointAndRange pr2) {
            return pr1.compareTo(pr2);
        }
    }

    public static final Comparator<PointAndRange> theRangeComparator =
        new RangeComparator();

    public static Comparator<PointAndRange> rangeComparator() {
        return PointAndRange.theRangeComparator;
    }

    private static class PointComparator implements Comparator<PointAndRange> {
        @Override
        public int compare(PointAndRange pr1, PointAndRange pr2) {
            int r = pr1.point.compareTo(pr2.point);
            if (r == 0)
                return Double.compare(pr1.range, pr2.range);
            else
                return r;
        }
    }

    public static final Comparator<PointAndRange> thePointComparator =
        new PointComparator();

    public static Comparator<PointAndRange> pointComparator() {
        return PointAndRange.thePointComparator;
    }

    @Override
    public boolean equals(Object pr) {
        if (pr instanceof PointAndRange) {
            PointAndRange _pr = (PointAndRange) pr;
            return this.point.equals(_pr.point) && this.range == _pr.range;
        } else
            return false;
     }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.point);
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.range) ^ (Double.doubleToLongBits(this.range) >>> 32));
        return hash;
    }

    /**
     * Compare two PointAndRanges.
     *
     * The default order is to order by range and then by the lexicographic
     * order on points.
     */
    @Override
    public int compareTo(PointAndRange other) {
            int r = Double.compare(this.range, other.range);
	    if (r != 0)
                return r;
            else
                return this.point.compareTo(other.point);
    }
}
