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
public class DistanceBasedAnchorSelection implements AnchorSelection, Releasable {

    private double threshold = 4;

    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
        
    private class AnchorAndDistance implements Comparable {
        public Point2d anchor;
        public double distance;
        public double distanceOffset;

        private AnchorAndDistance(Point2d anchor, double distance, double distanceOffset) {
            this.anchor = anchor;
            this.distance = distance;
            this.distanceOffset = distanceOffset;
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(distanceOffset, ((AnchorAndDistance)o).distanceOffset);
        }
        
    }

    @Override
    public String getName() {
        return threshold == 0 ? "SORT-DIST" : "SORT-DIST T-" + threshold;
    }

    @Override
    public void deriveLogFilename(int uid, LaterationAlgorithm algorithm, LocationFilter filter) {
        // no logging needed
    }

    @Override
    public AnchorSelectionResult select(Point2d[] anchors, double[] measuredDistances, Point2d lastLocation) {
        AnchorAndDistance[] tmp = new AnchorAndDistance[anchors.length];
        for (int i = 0; i < anchors.length; i++) {
            double dist = lastLocation != null ? anchors[i].distance(lastLocation) : 0;
            tmp[i] = new AnchorAndDistance(anchors[i], measuredDistances[i], Math.abs(measuredDistances[i] - dist));
        }
        Arrays.sort(tmp);
        int c = 0;
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].distanceOffset < threshold) {
                c++;
            }
        }
        if (c < 3) {
            c = tmp.length;
        }
        Point2d[] a = new Point2d[c];
        double[] md = new double[c];
        for (int i = 0; i < c; i++) {
            a[i] = tmp[i].anchor;
            md[i] = tmp[i].distance;
        }
        return new AnchorSelectionResult(a, md);
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
        spinner.setModel(new SpinnerNumberModel(threshold, 0.0, 1000.0, 1.0));
        JLabel label = new JLabel("Set threshold:");
        content.add(label);
        content.add(spinner);

        final ConfigDialog dialog = new ConfigDialog(
                parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                threshold = (Double) spinner.getValue();
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
