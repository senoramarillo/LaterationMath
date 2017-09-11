package latmath.algorithm;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.Releasable;
import latmath.util.StandardDeviation;
import latmath.util.WelfordStandardDeviation;
import latmath.util.dialog.ConfigDialog;

/**
 * Min-Max algorithm with triangle weighing.
 *
 * @version 1.00, 2013-06-10
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class MDMinMax extends BasicLaterationAlgorithm implements Releasable {

    /** Weighting attributes */
    private double[] weightingAttrs;

    /** For accessing the four attributes in the array. */
    private static final int LOW    = 0;
    private static final int MEAN_X = 1;
    private static final int MEAN_Y = 2;
    private static final int UP     = 3;

    /** Names for config dialog */
    private static final String[] ATTR_NAMES = new String[] {
        "Low", "Mean X", "Mean Y", "Up"
    };

    private static final double EPSILON = Math.pow(2, -24);

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    public MDMinMax() {
        weightingAttrs = new double[4];
        weightingAttrs[LOW] = -2.16;        // 0.5% quantile
        weightingAttrs[MEAN_X] = -0.349;    // X mode value
        weightingAttrs[MEAN_Y] = 1.0;       // Y mode value
        weightingAttrs[UP] = 16.005;        // 99.5% quantile
    }

    @Override
    public String getName() {
        return "MD-MIN-MAX";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, weightingAttrs);
    }

    /**
     * Returns a string representation of this lateration algorithm.
     *
     * @return A string representation of this lateration algorithm.
     */
    @Override
    public String toString() {
	StringBuilder name = new StringBuilder(getName());
	name.append(" [");
	name.append(weightingAttrs[LOW]);
	name.append("; ");
	name.append(weightingAttrs[MEAN_X]);
	name.append("; ");
        name.append(weightingAttrs[MEAN_Y]);
	name.append("; ");
	name.append(weightingAttrs[UP]);
	name.append(']');
        return name.toString();
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean configure(Frame parent) {
        // Build JPanel with dialog content
        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.PAGE_AXIS));
        final JSpinner[] spTmpRef = new JSpinner[ATTR_NAMES.length];
        for (int i = 0; i < ATTR_NAMES.length; i++) {
            JPanel tmp = new JPanel();
            tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
            spTmpRef[i] = new JSpinner();
            spTmpRef[i].setModel(new SpinnerNumberModel(weightingAttrs[i], -1000, 1000, 0.1));
            Dimension d = spTmpRef[i].getPreferredSize();
            d.width = 60;
            spTmpRef[i].setPreferredSize(d);
            JPanel lContainer = new JPanel();
            lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
            lContainer.setPreferredSize(new Dimension(110, 30));
            JLabel label = new JLabel(ATTR_NAMES[i] + ":");
            lContainer.add(label);
            tmp.add(lContainer);
            tmp.add(spTmpRef[i]);
            content.add(tmp);
        }

        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < ATTR_NAMES.length; i++) {
                    weightingAttrs[i] = (Double) spTmpRef[i].getValue();
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
     * @param weightingAttrs The attributes that characterise the triangle
     *                       used for weighing.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges, double[] weightingAttrs) {
        // step 0: sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

	double left, right, top, bottom;
	left = anchors[0].x - ranges[0];
        right = anchors[0].x + ranges[0];
        top = anchors[0].y + ranges[0];
        bottom = anchors[0].y - ranges[0];
	
        for (int i = 1; i < anchors.length; ++i) {
            left = Math.max(left, anchors[i].x - ranges[i]);
            right = Math.min(right, anchors[i].x + ranges[i]);
            top = Math.min(top, anchors[i].y + ranges[i]);
            bottom = Math.max(bottom, anchors[i].y - ranges[i]);
        }

        double tmp;
        if (left > right) {
            tmp = left;
            left = right;
            right = tmp;
        }
        if (bottom > top) {
            tmp = bottom;
            bottom = top;
            top = tmp;
        }

        Point2d[] comGrid = new Point2d[] {
            new Point2d(left, top),
            new Point2d(left, bottom),
            new Point2d(right, top),
            new Point2d(right, bottom)
        };
        double[] comWeights = new double[comGrid.length];
        
        // Define low, mean and upper triangle points
        Point2d l = new Point2d(weightingAttrs[LOW], 0);
        Point2d m = new Point2d(weightingAttrs[MEAN_X], weightingAttrs[MEAN_Y]);
        Point2d u = new Point2d(weightingAttrs[UP], 0);
        
        // Define up and down lines in form of y = m*x + n
        double mUp = (m.y - l.y) / (m.x - l.x);
        double nUp = l.y - mUp * l.x;
        double mDown = (u.y - m.y) / (u.x - m.x);
        double nDown = m.y - mDown * m.x;
        
        StandardDeviation stddev = new WelfordStandardDeviation();
        for (int i = 0; i < comGrid.length; i++) {
            stddev.reset();
            for (int k = 0; k < anchors.length; k++) {
                tmp = ranges[k] - comGrid[i].distance(anchors[k]);
                double w = EPSILON;
                if (weightingAttrs[LOW] < tmp && tmp < weightingAttrs[MEAN_X]) {
                    w = mUp * tmp + nUp;
                } else if (weightingAttrs[MEAN_X] <= tmp && tmp < weightingAttrs[UP]) {
                    w = mDown * tmp + nDown;
                }
                stddev.addSample(w);
            }
            double sdev = stddev.standardDeviation();
            comWeights[i] = stddev.mean() / (sdev > EPSILON ? sdev : EPSILON);
        }
	return Point2d.centerOfMass(comGrid, comWeights);
    }
    
}
