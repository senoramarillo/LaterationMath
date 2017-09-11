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
 * Optimized Voting-Based Location Estimation (VBLE-O).
 * <p>
 * This version is a runtime improved implementation in contrast to
 * <code>VotingBasedLocationEstimation</code>. Also some changes were made
 * improving the localization accuracy (no negative distance measurements,
 * finding optimum values for grid step size L and error threshold).
 *
 * @version 1.01, 2013-05-10
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class OptimizedVotingBasedLocationEstimation extends BasicLaterationAlgorithm implements Releasable {

    /**
     * The error threshold for measuring too long in
     * simulation units, e.g. meters.
     * <p>
     * Set to maximum or expected measurement error.
     */
    private double errorThresholdPositive = 2.85;
    
    /**
     * The error threshold for measuring too short in
     * simulation units, e.g. meters.
     * <p>
     * Set to zero if it is impossible to measure too short.
     */
    private double errorThresholdNegative = 0.0;
    
    /**
     * Percent of side length of the initial rectangle for the cell size L.
     */
    private int sideLengthStart = 40;
    
    /**
     * Percent of side length of the initial rectangle for the smallest
     * cell size L to cancel iteration.
     */
    private int sideLengthEnd = 5;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "VBLE-O (" + String.format(Locale.ENGLISH,
                "%.2f", errorThresholdPositive) + ", "
                + String.format(Locale.ENGLISH,
                "%.2f", errorThresholdNegative) + ", "
                + sideLengthStart + ", " + sideLengthEnd + ")";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, errorThresholdPositive,
                errorThresholdNegative, sideLengthStart, sideLengthEnd);
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
        final JSpinner spSideLengthStart = new JSpinner();
        spSideLengthStart.setModel(new SpinnerNumberModel(this.sideLengthStart, 1, 100, 1));
        d = spSideLengthStart.getPreferredSize();
        d.width = 60;
        spSideLengthStart.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Percent for initial cell size L:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spSideLengthStart);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        
        // Create control
        final JSpinner spSideLengthEnd = new JSpinner();
        spSideLengthEnd.setModel(new SpinnerNumberModel(this.sideLengthEnd, 1, 100, 1));
        d = spSideLengthEnd.getPreferredSize();
        d.width = 60;
        spSideLengthEnd.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Percent for final cell size L:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spSideLengthEnd);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                errorThresholdPositive = (Double) spErrorThresholdPositive.getValue();
                errorThresholdNegative = (Double) spErrorThresholdNegative.getValue();
                int tmp = (Integer) spSideLengthStart.getValue();
                int tmp2 = (Integer) spSideLengthEnd.getValue();
                if (tmp > tmp2) {
                    sideLengthStart = tmp;
                    sideLengthEnd = tmp2;
                }
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
     * @param errorThreshold1 Maximum distance measurement error if error is
     *                        uniformly distributed; otherwise the average
     *                        (or twice the average) distance measurement error
     *                        might be more accurate.
     * @param errorThreshold2 Maximum distance measurement error if error is
     *                        uniformly distributed; otherwise the average
     *                        (or twice the average) distance measurement error
     *                        might be more accurate.
     * @param sideLengthStart Percent of side length of the initial rectangle
     *                        for the cell size L.
     * @param sideLengthEnd   Percent of side length of the initial rectangle
     *                        for the smallest cell size L to cancel iteration.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            double errorThreshold1, double errorThreshold2, int sideLengthStart,
            int sideLengthEnd) {
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
        //         plus errorThreshold2 value
        maxRanging += errorThreshold2;
        minX -= maxRanging;
        maxX += maxRanging;
        minY -= maxRanging;
        maxY += maxRanging;

        // step 3: calculate L - the side length of a cell in meters
        //         (grid step size). Use 40 percent of the rectangles
        //         shorter side.
        double minSide = Math.min(maxX - minX, maxY - minY);
        double L = (sideLengthStart/100.0) * minSide;
        double lastL = (sideLengthEnd/100.0) * minSide;

        // step 4: devide rectangle into M small squares (cells) with the same
        //         side length L. Iterative refinement and other optimizations
        //         are used in this version. NOTE: Use appropriate side length
        //         value conversion if value is not given in meters!
        return multilaterate(anchors, ranges, L, lastL, errorThreshold1,
                errorThreshold2, minX, maxX, minY, maxY, minX, maxX, minY, maxY);
    }

    private static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            double L, double lastL, double errorThreshold1,
            double errorThreshold2, double minX, double maxX,
            double minY, double maxY, double iterMinX, double iterMaxX,
            double iterMinY, double iterMaxY) {
        // do iterative/recursive solution
        int maxScore = 0;
        int maxScoreIndex = 0;
        int xLength = (int) Math.floor((maxX - minX)/L + 1);
	int yLength = (int) Math.floor((maxY - minY)/L + 1);
        double x, y;
        double finalX = 0, finalY = 0;
        double sqrtTwo = Math.sqrt(2);
        int xMin, xMax, yMin, yMax;
        byte[][] scores = new byte[yLength+1][xLength+1];
        double minXNew = Double.MAX_VALUE, maxXNew = Double.MIN_VALUE,
                minYNew = Double.MAX_VALUE, maxYNew = Double.MIN_VALUE;
        
        for (int i = 0; i < anchors.length; i++) {
            // calculate candidate ring with given error threshold in
            // meters. NOTE: Use appropriate error threshold value
            // conversion if value is not given in meters!
            // Test if cell overlaps with current candidate ring, in
            // contrast to the authors we dont't use negative distance
            // measurement errors!
            double ri = ((ranges[i] - errorThreshold1) > 0)
                    ? (ranges[i] - errorThreshold1) : 0;
            double ro = ranges[i] + errorThreshold2;

            // optimize outer test region
            xMin = (int) Math.floor((anchors[i].x - ro - minX) / L);
            yMin = (int) Math.floor((anchors[i].y - ro - minY) / L);
            int itmp = (int) Math.ceil(2 * ro / L + 1);
            xMax = xMin + itmp;
            yMax = yMin + itmp;

            // for iteration: find overlaping rectangles
            double anchorRectMinX = minX + xMin * L;
            double anchorRectMinY = minY + yMin * L;
            double anchorRectMaxX = minX + xMax * L;
            double anchorRectMaxY = minY + yMax * L;

            // test for intersection
            if (iterMaxX <= anchorRectMinX || iterMinX >= anchorRectMaxX
                    || iterMaxY <= anchorRectMinY || iterMinY >= anchorRectMaxY) {
                continue; // no intersection
            }

            int a = (int) Math.floor((iterMinX - minX) / L);
            int b = (int) Math.floor((iterMinY - minY) / L);
            int c = (int) Math.ceil((iterMaxX - minX) / L);
            int d = (int) Math.ceil((iterMaxY - minY) / L);
            if (a != 0 || b != 0 || maxX-iterMaxX != 0 || maxY-iterMaxY != 0) {
                xMin = Math.max(xMin, a);
                yMin = Math.max(yMin, b);
                xMax = Math.min(xMax, c);
                yMax = Math.min(yMax, d);
                if (xMin == xMax) {
                    xMax++; // possible NaN workaround
                }
                if (yMin == yMax) {
                    yMax++; // possible NaN workaround
                }
            }

            // optimize inner test region
            double tmp = (sqrtTwo * ri) / 2;
            double iboxMinX = anchors[i].x - tmp;
            double iBoxMinY = anchors[i].y - tmp;
            double iboxMaxX = anchors[i].x + tmp;
            double iBoxMaxY = anchors[i].y + tmp;

            // test each cell and increase score counter, use outer
            // test optimization
            for (int j = xMin; j < xMax; j++) {
                x = minX + j * L;
                int k = yMin;
                while (k < yMax) {
                    y = minY + k * L;
                    double dMin, dMax;
                    double xPlusL = x + L;
                    double yPlusL = y + L;
                    // check for inner cell optimization
                    if (x > iboxMinX && xPlusL < iboxMaxX && y > iBoxMinY && yPlusL < iBoxMaxY) {
                        int skip = (int) (((iBoxMaxY - y) - L) / L);
                        k += skip > 1 ? skip : 1;
                        continue;
                    }
                    if (anchors[i].x < x && anchors[i].y < y) {
                        // sector 1
                        dMin = new Point2d(x, y).distance(anchors[i]);
                        dMax = new Point2d(xPlusL, yPlusL).distance(anchors[i]);
                    } else if (anchors[i].x > xPlusL && anchors[i].y < y) {
                        // sector 3
                        dMin = new Point2d(xPlusL, y).distance(anchors[i]);
                        dMax = new Point2d(x, yPlusL).distance(anchors[i]);
                    } else if (anchors[i].x < x && anchors[i].y > yPlusL) {
                        // sector 7
                        dMin = new Point2d(x, yPlusL).distance(anchors[i]);
                        dMax = new Point2d(xPlusL, y).distance(anchors[i]);
                    } else if (anchors[i].x > xPlusL && anchors[i].y > yPlusL) {
                        // sector 9
                        dMin = new Point2d(xPlusL, yPlusL).distance(anchors[i]);
                        dMax = new Point2d(x, y).distance(anchors[i]);
                    } else if (anchors[i].y < y && anchors[i].x >= x && anchors[i].x <= xPlusL) {
                        // sector 2
                        dMin = y - anchors[i].y;
                        if (anchors[i].x - x > (xPlusL) - anchors[i].x) {
                            dMax = new Point2d(x, yPlusL).distance(anchors[i]);
                        } else {
                            dMax = new Point2d(xPlusL, yPlusL).distance(anchors[i]);
                        }
                    } else if (anchors[i].y > yPlusL && anchors[i].x >= x && anchors[i].x <= xPlusL) {
                        // sector 8
                        dMin = anchors[i].y - (yPlusL);
                        if (anchors[i].x - x > (xPlusL) - anchors[i].x) {
                            dMax = new Point2d(x, y).distance(anchors[i]);
                        } else {
                            dMax = new Point2d(xPlusL, y).distance(anchors[i]);
                        }
                    } else if (anchors[i].x < x && anchors[i].y >= y && anchors[i].y <= yPlusL) {
                        // sector 4
                        dMin = x - anchors[i].x;
                        if (anchors[i].y - y > (yPlusL) - anchors[i].y) {
                            dMax = new Point2d(xPlusL, y).distance(anchors[i]);
                        } else {
                            dMax = new Point2d(xPlusL, yPlusL).distance(anchors[i]);
                        }
                    } else if (anchors[i].x > xPlusL && anchors[i].y >= y && anchors[i].y <= yPlusL) {
                        // sector 6
                        dMin = anchors[i].x - (xPlusL);
                        if (anchors[i].y - y > (yPlusL) - anchors[i].y) {
                            dMax = new Point2d(x, y).distance(anchors[i]);
                        } else {
                            dMax = new Point2d(x, yPlusL).distance(anchors[i]);
                        }
                    } else {
                        // sector 5
                        dMin = 0;
                        double distTopL = new Point2d(x, y).distance(anchors[i]);
                        double distTopR = new Point2d(xPlusL, y).distance(anchors[i]);
                        double distBottomL = new Point2d(x, yPlusL).distance(anchors[i]);
                        double distBottomR = new Point2d(xPlusL, yPlusL).distance(anchors[i]);
                        dMax = Math.max(Math.max(distTopL, distTopR),
                                Math.max(distBottomL, distBottomR));
                    }

                    // test if candidate ring overlaps with cell
                    if (!(dMin > ro || dMax < ri)) {
                        scores[k][j]++;
                    }

                    if (scores[k][j] >= maxScore) {
                        if (scores[k][j] > maxScore) {
                            maxScore = scores[k][j];
                            maxScoreIndex = 0;
                            minXNew = Double.MAX_VALUE;
                            maxXNew = Double.MIN_VALUE;
                            minYNew = Double.MAX_VALUE;
                            maxYNew = Double.MIN_VALUE;
                            finalX = 0;
                            finalY = 0;
                        }
                        if (maxScore > 0) {
                            double tx = minX + j*L;
                            double ty = minY + k*L;
                            if (tx < minXNew) {
                                minXNew = tx;
                            }
                            if (tx > maxXNew) {
                                maxXNew = tx;
                            }
                            if (ty < minYNew) {
                                minYNew = ty;
                            }
                            if (ty > maxYNew) {
                                maxYNew = ty;
                            }
                            finalX += tx + L/2;
                            finalY += ty + L/2;
                            maxScoreIndex++;
                        }
                    }

                    k++;
                }
            }
        }

        // return geometric centroid from the cells with the highest
        // vote as the estimated location. Do recursion if neccessary
        // precision is not reached.
        if (L/2 < lastL) {
            return new Point2d(finalX / maxScoreIndex, finalY / maxScoreIndex);
        } else {
            return multilaterate(anchors, ranges, L/2, lastL, errorThreshold1,
                    errorThreshold2, minX, maxX, minY, maxY, minXNew, maxXNew+L,
                    minYNew, maxYNew+L);
        }
    }
    
}
