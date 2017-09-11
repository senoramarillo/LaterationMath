package latmath.ranging.filter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Ranging filter which does offset correction on ranging values.
 *
 * @version 1.0, 2013-02-11
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class OffsetCorrectionRangingFilter implements RangingFilter, Releasable {

    private double offset = 3.04;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "RF-OFFSET ("+offset+")";
    }

    @Override
    public double[] filter(double[] measuredDistances, double[] realDistances, long timestamp) {
        double[] result = new double[measuredDistances.length];
        for (int i = 0; i < measuredDistances.length; i++) {
            if (measuredDistances[i] >= 0) {
                result[i] = Math.max(measuredDistances[i] - offset, 0.01);
            } else {
                result[i] = -1.0;
            }
        }
        return result;
    }

    @Override
    public void reset() {}

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean configure(Frame parent) {
        // Build JPanel with dialog content
        JPanel content = new JPanel();
        content.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        final JSpinner offsetSpinner = new JSpinner();
        offsetSpinner.setModel(new SpinnerNumberModel(offset, -1000.0, 1000.0, 0.1));
        Dimension d = offsetSpinner.getPreferredSize();
        d.width = 60;
        offsetSpinner.setPreferredSize(d);
        JLabel label = new JLabel("Offset correction:");
        content.add(label);
        content.add(offsetSpinner);

        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                offset = (Double) offsetSpinner.getValue();
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

