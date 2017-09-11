package latmath.algorithm;

import java.util.Arrays;
import latmath.errormodel.ErrorModel;
import latmath.util.ArrayUtils;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;

/**
 * Least Median of Squares (LMS).
 * <p>
 * LMS is described in "Robust Statistical Methods for Securing Wireless
 * Localization in Sensor Networks", Zang Li, Wade Trappe, Yanyong Zhang,
 * Badri Nath, 2005.
 *
 * @version 1.0, 2011-08-03
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class LeastMedianOfSquares extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "LMS";
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
        // 1. Set number of subsets, subset size and threshold
        int k = 4;
        int N = anchors.length;
        int M = N > 6 ? 20 : LMath.binom(N, k);
        double threshold = 2.5;

        if (N < k) {
            double[] weights = new double[N];
            ArrayUtils.fill(weights, 1);
            PositionEstimate pe = LinearLeastSquares.multilaterate(anchors,
                    ranges, weights);
            return pe != null ? pe.getLocation() : null;
        }

        // 2. Randomly draw M k-permutations
        int rpi = 0;
        int[] rand = new int[M];
        int binom = LMath.binom(N, k);
        int[] permutations = new int[k];
        int[][] randPermutations = new int[M][k];

        if (binom <= M) {
            // select all available permutations
            for (int i = 0; i < M; i++) {
                rand[i] = i;
            }
        } else {
            // select M permutations randomly
            for (int i = 0; i < M; i++) {
                rand[i] = -1;
                do {
                    int j;
                    int rnd = (int) (Math.random() * (binom - 1));
                    for (j = 0; j < i; j++) {
                        if (rand[j] == rnd) break;
                    }
                    if (j == i) {
                        rand[i] = rnd;
                    }
                } while (rand[i] == -1);
            }
            Arrays.sort(rand);
        }

        // initialisation for calculating k-permutations
        for (int i = 0; i < permutations.length; i++) {
            permutations[i] = i;
        }

        // build all k-permutations
        for (int i = 0; i < binom; i++) {

            // add current permutation if choosen
            if (i == rand[rpi]) {
                System.arraycopy(permutations, 0, randPermutations[rpi], 0, k);
                rpi++;
                if (rpi == M) break;
            }

            // build next permutation
            if (i == binom - 1) break;
            int j = k - 1;
            while (j >= 0) {
                if (!LMath.incCounter(permutations, j, N, k)) break;
                j--;
            }
            for (int l = j+1; l < k; l++) {
                permutations[l] = permutations[l-1] + 1;
            }
        }
        
        // calculate intermediate position and median of residues
        Point2d[] iPos = new Point2d[randPermutations.length];
        double[] medians = new double[randPermutations.length];
        double[] weights = new double[k];
        ArrayUtils.fill(weights, 1);
        Point2d[] tmpAnchors = new Point2d[k];
        double[] tmpRanges = new double[k];
        double[] tmpMedian = new double[N];

        for (int j = 0; j < randPermutations.length; j++) {
            for (int i = 0; i < k; i++) {
                tmpAnchors[i] = anchors[randPermutations[j][i]];
                tmpRanges[i] = ranges[randPermutations[j][i]];
            }
            PositionEstimate pe = LinearLeastSquares.multilaterate(tmpAnchors,
                    tmpRanges, weights);
            iPos[j] = pe != null ? pe.getLocation() : null;
            if (iPos[j] != null) {
                // calculate residue for all points and find median
                for (int i = 0; i < N; i++) {
                    double residue = iPos[j].distance(anchors[i]) - ranges[i];
                    tmpMedian[i] = residue * residue;
                }
                Arrays.sort(tmpMedian);
                medians[j] = tmpMedian[tmpMedian.length/2];
            } else {
                // use max value in this case, won't be chosen as least median
                medians[j] = Double.MAX_VALUE;
            }
        }

        // 3. Find index of least median
        int m = 0;
        for (int i = 1; i < medians.length; i++) {
            if (medians[i] < medians[m]) {
                m = i;
            }
        }

        // 4. Calculate s0
        double s0 = 1.4826 * (1.0 + 5.0 / ((double)N - 2.0)) * Math.sqrt(medians[m]);

        // 5. Assign weights to samples
        weights = new double[N];
        for (int i = 0; i < N; i++) {
            double ri = iPos[m] != null ?
                    iPos[m].distance(anchors[i]) - ranges[i] :
                    Double.MAX_VALUE;
            weights[i] = (Math.abs(ri/s0) <= threshold) ? 1 : 0;
        }

        // 6. Calculate weighted LS and return result as final position
        PositionEstimate pe = LinearLeastSquares.multilaterate(anchors,
                ranges, weights);
        return pe != null ? pe.getLocation() : null;
    }
    
}
