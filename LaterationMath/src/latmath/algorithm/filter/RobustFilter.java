package latmath.algorithm.filter;

import java.util.LinkedList;
import java.util.List;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.QuickSelect;

/**
 * Robust median filter. Removes all intermediate position estimates x_j that
 * are separated from more than a half of the other intermediate positions x_k
 * (j != k) more than two times the median.
 * <p>
 * The filter is described in "Hybrid RSS-RTT Localization Scheme for Indoor
 * Wireless Networks", A. Bahillo, S.Mazuelas, R. M. Lorenzo, P. Fernandez,
 * J. Prieto, R. J. Duran and E. J. Abril, 2010.
 * 
 * @version 1.0, 2011-08-03
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class RobustFilter {

    private Point2d[] pts;

    /**
     * Creates a new instance of <code>RobustFilter</code>.
     *
     * @param pts The points to be filtered.
     */
    public RobustFilter(Point2d[] pts) {
        this.pts = pts;
    }

    /**
     * Runs the filter procedure and returns the remaining "filtered" points.
     *
     * @return The remaining "filtered" points.
     */
    public Point2d[] filter() {
        int N = LMath.binom(pts.length, 2);
        double[] v = new double[N];
        Double[] vC = new Double[N];
        for (int i = 0; i < pts.length-1; i++) {
            for (int j = i+1; j < pts.length; j++) {
                int idx = toIndex(i, j, pts.length);
                v[idx] = pts[i].distance(pts[j]);
                vC[idx] = v[idx];
            }
        }

        double MEDV = 2 * (Double) QuickSelect.select(vC, vC.length/2+1);
        List<Point2d> filtered = new LinkedList<>();

        for (int i = 0; i < pts.length; i++) {
            int dropCounter = 0;
            for (int j = 0; j < pts.length; j++) {
                if (i == j) {
                    continue;
                }
                double dist = v[toIndex(i, j, pts.length)];
                if (dist >= MEDV) {
                    dropCounter++;
                }
            }
            if (dropCounter <= pts.length/2) {
                filtered.add(pts[i]);
            }
        }

        return filtered.toArray(new Point2d[0]);
    }

    private static int toIndex(int row, int col, int N) {
        int idx = -1;
        if (row < col) {
            idx = row * (N-1) - (row-1) * ((row-1) + 1)/2 + col - row - 1;
        } else if (col < row) {
            idx = col * (N-1) - (col-1) * ((col-1) + 1)/2 + row - col - 1;
        }
        return idx;
    }
    
}