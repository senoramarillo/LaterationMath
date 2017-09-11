package latmath.ranging.filter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.filter.SavitzkyGolayFilter;
import latmath.util.dialog.ConfigDialog;
import latmath.util.Releasable;

/**
 * Ranging filter based on Savitzky-Golay filter.
 *
 * @version 1.0, 2012-05-04
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class SavitzkyGolayBasedRangingFilter implements RangingFilter, Releasable {

    /** Filter size */
    private int filterSize = 11;

    /** Flush limit of filter */
    private int filterFlushLimit = 5;

    /** Degree of the fitting polynomial */
    private int filterDegree = 2;

    /** Array of Savitzky-Golay filters for every anchor */
    private transient SavitzkyGolayFilter[] sgFilter;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "RF-SAVITZKY-GOLAY (" + filterSize + "," + filterFlushLimit + "," + filterDegree + ")";
    }

    @Override
    public double[] filter(double[] measuredDistances, double[] realDistances, long timestamp) {
        if (sgFilter == null) {
            sgFilter = new SavitzkyGolayFilter[measuredDistances.length];
            for (int i = 0; i < sgFilter.length; i++) {
                sgFilter[i] = new SavitzkyGolayFilter(filterDegree, filterSize, filterFlushLimit);
            }
        }
        double[] result = new double[measuredDistances.length];
        for (int i = 0; i < measuredDistances.length; i++) {
            if (measuredDistances[i] == -1) {
                sgFilter[i].incFlush();
            } else {
                sgFilter[i].add(measuredDistances[i]);
            }
            result[i] = sgFilter[i].get();
        }
        return result;
    }

    @Override
    public void reset() {
        sgFilter = null;
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

        final JSpinner mdSpinner = new JSpinner();
        mdSpinner.setModel(new SpinnerNumberModel(this.filterDegree, 2, 10, 1));
        d = mdSpinner.getPreferredSize();
        d.width = 60;
        mdSpinner.setPreferredSize(d);
        label = new JLabel("Set fitting degree:");
        content.add(label);
        content.add(mdSpinner);

        final ConfigDialog dialog = new ConfigDialog(
                parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((Integer) meSpinner.getValue() <= (Integer) mdSpinner.getValue()) {
                    JOptionPane.showMessageDialog(dialog, "Fitting degree must be smaller than filter size!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                filterFlushLimit = (Integer) moSpinner.getValue();
                filterSize = (Integer) meSpinner.getValue();
                filterDegree = (Integer) mdSpinner.getValue();
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
