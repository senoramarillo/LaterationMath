package latmath.algorithm;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.errormodel.ErrorModel;
import latmath.util.Circle;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Clustering based Robust Localization (CluRoL).
 * <p>
 * CluRoL is described in "CluRoL: Clustering based Robust Localization in
 * Wireless Sensor Networks", Satyajayant Misra, Guoliang Xue, 2007.
 * <p>
 * The paper doesn't state how the minimum MSE problem is solved (LLS or
 * NLLS). This is the second approach using NLLS method, however, more unlikely.
 * 
 * @version 1.0, 2012-03-06
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ClusteringBasedRobustLocalization2 extends BasicLaterationAlgorithm implements Releasable {
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    /**
     * Set to maximum distance error (paper value).
     * Seems to be a relative value to current distance measured.
     */
    private double dMax = 0.05;
    
    @Override
    public String getName() {
        return "CluRoL (NLLS) [dMax=" + dMax + "]";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, dMax);
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
        final JSpinner clurolDmax = new JSpinner();
        clurolDmax.setModel(new SpinnerNumberModel(this.dMax, 0.0, 1000.0, 0.1));
        Dimension d = clurolDmax.getPreferredSize();
        d.width = 60;
        clurolDmax.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("dMax value:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(clurolDmax);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dMax = (Double) clurolDmax.getValue();
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
     * @param dMax Maximum measurement error in distance estimation process,
     *             in the paper used as uniform random variable ~U[-dMax,dMax].
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            double dMax) {
        
        // step 0: sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

        // step 1: initialize variables
        int n = anchors.length;
        int ancStatusTaken = 0;
        boolean[] ancStatus = new boolean[n];

        // step 2: calculate circle intersections
        int binom = LMath.binom(n, 2);
        Point2d[][] intersections = new Point2d[binom][];

        int num = 0;
        for (int i = 0; i < anchors.length-1; i++) {
            for (int j = i+1; j < anchors.length; j++) {
                // Berechne Schnittpunkte mit aktueller Permutation
                intersections[num] = Circle.getIntersection(anchors[i],
                        ranges[i], anchors[j], ranges[j]);
                num++;
            }
        }

        // step 3: copy intersections into one array
        int totalLength = 0;
        int currentLength = 0;
        for (int i = 0; i < intersections.length; i++) {
            if (intersections[i] != null) {
                totalLength += intersections[i].length;
            }
        }
        Point2d[] points = new Point2d[totalLength];
        for (int i = 0; i < intersections.length; i++) {
            if (intersections[i] != null) {
                System.arraycopy(intersections[i], 0, points, currentLength,
                        intersections[i].length);
                currentLength += intersections[i].length;
            }
        }

        if (points.length < 2) {
            // no sense to do CluRoL, return NLLS here
            PositionEstimate pe = NonlinearLeastSquares.multilaterate(anchors,
                ranges);
            return pe != null ? pe.getLocation() : null;
        }

        // step 4: build all pairwise distance tuples
        num = 0;
        binom = LMath.binom(points.length, 2);
        PairwiseDistanceTuple[] D = new PairwiseDistanceTuple[binom];
        for (int i = 0; i < points.length-1; i++) {
            for (int j = i+1; j < points.length; j++) {
                D[num++] = new PairwiseDistanceTuple(points[i], points[j]);
            }
        }

        // step 5: sort D in ascending order of the pairwise distances
        Arrays.sort(D);

        // step 6: calculate distance threshold as n-th percentile tuple’s
        //         pairwise distance value
        int alpha = LMath.binom((int)(Math.ceil(n/2.0) + 2), 2);
        int beta = 2 * LMath.binom(n, 2);
        double nth = (LMath.binom(alpha, 2)/(double)LMath.binom(beta, 2));
        int nthPercentile = (int) Math.round((nth * D.length) + 0.5);
        nthPercentile = Math.min(nthPercentile, D.length);
        double dth = D[nthPercentile-1].d;

        // step 7: call findMaxCluster subroutine
        Set<Point2d> cMax = findMaxCluster(D, dth);

        // step 8: determine anchors for Minimum Squared Error (MSE) method
        double boundConst = (1 + dMax) * (1 + dMax);
        Iterator<Point2d> iter = cMax.iterator();
        while (iter.hasNext()) {
            Point2d x = iter.next();
            for (int i = 0; i < n; i++) {
                double ub = ranges[i] * boundConst;
                double lb = ranges[i] / boundConst;
                double dist = x.distance(anchors[i]);
                if (dist <= ub && dist >= lb) {
                    if (!ancStatus[i]) {
                        ancStatusTaken++;
                    }
                    ancStatus[i] = true;
                }
            }
        }

        // step 9: copy anchors and ranges into new array and localize
        num = 0;
        Point2d[] anchorsTaken = new Point2d[ancStatusTaken];
        double[] rangesTaken = new double[ancStatusTaken];
        for (int i = 0; i < n; i++) {
            if (ancStatus[i]) {
                anchorsTaken[num] = anchors[i];
                rangesTaken[num++] = ranges[i];
            }
        }

        PositionEstimate pe = NonlinearLeastSquares.multilaterate(anchorsTaken,
                rangesTaken);
        return pe != null ? pe.getLocation() : null;
    }

    /**
     * Finds maximum cluster.
     *
     * @param D Sorted list of pairwise distance tuples.
     * @param dth distance threshold value.
     *
     * @return The maximum cluster.
     */
    private static Set<Point2d> findMaxCluster(PairwiseDistanceTuple[] D,
            double dth) {
        List<Set<Point2d>> clusterList = new ArrayList<>();
        
        // Build initial cluster
        Set<Point2d> firstCluster = new HashSet<>();
        firstCluster.add(D[0].x);
        firstCluster.add(D[0].y);
        clusterList.add(firstCluster);

        for (int i = 1; i < D.length; i++) {
            Point2d x = D[i].x;
            Point2d y = D[i].y;
            double dist = D[i].d;
            Set<Point2d> clusterOfX = null;
            Set<Point2d> clusterOfY = null;
            Iterator<Set<Point2d>> iter = clusterList.iterator();
            while (iter.hasNext()) {
                Set<Point2d> cluster = iter.next();
                if (clusterOfX == null && cluster.contains(x)) {
                    clusterOfX = cluster;
                }
                if (clusterOfY == null && cluster.contains(y)) {
                    clusterOfY = cluster;
                }
                if (clusterOfX != null && clusterOfY != null) {
                    break;
                }
            }

            // test first condition, paper lines 5..7
            if (clusterOfX == null && clusterOfY == null) {
                // x and y not in any cluster, add to a new cluster
                Set<Point2d> newCluster = new HashSet<>();
                newCluster.add(x);
                newCluster.add(y);
                clusterList.add(newCluster);
                // continue with for-loop
                continue;
            }

            // test second condition, paper lines 8..12
            if (clusterOfX != null && clusterOfY == null) {
                // x in cluster and y does not belong to any cluster
                if (dist <= dth) {
                    clusterOfX.add(y);
                }
                // continue with for-loop
                continue;
            }

            // test third condition, paper lines 13..17
            if (clusterOfX == null && clusterOfY != null) {
                // y in cluster and x does not belong to any cluster
                if (dist <= dth) {
                    clusterOfY.add(x);
                }
                // continue with for-loop
                continue;
            }

            // test fourth condition, paper lines 18..26
            if (clusterOfX != null && clusterOfY != null && clusterOfX != clusterOfY) {
                // need to check if Cx and Cy can be merged
                Point2d centroidCx = Point2d.centerOfMass(clusterOfX.toArray(new Point2d[0]));
                Point2d centroidCy = Point2d.centerOfMass(clusterOfY.toArray(new Point2d[0]));
                if (centroidCx.distance(centroidCy) <= dth) {
                    // Merge both sets, remove one set from list
                    clusterOfX.addAll(clusterOfY);
                    clusterList.remove(clusterOfY);
                }
            }
        }

        // return cluster with maximum cardinality as result
        Set<Point2d> maxCluster = clusterList.get(0);
        for (int i = 1; i < clusterList.size(); i++) {
            Set<Point2d> cluster = clusterList.get(i);
            if (cluster.size() > maxCluster.size()) {
                maxCluster = cluster;
            }
        }

        return maxCluster;
    }

    /**
     * Pairwise distance tuple class.
     */
    private static final class PairwiseDistanceTuple implements
            Comparable<PairwiseDistanceTuple> {

        /** The first point */
        public Point2d x;

        /** The second point */
        public Point2d y;

        /** The distance between both points */
        public double d;

        /**
         * Creates a new instance of {@code PairwiseDistanceTuple}.
         * <p>
         * The distance between {@code x} and {@code y} is automatically set.
         *
         * @param x The first point.
         * @param y The second point.
         */
        public PairwiseDistanceTuple(Point2d x, Point2d y) {
            this.x = x;
            this.y = y;
            this.d = x.distance(y);
        }

        @Override
        public int compareTo(PairwiseDistanceTuple o) {
            return Double.compare(d, o.d);
        }

    }

}
