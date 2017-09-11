package latmath.algorithm;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import latmath.errormodel.ErrorModel;
import latmath.util.LMath;
import latmath.util.Point2d;

/**
 * Brute Force Optimal Anchor Selection Algorithm (BF-OASA).
 * <p>
 * Selects anchors which lead to best localization result. Only works if real
 * location of mobile node is known.
 * <p>
 * This algorithm is for optimization purposes only and not contained in the
 * algorithm collection class!
 *
 * @version 1.0, 2012-02-21
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class OptimalAnchorSelectionAlgorithm extends BasicLaterationAlgorithm {

    /** The algorithm that really does the localization */
    private BasicLaterationAlgorithm algorithm;

    /** Internal BF-OASA stats */
    private transient InternalBfOasaStatistic statistics;

    private static final String NAME = "BF-OASA";
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public OptimalAnchorSelectionAlgorithm() {
        statistics = new InternalBfOasaStatistic();
    }

    public OptimalAnchorSelectionAlgorithm(BasicLaterationAlgorithm algorithm) {
        this.algorithm = algorithm;
        statistics = new InternalBfOasaStatistic();
    }
    
    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();
            statistics = new InternalBfOasaStatistic();
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    public BasicLaterationAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(BasicLaterationAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public InternalBfOasaStatistic getStatistic() {
        return statistics;
    }

    @Override
    public String getName() {
        return algorithm != null ? NAME + " | " + algorithm.getName() : NAME;
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, actualPosition, errorModel,
                algorithm, statistics);
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
     * Also reset internal algorithm used (for statistics).
     */
    @Override
    public void reset() {
        super.reset();
        statistics.reset();
        if (algorithm != null) {
            algorithm.reset();
        }
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
     * @param actualPosition The actual location of the mobile node to be located.
     * @param errorModel The current error model used for simulation or
     *                   <code>null</code> if running with real data (no error
     *                   model available).
     * @param algorithm The algorithm that really does the localization.
     * @param statistics Object to store statistics about BF-OASA.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel,
            BasicLaterationAlgorithm algorithm, InternalBfOasaStatistic statistics) {
        // step 0: sanity checks
        if (anchors.length != ranges.length) {
            return null;
        }

        if (algorithm == null) {
            return null;
        }

        // clear statistics
        statistics.getIntermediatePositions().clear();

        if (actualPosition == null) {
            // cannot do brute force search of best set, exit here
            return algorithm.localize(anchors, ranges, actualPosition, errorModel, -1, -1);
        }

        // step 1: We can run brute force method, calculate all k-permutations
        // for k = 3 to ANCHORS_LENGTH and run algorithm with them, store best
        double bestDistance = 0;
        Point2d bestLocation = null;
        double[] bestRanges = null;     // for statistics only
        Point2d[] bestAnchors = null;   // for statistics only
        for (int k = 3; k <= anchors.length; k++) {

            int binom = LMath.binom(anchors.length, k);
            int[] permutations = new int[k];
            double[] tmpRanges = new double[k];
            Point2d[] tmpAnchors = new Point2d[k];

            // initialisation for calculating k-permutations
            for (int i = 0; i < permutations.length; i++) {
                permutations[i] = i;
            }

            // build all k-permutations
            for (int i = 0; i < binom; i++) {

                // calculate position estimate using the given algorithm
                for (int h = 0; h < k; h++) {
                    tmpAnchors[h] = anchors[permutations[h]];
                    tmpRanges[h] = ranges[permutations[h]];
                }

                Point2d iPos = algorithm.localize(tmpAnchors, tmpRanges,
                        actualPosition, errorModel, -1, -1);

                if (iPos != null) {
                    double distance = iPos.distance(actualPosition);
                    if (bestLocation == null || distance < bestDistance) {
                        bestLocation = iPos;
                        bestDistance = distance;
                        // for statistics only:
                        if (bestRanges == null || bestRanges.length != tmpRanges.length) {
                            bestRanges = new double[tmpRanges.length];
                            bestAnchors = new Point2d[tmpAnchors.length];
                        }
                        System.arraycopy(tmpRanges, 0, bestRanges, 0, bestRanges.length);
                        System.arraycopy(tmpAnchors, 0, bestAnchors, 0, bestAnchors.length);
                    }
                    // store intermediate position for display purposes
                    statistics.getIntermediatePositions().add(iPos);
                }

                // build next permutation
                if (i == binom - 1) {
                    break;
                }
                int j = k - 1;
                while (j >= 0) {
                    if (!LMath.incCounter(permutations, j, anchors.length, k)) {
                        break;
                    }
                    j--;
                }
                for (int l = j + 1; l < k; l++) {
                    permutations[l] = permutations[l - 1] + 1;
                }
            }
        }

        // step 2: we found the best location to have the shortest distance to
        //         the actual location of the mobile node (can be null), do
        //         evaluation of internal algorithm with best values and return
        if (bestRanges == null) {
            // calculation failed, take all anchors and ranges as input for eval
            bestRanges = ranges;
            bestAnchors = anchors;
        }
        // evaluate and collect stats
        algorithm.evaluate(bestAnchors, bestRanges, bestLocation, actualPosition);
        statistics.process(bestAnchors, bestRanges, actualPosition);
        // return best location
        return bestLocation;
    }

    /**
     * Internal statistic for evaluation of BF-OASA.
     */
    public static class InternalBfOasaStatistic {

        private double avgRangeError;
        private double maxRangeError;
        private long avgRangeErrorCount;
        private Hashtable<Integer, Integer> rangingHistogram;

        private Point2d[] anchors;
        private double[] ranges;
        private ArrayList<Point2d> intermediatePositions;

        public InternalBfOasaStatistic() {
            rangingHistogram = new Hashtable<>();
            intermediatePositions = new ArrayList<>();
        }

        public void reset() {
            avgRangeErrorCount = 0;
            avgRangeError = 0;
            maxRangeError = 0;
            rangingHistogram.clear();
        }

        public void process(Point2d[] anchors, double[] ranges, Point2d realPosition) {
            this.anchors = anchors;
            this.ranges = ranges;
            for (int i = 0; i < ranges.length; i++) {
                if (realPosition != null) {
                    double offset = ranges[i] - anchors[i].distance(realPosition);

                    // For statistics remove sign
                    offset = Math.abs(offset);

                    // Account total average ranging error
                    avgRangeError += offset;
                    avgRangeErrorCount++;
                    if (offset > maxRangeError) {
                        maxRangeError = offset;
                    }
                }
            }
            // Account total rangings by anchor count
            histogramIncValue(anchors.length, rangingHistogram);
        }

        public Point2d[] getAnchors() {
            return anchors;
        }

        public double[] getRanges() {
            return ranges;
        }

        public List<Point2d> getIntermediatePositions() {
            return intermediatePositions;
        }

        public double getAverageRangingError() {
            return avgRangeErrorCount > 0 ? avgRangeError/avgRangeErrorCount : 0;
        }

        public double getMaximumRangingError() {
            return maxRangeError;
        }

        public Integer[] getAnchorCountFrequency() {
            return toNumberArray(rangingHistogram);
        }

        private void histogramIncValue(int key, Hashtable<Integer, Integer> histogram) {
            Integer v = histogram.get(key);
            if (v == null) {
                v = 0;
            }
            v++;
            histogram.put(key, v);
        }

        private Integer[] toNumberArray(Hashtable<Integer, Integer> table) {
            int maxKey = -1;
            Integer[] keys = table.keySet().toArray(new Integer[0]);
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] > maxKey) {
                    maxKey = keys[i];
                }
            }
            Integer[] values = new Integer[maxKey+1];
            for (int i = 0; i < values.length; i++) {
                Integer val = table.get(i);
                values[i] = val != null ? val : 0;
            }
            return values;
        }

    }

}
