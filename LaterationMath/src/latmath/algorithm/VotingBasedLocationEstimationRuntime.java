package latmath.algorithm;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Voting-Based Location Estimation (VBLE).
 * <p>
 * VBLE is described in "Attack-Resistant Location Estimation in Sensor Networks",
 * Donggang Liu, Peng Ning and Wenliang Kevin Du, 2005.
 * <p>
 * This is the original code as described in the above paper but with a runtime
 * optimization in the candidate overlap check procedure.
 *
 * @version 1.01, 2013-05-11
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class VotingBasedLocationEstimationRuntime extends BasicLaterationAlgorithm implements Releasable {

    /**
     * The error threshold in simulation units, e.g. meters.
     * <p>
     * According to the paper, set this value to the maximum
     * ranging error possible.
     */
    private double errorThresholdPositive = 2.85;
    
    /**
     * The error threshold in simulation units, e.g. meters.
     * <p>
     * According to the paper, set this value to the maximum
     * ranging error possible.
     */
    private double errorThresholdNegative = 2.85;
    
    /**
     * Percent of side length of the initial rectangle for the cell size L.
     */
    private int sideLength = 5;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "VBLE-R (" + String.format(Locale.ENGLISH,
                "%.2f", errorThresholdPositive) + ", "
                + String.format(Locale.ENGLISH,
                "%.2f", errorThresholdNegative) + ", " + sideLength + ")";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, errorThresholdPositive,
                errorThresholdNegative, sideLength);
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
        JPanel tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        
        // Create control
        final JSpinner spErrorThresholdPositive = new JSpinner();
        spErrorThresholdPositive.setModel(new SpinnerNumberModel(this.errorThresholdPositive, 0.0, 1000.0, 0.1));
        Dimension d = spErrorThresholdPositive.getPreferredSize();
        d.width = 60;
        spErrorThresholdPositive.setPreferredSize(d);
        // Create label and add control
        JPanel lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        JLabel label = new JLabel("Error threshold (too long):");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spErrorThresholdPositive);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        
        // Create control
        final JSpinner spErrorThresholdNegative = new JSpinner();
        spErrorThresholdNegative.setModel(new SpinnerNumberModel(this.errorThresholdNegative, 0.0, 1000.0, 0.1));
        d = spErrorThresholdNegative.getPreferredSize();
        d.width = 60;
        spErrorThresholdNegative.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Error threshold (too short):");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spErrorThresholdNegative);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        
        // Create control
        final JSpinner spSideLength = new JSpinner();
        spSideLength.setModel(new SpinnerNumberModel(this.sideLength, 1, 100, 1));
        d = spSideLength.getPreferredSize();
        d.width = 60;
        spSideLength.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Percent for cell size L:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spSideLength);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                errorThresholdPositive = (Double) spErrorThresholdPositive.getValue();
                errorThresholdNegative = (Double) spErrorThresholdNegative.getValue();
                sideLength = (Integer) spSideLength.getValue();
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
     * <p>
     * NOTE: Parameters <code>L</code> and <code>errorThreshold</code> might
     * need conversion if not given in meters!
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     * @param errorThreshold1 Maximum distance measurement error if error is
     *                        uniformly distributed; otherwise the average
     *                        (or twice the average) distance measurement error
     *                        might be more accurate.
     * @param errorThreshold2 Maximum distance measurement error if error is
     *                        uniformly distributed; otherwise the average
     *                        (or twice the average) distance measurement error
     *                        might be more accurate.
     * @param sideLength Percent of side length of the initial rectangle for
     *                   the cell size L.
     * 
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            double errorThreshold1, double errorThreshold2, int sideLength) {
        // step 1: find minimum rectangle that covers all anchors
        double maxRanging = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE, maxX = 0;
        double minY = Double.MAX_VALUE, maxY = 0;
        for (int i = 0; i < anchors.length; i++) {
            if (anchors[i].x > maxX) {
                maxX = anchors[i].x;
            }
            if (anchors[i].x < minX) {
                minX = anchors[i].x;
            }
            if (anchors[i].y > maxY) {
                maxY = anchors[i].y;
            }
            if (anchors[i].y < minY) {
                minY = anchors[i].y;
            }
            if (ranges[i] > maxRanging) {
                maxRanging = ranges[i];
            }
        }

        // step 2: extend rectangle by maximum transmission range of a beacon
        //         signal, here: extend rectangle by maximum ranging value
        minX -= maxRanging;
        maxX += maxRanging;
        minY -= maxRanging;
        maxY += maxRanging;

        // for the sake of comparison, L - the side length of a cell in
        // meters (grid step size) - should be set to the final value of our
        // optimized version
        double minSide = Math.min(maxX - minX, maxY - minY);
        double L = (sideLength/100.0) * minSide;

        // step 3: devide rectangle into M small squares (cells) with the same
        //         side length L. No iterative refinement or other optimizations
        //         are used in this version, because we don't run on resource
        //         constrained sensor nodes. NOTE: Use appropriate side length
        //         value conversion if value is not given in meters!
        int score;
        int maxScore = 0;
        int maxScoreIndex = 0;
        double xMaxScore = 0;
        double yMaxScore = 0;
        double x, y;
        for (x = minX; x < maxX; x += L) {
            for (y = minY; y < maxY; y += L) {
                score = 0;
                for (int i = 0; i < anchors.length; i++) {
                    // calculate candidate ring with given error threshold in
                    // meters. NOTE: Use appropriate error threshold value
                    // conversion if value is not given in meters!
                    // Test if cell overlaps with current candidate ring, code
                    // not optimized!
                    double dMin, dMax;
                    double ri = ((ranges[i] - errorThreshold1) > 0)
                            ? (ranges[i] - errorThreshold1) : 0;
                    double ro = ranges[i] + errorThreshold2;
                    
                    // THIS IS DIFFERENT TO THE ORIGINAL CODE:
                    // calculate dMin and dMax for candidate ring overlap check
                    // without the need of a long if-then-else block. However,
                    // in Java this isn't really much faster! In the C SSE/AVX
                    // version this is needed for vectorization and thus faster.
                    double tmp1, tmp2;
                    tmp1 = anchors[i].x >= x ? anchors[i].x : x;
                    tmp2 = anchors[i].y >= y ? anchors[i].y : y;
                    double closestX = Math.min(tmp1, x+L); // leave one Min call
                    double closestY = Math.min(tmp2, y+L); // to be fair
                    tmp1 = closestX - x;
                    tmp2 = (x+L) - closestX;
                    //double farXoffset = Math.max(closestX - x, (x+L) - closestX);
                    double farXoffset = tmp1 >= tmp2 ? tmp1 : tmp2;
                    tmp1 = closestY - y;
                    tmp2 = (y+L) - closestY;
                    //double farYoffset = Math.max(closestY - y, (y+L) - closestY);
                    double farYoffset = tmp1 >= tmp2 ? tmp1 : tmp2;
                    tmp1 = closestX - anchors[i].x;
                    tmp2 = anchors[i].x - closestX;
                    //double minDistX = Math.max(closestX - anchors[i].x, anchors[i].x - closestX);
                    double minDistX = tmp1 >= tmp2 ? tmp1 : tmp2;
                    tmp1 = closestY - anchors[i].y;
                    tmp2 = anchors[i].y - closestY;
                    //double minDistY = Math.max(closestY - anchors[i].y, anchors[i].y - closestY);
                    double minDistY = tmp1 >= tmp2 ? tmp1 : tmp2;
                    double maxDistX = minDistX + farXoffset;
                    double maxDistY = minDistY + farYoffset;
                    // to be fair: leave square root call as in original impl.
                    dMin = Math.sqrt(minDistX * minDistX + minDistY * minDistY);
                    dMax = Math.sqrt(maxDistX * maxDistX + maxDistY * maxDistY);
                    
                    // test if candidate ring overlaps with cell
                    if (!(dMin > ro || dMax < ri)) {
                        score++;
                    }
                }
                if (score >= maxScore) {
                    if (score > maxScore) {
                        maxScore = score;
                        xMaxScore = 0;
                        yMaxScore = 0;
                        maxScoreIndex = 0;
                    }
                    if (maxScore > 0) {
                        xMaxScore += x + L/2;
                        yMaxScore += y + L/2;
                        maxScoreIndex++;
                    }
                }
            }
        }

        // step 4: return geometric centroid from the cells with the highest
        //         vote as the estimated location
        return new Point2d(xMaxScore/maxScoreIndex, yMaxScore/maxScoreIndex);
    }
    
}
