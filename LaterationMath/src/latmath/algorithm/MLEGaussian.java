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
import latmath.util.ArrayUtils;
import latmath.util.Matrix;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Multilateration using maximum likelihood solution with Gaussian PDF.
 *
 * @version 1.0, 2013-02-11
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class MLEGaussian extends BasicLaterationAlgorithm implements Releasable {

    /** Gaussian MF by M2 */
    private double mean = 2.43;
    private double sdev = 3.57;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "MLE Gaussian("+String.format(Locale.ENGLISH, "%.2f", mean)+", "
                +String.format(Locale.ENGLISH, "%.2f", sdev)+"^2)";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        PositionEstimate pe = multilaterate(anchors, ranges, mean, sdev);
        return pe != null ? pe.getLocation() : null;
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
        final JSpinner spMean = new JSpinner();
        spMean.setModel(new SpinnerNumberModel(this.mean, 0.0, 1000.0, 0.1));
        Dimension d = spMean.getPreferredSize();
        d.width = 60;
        spMean.setPreferredSize(d);
        // Create label and add control
        JPanel lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        JLabel label = new JLabel("Normal distribution, mean:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spMean);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner spSdev = new JSpinner();
        spSdev.setModel(new SpinnerNumberModel(this.sdev, 0.0, 1000.0, 0.1));
        d = spSdev.getPreferredSize();
        d.width = 60;
        spSdev.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Normal distribution, sdev:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spSdev);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mean = (Double) spMean.getValue();
                sdev = (Double) spSdev.getValue();
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
     * @param mean The mean of the normal distribution.
     * @param sdev The standard deviation of the normal distribution.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static PositionEstimate multilaterate(Point2d[] anchors,
            double[] ranges, double mean, double sdev) {
        // sanity check
        if (anchors.length != ranges.length || anchors.length < 3) {
            return null;
        }

        for (int i = 0; i < ranges.length; i++) {
            ranges[i] = Math.max(ranges[i] - mean, 0.01);
        }
        
        // Starting point of optimization: s0 = (u0, v0)
        Point2d s0;

        // Fill weight matrix
        double[] weights = new double[anchors.length];
        ArrayUtils.fill(weights, 1/sdev);
        
        // Try different starting points and choose the one with the smallest
        // residual error as result
        Point2d[] sPoint = new Point2d[3];
        double minResidualError = Double.MAX_VALUE;
        int minResidualErrorIdx = -1;

        // Take the following points as starting point of optimization:
        //
        // 1. Linear least squares result
        PositionEstimate pe = LinearLeastSquares.multilaterate(anchors, ranges, weights);
        sPoint[0] = pe != null ? pe.getLocation() : null; // might be null

        // 2. Centroid
        sPoint[1] = Point2d.centerOfMass(anchors, weights);
        
        // 3. Min-Max
        sPoint[2] = MinMax.multilaterate(anchors, ranges); // might be null
        
        // precalc inverse covariance matrix/weight matrix W^-1,
        // entries are 1/variance = 1/(sdev*sdev)
        double[][] w = new double[anchors.length][anchors.length];
        for (int i = 0; i < anchors.length; i++) {
            w[i][i] = (weights[i] * weights[i]);
        }
        Matrix mWI = new Matrix(w);

        valueLoop:
        for (int j = 0; j < sPoint.length; j++) {

            s0 = sPoint[j];
            if (s0 == null) {
                continue;
            }

            double e0, e1;
            int iterations = 0;
            double epsilon = 0.001;
            Point2d r0 = new Point2d();

            do {
                // initial squared error
                e0 = PositionEstimate.calculateResidualError(anchors, ranges, weights, s0);

                // 2. Solve equation of form W*A*x = W*b
                double[][] b = new double[anchors.length][1];
                double[][] a = new double[anchors.length][2];
                for (int i = 0; i < anchors.length; i++) {
                    double dist = s0.distance(anchors[i]);
                    if (dist == 0) {
                        // avoid NaN
                        continue valueLoop;
                    }
                    a[i][0] = (s0.x - anchors[i].x) / dist;
                    a[i][1] = (s0.y - anchors[i].y) / dist;
                    b[i][0] = (ranges[i] - dist) + (a[i][0] * s0.x + a[i][1] * s0.y);
                }

                // Solve with closed form solution: x = (A^T * W^-1 * A)^-1 * A^T * W^-1 * b
                Matrix mA = new Matrix(a);
                Matrix mB = new Matrix(b);
                Matrix mAT = mA.transpose();
                Matrix tmp = mAT.times(mWI);
                tmp = tmp.times(mA);
                tmp = tmp.inverse();
                if (tmp == null) {
                    // Matrix was singular => no inverse
                    break;
                }
                tmp = tmp.times(mAT);
                tmp = tmp.times(mWI);
                tmp = tmp.times(mB);
                r0.x = tmp.cell(0, 0);
                r0.y = tmp.cell(1, 0);

                // new squared error
                e1 = PositionEstimate.calculateResidualError(anchors, ranges, weights, r0);
                if (e0 - e1 < epsilon) {
                    break;
                }

                // Set refined position for next step
                s0.x = r0.x;
                s0.y = r0.y;
                iterations++;

            } while (iterations <= 10);

            // check if new result is better than last one
            if (e0 < minResidualError) {
                minResidualError = e0;
                minResidualErrorIdx = j;
            }
        }

        if (minResidualErrorIdx != -1) {
            return new PositionEstimate(sPoint[minResidualErrorIdx],
                    minResidualError);
        } else {
            return null;
        }
    }

}
