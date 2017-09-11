package latmath.algorithm;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.errormodel.ErrorModel;
import latmath.util.Circle;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Iterative Clustering-Based Localization Algorithm (ICLA).
 * <p>
 * ICLA is described in "An Iterative Clustering-Based Localization Algorithm
 * for Wireless Sensor Networks", Luo Haiyong, Li Hui, Zhao Fang, Peng Jinghua,
 * 2011.
 *
 * @version 1.0, 2012-02-16
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class IterativeClusteringBasedLocalization extends BasicLaterationAlgorithm implements Releasable {
    
    private double alpha;
    private double moveStep;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public IterativeClusteringBasedLocalization() {
        this(0.5, 1.5); // set moveStep to 25 for LS² and to 0.5 for real world data
    }

    public IterativeClusteringBasedLocalization(double moveStep, double alpha) {
        this.alpha = alpha;
        this.moveStep = moveStep;
    }

    @Override
    public String getName() {
        return "ICLA [step=" + moveStep + ", alpha=" + alpha + "]";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, moveStep, alpha);
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
    
    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean configure(Frame parent) {
        // Build JPanel with dialog content
        int lWidth = 200;
        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.PAGE_AXIS));
        
        // Add next control
        JLabel label;
        JPanel tmp, lContainer;
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner stepSize = new JSpinner();
        stepSize.setModel(new SpinnerNumberModel(this.moveStep, 0.0, 1000.0, 1.0));
        Dimension d = stepSize.getPreferredSize();
        d.width = 60;
        stepSize.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Size of move step:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(stepSize);
        content.add(tmp);
        
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner iclaAlpha = new JSpinner();
        iclaAlpha.setModel(new SpinnerNumberModel(this.alpha, 0.0, 1000.0, 1.0));
        d = iclaAlpha.getPreferredSize();
        d.width = 60;
        iclaAlpha.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Size of alpha:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(iclaAlpha);
        content.add(tmp);
        
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveStep = (Double) stepSize.getValue();
                alpha = (Double) iclaAlpha.getValue();
                dialog.dispose();
            }
        };

        ActionListener cancelAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        };

        dialog.setContent(content);
        dialog.setOKAction(okAction);
        dialog.setCancelAction(cancelAction);
        dialog.showDialog("Edit properties", false);
        return dialog.getDialogResult();
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
     * @param moveStep The length of a moving step.
     * @param alpha The convergent speed and clustering granularity.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges, double moveStep, double alpha) {
        // step 0: sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

        // step 1: calculate circle intersections
        int n = anchors.length;
        int k = 2;
        int[] permutations = new int[k];
        int binom = LMath.binom(n, k);
        Point2d[][] intersections = new Point2d[binom][];

        // initialisation for calculating k-permutations
        for (int i = 0; i < k; i++) {
            permutations[i] = i;
        }

        // build all k-permutations
        for (int i = 0; i < binom; i++) {
            intersections[i] = 
                    Circle.getIntersection(anchors[permutations[0]],
                    ranges[permutations[0]], anchors[permutations[1]],
                    ranges[permutations[1]]);
            // build next permutation
            if (i == binom - 1) break;
            int j = k - 1;
            while (j >= 0) {
                if (!LMath.incCounter(permutations, j, n, k)) break;
                j--;
            }
            for (int l = j+1; l < k; l++) {
                permutations[l] = permutations[l-1] + 1;
            }
        }

        // copy intersections into one array
        int totalLength = 0;
        for (int i = 0; i < intersections.length; i++) {
            if (intersections[i] == null) continue;
            totalLength += intersections[i].length;
        }

        // check if any intersections available
        if (totalLength == 0) {
            return null;
        }

        int currentLength = 0;
        IcmPoint[] points = new IcmPoint[totalLength];
        for (int i = 0; i < intersections.length; i++) {
            if (intersections[i] == null) continue;
            for (int j = 0; j < intersections[i].length; j++) {
                points[currentLength] = new IcmPoint(intersections[i][j], currentLength);
                currentLength++;
            }
        }

        // step 2: adapt iterative clustering model (ICM)
        boolean iterate = true;
        // ICM step 1: define initiation range
        computeInitialAttractingBoundary(points);
        while (iterate) {
            // ICM step 2: determine moving direction
            computeMovingDirection(points);
            // ICM step 3: move all points according to current direction on step forward
            for (int i = 0; i < points.length; i++) {
                if (points[i].merged) continue;
                points[i].nodesInRange = getNodesInRange(points, i);
            }
            for (int i = 0; i < points.length; i++) {
                if (points[i].merged || points[i].attractingBoundary == 0) continue;
                points[i].move(moveStep);
            }
            // ICM step 4: if merging condition is true, merge points
            for (int i = 0; i < points.length; i++) {
                if (points[i].merged) continue;
                if (points[i].nodesInRange == null) continue;
                // if only one node in range and not in this nodes' attracting
                // boundary => kick this node from list, set attracting boundary
                // to zero to stop from moving
                if (points[i].nodesInRange.length == 1) {
                    double d = points[i].currentLocation.distance(points[i].nodesInRange[0].currentLocation);
                    if (d > points[i].nodesInRange[0].attractingBoundary) {
                        // kick point
                        points[i].nodesInRange = null;
                        points[i].attractingBoundary = 0;
                    } else {
                        // merge points
                        if (!points[i].nodesInRange[0].merged) {
                            points[i].merge(points[i].nodesInRange[0]);
                        } else {
                            // merged with other point before we could merge
                            points[i].nodesInRange = null;
                            points[i].attractingBoundary = 0;
                        }
                    }
                } else {
                    // test if points can be merged
                    double dShort = Double.MAX_VALUE;
                    IcmPoint pShort = null;
                    for (int j = 0; j < points[i].nodesInRange.length; j++) {
                        if (points[i].nodesInRange[j].merged) continue;
                        double d = points[i].currentLocation.distance(points[i].nodesInRange[j].currentLocation);
                        if (d <= moveStep*Math.sqrt(2)) {
                             points[i].merge(points[i].nodesInRange[j]);
                        }
                        if (d < dShort) {
                            dShort = d;
                            pShort = points[i].nodesInRange[j];
                        }
                    }
                    // merge points when distance between them is the
                    // shortest in both points attracting boundary
                    if (pShort != null && !pShort.merged && pShort.nodesInRange != null) {
                        double dShort2 = Double.MAX_VALUE;
                        IcmPoint pShort2 = null;
                        for (int l = 0; l < pShort.nodesInRange.length; l++) {
                            if (pShort.nodesInRange[l].merged) continue;
                            double d = pShort.currentLocation.distance(pShort.nodesInRange[l].currentLocation);
                            if (d < dShort2) {
                                dShort2 = d;
                                pShort2 = pShort.nodesInRange[l];
                            }
                        }
                        if (pShort2 != null && pShort2 == points[i]) {
                            //points[i].merge(pShort);
                        }
                    }
                }
            }

            // ICM step 6: Update ranges of points whose range radii are not zero
            for (int i = 0; i < points.length; i++) {
                double dMax = 0;
                double dMin = Double.MAX_VALUE;
                if (points[i].merged) continue;
                if (points[i].attractingBoundary == 0) continue;
                if (points[i].nodesInRange != null) {
                    for (int j = 0; j < points[i].nodesInRange.length; j++) {
                        if (points[i].nodesInRange[j].merged) continue;
                        double d = points[i].currentLocation.distance(points[i].nodesInRange[j].currentLocation);
                        dMin = (d < dMin) ? d : dMin;
                        dMax = (d > dMax) ? d : dMax;
                    }
                }
                points[i].attractingBoundary = dMin == Double.MAX_VALUE ? 0 : dMin + ((dMax - dMin) / alpha);
            }

            // ICM step 5: check if we can terminate
            iterate = false;
            for (int i = 0; i < points.length; i++) {
                if (points[i].merged) continue;
                if (points[i].attractingBoundary != 0) {
                    iterate = true;
                    break;
                }
            }
        }

        // step 3: return centroid of selected intersection points
        int maxCluster = -1;
        for (int i = 0; i < points.length; i++) {
            if (!points[i].merged) {
                if (maxCluster == -1) {
                    maxCluster = i;
                } else {
                    if (points[i].mergeList.size() > points[maxCluster].mergeList.size()) {
                        maxCluster = i;
                    }
                }
            }
        }

        Point2d[] pCenterOfMass = new Point2d[points[maxCluster].mergeList.size()+1];
        pCenterOfMass[0] = points[maxCluster].intersection;
        for (int i = 1; i < pCenterOfMass.length; i++) {
            pCenterOfMass[i] = points[maxCluster].mergeList.get(i-1).intersection;
        }
        return Point2d.centerOfMass(pCenterOfMass);
    }

    // computes initial radius of each points' attracting boundary as longest
    // distance from other points. Uses brute force method (may be optimized)!
    private static void computeInitialAttractingBoundary(IcmPoint[] points) {
        for (int i = 0; i < points.length; i++) {
            double lDist = 0;
            for (int j = 0; j < points.length; j++) {
                if (i != j) {
                    double d = points[i].intersection.distance(points[j].intersection);
                    if (d > lDist) {
                        lDist = d;
                    }
                }
            }
            points[i].attractingBoundary = lDist;
        }
    }

    // compute moving direction of each point
    private static void computeMovingDirection(IcmPoint[] points) {
        for (int i = 0; i < points.length; i++) {
            if (points[i].merged) continue;
            int[] forceVector = new int[9]; // initial to 0's
            for (int j = 0; j < points.length; j++) {
                if (i != j && !points[j].merged && points[i].currentLocation.distance(
                        points[j].currentLocation) <= points[i].attractingBoundary) {
                    int idx = points[i].getAttractingForceDirection(points[j]);
                    forceVector[idx-1] += points[j].weight;
                }
            }
            int maxWeightIdx = 0;
            for (int j = 1; j < forceVector.length; j++) {
                if (forceVector[j] > forceVector[maxWeightIdx]) {
                    maxWeightIdx = j;
                }
            }
            points[i].movingDirection = maxWeightIdx + 1;
        }
    }

    private static IcmPoint[] getNodesInRange(IcmPoint[] points, int self) {
        int count = 0;
        IcmPoint[] result = null;
        for (int i = 0; i < points.length; i++) {
            if (points[i].merged) continue;
            if (i != self && points[i].currentLocation.distance(
                    points[self].currentLocation) <= points[self].attractingBoundary) {
                count++;
            }
        }
        if (count > 0) {
            result = new IcmPoint[count];
            count = 0;
            for (int i = 0; i < points.length; i++) {
                if (points[i].merged) continue;
                if (i != self && points[i].currentLocation.distance(
                        points[self].currentLocation) <= points[self].attractingBoundary) {
                    result[count++] = points[i];
                }
            }
        }
        return result;
    }

    private static class IcmPoint {

        private int id;
        public int weight;
        public boolean merged;
        public int movingDirection;
        public Point2d intersection;
        public Point2d currentLocation;
        public IcmPoint[] nodesInRange;
        public double attractingBoundary;
        public List<IcmPoint> mergeList;

        private static final int[][] MAPPING_TABLE = new int[][] {
            {6, 7, 8},
            {5, 9, 1},
            {4, 3, 2}
        };

        public static final Point2d[] MOVING_DIRECTIONS = new Point2d[] {
            null, // to follow paper index 1..9
            new Point2d(0, 1), new Point2d(1, 1), new Point2d(1, 0),
            new Point2d(1, -1), new Point2d(0, -1), new Point2d(-1, -1),
            new Point2d(-1, 0), new Point2d(-1, 1), new Point2d(0, 0)
        };

        public IcmPoint(Point2d intersection, int id) {
            this.id = id;
            this.intersection = intersection;
            this.mergeList = new ArrayList<>(30);
            this.currentLocation = new Point2d(intersection.x, intersection.y);
            this.merged = false;
            this.weight = 1;
        }

        @Override
        public String toString() {
            return id + ":" + intersection.toString();
        }

        public int getAttractingForceDirection(IcmPoint p) {
            double d = p.currentLocation.distance(currentLocation);
            int x = (int) Math.round((p.currentLocation.x - currentLocation.x) / d);
            int y = (int) Math.round((p.currentLocation.y - currentLocation.y) / d);
            return MAPPING_TABLE[x+1][y+1];
        }

        public void move(double step) {
            Point2d dir = MOVING_DIRECTIONS[movingDirection];
            double dirLength = Math.sqrt(dir.x * dir.x + dir.y * dir.y);
            if (dirLength == 0) return; // no movement
            currentLocation.x = currentLocation.x + (step / dirLength) * dir.x;
            currentLocation.y = currentLocation.y + (step / dirLength) * dir.y;
        }

        public void merge(IcmPoint p) {
            p.merged = true;
            mergeList.add(p);
            for (int i = 0; i < p.mergeList.size(); i++) {
                mergeList.add(p.mergeList.get(i));
            }
            this.weight += p.weight;
            this.attractingBoundary = Math.max(this.attractingBoundary, p.attractingBoundary);
        }

    }

}
