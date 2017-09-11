package latmath.ranging.filter;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.errormodel.ErrorModelNanopan5375;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Ranging filter which simulates Nanotron ranging error.
 *
 * @version 1.0, 2012-05-10
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class NTErrorRangingFilter implements RangingFilter, Releasable {

    private ErrorModelNanopan5375 nanoError;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    public NTErrorRangingFilter() {
        this.nanoError = new ErrorModelNanopan5375(30);
        this.nanoError.setNegativeOffsetsEnabled(true);
    }

    @Override
    public String getName() {
        return "RF-NANOPAN-SIM";
    }

    @Override
    public double[] filter(double[] measuredDistances, double[] realDistances, long timestamp) {
        double[] result = new double[measuredDistances.length];
        for (int i = 0; i < measuredDistances.length; i++) {
            if (realDistances == null) {
                result[i] = -1;
            } else {
                result[i] = measuredDistances[i] != -1 ? realDistances[i] + nanoError.getOffset(realDistances[i], 0) : -1;
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
        final JSpinner meSpinner = new JSpinner();
        meSpinner.setModel(new SpinnerNumberModel(nanoError.getMaximumAllowedError(), 1.0, 9999.0, 1.0));
        Dimension d = meSpinner.getPreferredSize();
        d.width = 60;
        meSpinner.setPreferredSize(d);
        JLabel label = new JLabel("Set max. error:");
        content.add(label);
        content.add(meSpinner);

        final JCheckBox cb = new JCheckBox("Allow negative offsets");
        cb.setSelected(nanoError.isNegativeOffsetsEnabled());
        content.add(cb);

        final ConfigDialog dialog = new ConfigDialog(
                parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double max = (Double) meSpinner.getValue();
                nanoError.setMaximumAllowedError(max);
                nanoError.setNegativeOffsetsEnabled(cb.isSelected());
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

