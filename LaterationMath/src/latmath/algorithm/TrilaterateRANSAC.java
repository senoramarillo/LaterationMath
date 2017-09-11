package latmath.algorithm;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Trilaterate (NLLS with 3 anchors) using RANSAC.
 * <p>
 * RANSAC is described in "Evaluating LaterationBased Positioning Algorithms for
 * FineGrained Tracking", Andrew Rice, Robert Harle, 2005.
 * <p>
 * Parts of code taken from NLMAP library.
 *
 * @version 1.0, 2013-07-02
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class TrilaterateRANSAC extends BasicLaterationAlgorithm implements Releasable {
    
    /**
     * Comes from experimental data, sdev of ranging error.
     */
    private double sigma = 3.62;
    
    /**
     * These values come from NLMAP library.
     */
    private double pGood = 0.5;
    private double pFail = 0.001;
    
    /**
     * Mode to calculate temp position: TRILAT or NLLS.
     */
    private int calcMode = CALC_MODE_TRILAT;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    private static final int CALC_MODE_TRILAT = 0;
    private static final int CALC_MODE_NLLS   = 1;
    
    @Override
    public String getName() {
        return "Trilaterate RANSAC";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, pFail, pGood, sigma, calcMode);
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
        final JComboBox apm = new JComboBox();
        String[] apmNames = new String[] {"Trilateration", "NLLS"};
        apm.setModel(new DefaultComboBoxModel(apmNames));
        apm.setSelectedIndex(calcMode);
        // Create label and add control
        JPanel lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        JLabel label = new JLabel("Set calculation method:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(apm);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner spSigma = new JSpinner();
        spSigma.setModel(new SpinnerNumberModel(this.sigma, 0.0, 1000.0, 0.1));
        Dimension d = spSigma.getPreferredSize();
        d.width = 60;
        spSigma.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Sigma (expected ranging error):");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spSigma);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcMode = apm.getSelectedIndex();
                sigma = (Double) spSigma.getValue();
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
     * @param pFail The probability that the algorithm will exit without
     *              finding a good fit if one exists (false negative).
     * @param pGood The probability of a randomly selected data item being
     *              part of a good model.
     * @param sigma Expected error for each distance (e.g. standard error).
     * @param calcMode Calculation mode for temp positions.
     * 
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            double pFail, double pGood, double sigma, int calcMode) {
        if (anchors == null || ranges == null || anchors.length < 3 ||
                anchors.length != ranges.length) {
            return null;
        }
        
        int bestQuorumSize = -1;
        Point2d bestEstimate = null;
        
        /**
         * The number of times to search for a valid quorum of data points. This
         * is calculated from p_fail and p_good in the following manner: pfail =
         * p(L consecutive failures) pfail = p(given trial is a failure)**L
         * pfail = p(1-given trial succeeds)**L pfail = p(1-p_good**N)**L L =
         * log(p_fail)/log(1-p_good**N) Note: N is the number of data points
         * required for tri-lateration (three). L is the number of iterations to
         * perform before giving up.
         */
        int mIterationCount = (int)(Math.log(pFail) / Math.log(1 -
                pGood * pGood * pGood));
        
        int[] points = new int[3];
        Random rand = new Random();
        for (int iterations = 0; iterations < mIterationCount; ++iterations) {

            // select 3 data points at random making
            // sure we have no collisions
            for (int i = 0; i < 3; ++i) {
                points[i] = rand.nextInt(anchors.length);
                for (int j = 0; j < i;) {
                    if (points[i] == points[j]) {
                        points[i] = rand.nextInt(anchors.length);
                        j = 0;
                    } else {
                        ++j;
                    }
                }
            }
            
            Point2d tmp;
            if (calcMode == TrilaterateRANSAC.CALC_MODE_TRILAT) {
                tmp = Trilateration.trilaterate(
                    new Point2d[] {
                        anchors[points[0]],
                        anchors[points[1]],
                        anchors[points[2]]},
                    new double[] {
                        ranges[points[0]],
                        ranges[points[1]],
                        ranges[points[2]]}
                    );
            } else {
                PositionEstimate pe = NonlinearLeastSquares.multilaterate(
                    new Point2d[] {
                        anchors[points[0]],
                        anchors[points[1]],
                        anchors[points[2]]},
                    new double[] {
                        ranges[points[0]],
                        ranges[points[1]],
                        ranges[points[2]]}
                    );
                if (pe == null) {
                    continue;
                }
                tmp = pe.getLocation();
            }
            
            // find out how many points within the data set fit the
            // model
            int t = 0;
            for (int i = 0; i < anchors.length; ++i) {
                double dist = tmp.distance(anchors[i]);
                double error = Math.abs(ranges[i] - dist);
                if (error < sigma) {
                    t++;
                }
            }

            // if this is more than 8 points (see the original paper for where
            // this number comes from) then return the data
            if (t == anchors.length || t > 8) {
                return tmp;
            }

            if (t > bestQuorumSize) {
                bestEstimate = tmp;
                bestQuorumSize = t;
            }

            // if this is less than 8 points then repeat
        }
        
        if (bestQuorumSize > 5) {
            return bestEstimate;
        }
        
        // if we get here we don't deem it likely that we will get a good
        // answer - so give up.
        return null;
    }
    
}
