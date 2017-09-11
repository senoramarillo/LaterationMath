package latmath.errormodel;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.distribution.NormalDistribution;
import latmath.util.dialog.ConfigDialog;

/**
 * Error model modeling LOS using a Normal Distribution (LOS).
 *
 * @version 1.00, 2012-10-20
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ErrorModelLos extends BasicErrorModel {

    private NormalDistribution gaussian;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new instance of <code>ErrorModelLos</code>.
     * 
     * @param maximumAllowedError The maximum allowed error.
     * @param mean The mean of the Normal Distribution (LOS).
     * @param sdev The standard deviation of the Normal Distribution (LOS).
     */
    public ErrorModelLos(double maximumAllowedError, double mean, double sdev) {
        super(maximumAllowedError);
        gaussian = new NormalDistribution(mean, sdev);
    }

    /**
     * Get the variance of the Normal Distribution of the error model.
     * 
     * @return The variance of the Normal Distribution of the error model.
     */
    public double getVariance() {
        return gaussian.getStandardDeviation() * gaussian.getStandardDeviation();
    }
    
    @Override
    public String getName() {
        return "LOS";
    }

    @Override
    public double getOffset(double distance, double pNlos) {
        double error = 0;
        do {
            // get random sample form normal distribution (LOS error)
            double x = gaussian.sample();
            // remove negative values if not allowed
            double y = x;
            if (!negativeOffset && x < 0) {
                y = -x;
            }
            // return error if below maximum error allowed
            error = y * bias;
        } while (Math.abs(error) > maximumAllowedError);
        accountOffset(error);
        return error;
    }
    
    /**
     * Returns a string representation of this error model.
     *
     * @return A string representation of this error model.
     */
    @Override
    public String toString() {
        return getName() + " N(" + gaussian.getMean() + "," +
                gaussian.getStandardDeviation() + ")";
    }

    @Override
    public Object clone() {
        ErrorModelLos clone = new ErrorModelLos(maximumAllowedError,
                gaussian.getMean(), gaussian.getStandardDeviation());
        clone.setNegativeOffsetsEnabled(this.isNegativeOffsetsEnabled());
        clone.setScaleFactor(this.getScaleFactor());
        return clone;
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
        JLabel label;
        JPanel tmp, lContainer;
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner meanSpinner = new JSpinner();
        meanSpinner.setModel(new SpinnerNumberModel(this.gaussian.getMean(), 0.0, 1000.0, 1.0));
        Dimension d = meanSpinner.getPreferredSize();
        d.width = 60;
        meanSpinner.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Mean:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(meanSpinner);
        content.add(tmp);
        
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner sdevSpinner = new JSpinner();
        sdevSpinner.setModel(new SpinnerNumberModel(this.gaussian.getStandardDeviation(), 0.0, 1000.0, 1.0));
        d = sdevSpinner.getPreferredSize();
        d.width = 60;
        sdevSpinner.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Standard deviation:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(sdevSpinner);
        content.add(tmp);
        
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double tmpMean = (Double) meanSpinner.getValue();
                double tmpSdev = (Double) sdevSpinner.getValue();
                gaussian = new NormalDistribution(tmpMean, tmpSdev);
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
        dialog.showDialog("Edit \"" + getName() + "\" error model", false);
        return dialog.getDialogResult();
    }

}
