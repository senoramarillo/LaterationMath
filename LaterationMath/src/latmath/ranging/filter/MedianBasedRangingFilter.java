package latmath.ranging.filter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.filter.MedianFilter;
import latmath.util.dialog.ConfigDialog;
import latmath.util.Releasable;

/**
 * Ranging filter based on median filtering.
 *
 * @version 1.0, 2012-02-27
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class MedianBasedRangingFilter implements RangingFilter, Releasable {

    /** Filter size */
    private int filterSize = 5;

    /** Flush limit of filter */
    private int filterFlushLimit = 7;

    /** Array of median filters for every anchor */
    private transient MedianFilter[] medianFilter;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "RF-MEDIAN (" + filterSize + "," + filterFlushLimit + ")";
    }

    @Override
    public double[] filter(double[] measuredDistances, double[] realDistances, long timestamp) {
        if (medianFilter == null) {
            medianFilter = new MedianFilter[measuredDistances.length];
            for (int i = 0; i < medianFilter.length; i++) {
                medianFilter[i] = new MedianFilter(filterSize, filterFlushLimit);
            }
        }
        double[] result = new double[measuredDistances.length];
        for (int i = 0; i < measuredDistances.length; i++) {
            if (measuredDistances[i] == -1) {
                medianFilter[i].incFlush();
            } else {
                medianFilter[i].add(measuredDistances[i]);
            }
            result[i] = medianFilter[i].getMedian4();
        }
        return result;
    }

    @Override
    public void reset() {
        medianFilter = null;
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
        final JSpinner meSpinner = new JSpinner();
        meSpinner.setModel(new SpinnerNumberModel(this.filterSize, 1, 9999, 1));
        Dimension d = meSpinner.getPreferredSize();
        d.width = 60;
        meSpinner.setPreferredSize(d);
        JLabel label = new JLabel("Set filter size:");
        content.add(label);
        content.add(meSpinner);

        final JSpinner moSpinner = new JSpinner();
        moSpinner.setModel(new SpinnerNumberModel(this.filterFlushLimit, 1, 9999, 1));
        d = moSpinner.getPreferredSize();
        d.width = 60;
        moSpinner.setPreferredSize(d);
        label = new JLabel("Set flush limit:");
        content.add(label);
        content.add(moSpinner);

        final ConfigDialog dialog = new ConfigDialog(
                parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterFlushLimit = (Integer) moSpinner.getValue();
                filterSize = (Integer) meSpinner.getValue();
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
