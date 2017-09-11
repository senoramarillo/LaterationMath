package latmath.anchorselection;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.algorithm.LaterationAlgorithm;
import latmath.location.filter.LocationFilter;
import latmath.util.dialog.ConfigDialog;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Random anchor selection algorithm.
 *
 * @version 1.0, 2013-08-10
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class RandomAnchorSelection implements AnchorSelection, Releasable {

    /** Number of anchors to select */
    private int count = 3;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
        
    @Override
    public String getName() {
        return "RANDOM (n=" + count + ")";
    }

    @Override
    public void deriveLogFilename(int uid, LaterationAlgorithm algorithm,
            LocationFilter filter) {
        // no logging needed
    }

    @Override
    public AnchorSelectionResult select(Point2d[] anchors, double[]
            measuredDistances, Point2d lastLocation) {
        
        // do we have to select?
        if (anchors.length <= count) {
            return new AnchorSelectionResult(anchors, measuredDistances);
        }
        
        int[] points = new int[count];
        Random rand = new Random();
        
        // select count anchors at random making
        // sure we have no collisions
        for (int i = 0; i < count; ++i) {
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
        
        Point2d[] a = new Point2d[count];
        double[] md = new double[count];
        for (int i = 0; i < count; i++) {
            a[i] = anchors[points[i]];
            md[i] = measuredDistances[points[i]];
        }
        return new AnchorSelectionResult(a, md);
    }

    @Override
    public AnchorSelectionResult select(Point2d[] anchors, double[]
            measuredDistances, Point2d lastLocation, long timestamp) {
        return select(anchors, measuredDistances, lastLocation);
    }

    @Override
    public void reset() {
        // do nothing
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean configure(Frame parent) {
        // Build JPanel with dialog content
        JPanel content = new JPanel();
        content.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        final JSpinner spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(count, 3, 1000, 1));
        JLabel label = new JLabel("Set count:");
        content.add(label);
        content.add(spinner);

        final ConfigDialog dialog = new ConfigDialog(
                parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                count = (Integer) spinner.getValue();
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

}
