package latmath.anchorselection;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
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
 * Simple implementation of anchor selection algorithm based on measured distances.
 *
 * @version 1.0, 2012-02-07
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class BoundedAnchorSelection implements AnchorSelection, Releasable {

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    private double bound = 30;
        
    @Override
    public String getName() {
        return "BOUND-DIST " + bound;
    }

    @Override
    public void deriveLogFilename(int uid, LaterationAlgorithm algorithm, LocationFilter filter) {
        // no logging needed
    }

    @Override
    public AnchorSelectionResult select(Point2d[] anchors, double[] measuredDistances, Point2d lastLocation) {
	int num = 0;
        for (double d: measuredDistances) {
	    if (d <= bound) {
		++num;
	    }
	}
	Point2d[] pr = new Point2d[num];
	double[] pd = new double[num];
	num = 0;

	for (int i = 0; i < anchors.length; ++i) {
	    if (measuredDistances[i] <= bound) {
		pr[num] = anchors[i];
		pd[num] = measuredDistances[i];
		num++;
	    }
	}
        return new AnchorSelectionResult(pr, pd);
    }

    @Override
    public AnchorSelectionResult select(Point2d[] anchors, double[] measuredDistances, Point2d lastLocation, long timestamp) {
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
        spinner.setModel(new SpinnerNumberModel(bound, 0.0, 1000.0, 1.0));
        JLabel label = new JLabel("Set bound:");
        content.add(label);
        content.add(spinner);

        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bound = (Double) spinner.getValue();
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
