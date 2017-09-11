package latmath.weighting;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.util.Point2d;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;
import org.apache.commons.math3.special.Gamma;

/**
 * Weighting method based on a given Gamma distribution.
 * 
 * @version 1.0, 2013-05-12
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class GammaWeigher implements Releasable, Weighable {

    private double shape = 3.3;
    private double rate = 0.576;
    private double offset = 3.31060119642765;
    private double gamma;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
        
    public GammaWeigher() {
        init();
    }
    
    private void init() {
        gamma = Math.pow(rate, shape) / Gamma.gamma(shape);
    }
    
    @Override
    public String getName() {
        return "Gamma Weigher";
    }

    @Override
    public double weigh(Point2d position, Point2d[] anchors, double[] ranges) {
        double result = 1.0;
        for (int i = 0; i < anchors.length; i++) {
            double distance = position.distance(anchors[i]);
            double x = ranges[i] + offset - distance;
            if (x <= 0.0) {
                return 0.0;
            }
            double probability = gamma * Math.pow(x, shape - 1.0)
                    * Math.exp(-rate * x);
            result *= probability;
        }
        return result;
    }
    
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
        final JSpinner spRate = new JSpinner();
        spRate.setModel(new SpinnerNumberModel(this.rate, 0.0, 1000.0, 0.1));
        Dimension d = spRate.getPreferredSize();
        d.width = 60;
        spRate.setPreferredSize(d);
        // Create label and add control
        JPanel lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        JLabel label = new JLabel("Gamma distribution, rate:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spRate);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner spShape = new JSpinner();
        spShape.setModel(new SpinnerNumberModel(this.shape, 0.0, 1000.0, 1.0));
        d = spShape.getPreferredSize();
        d.width = 60;
        spShape.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Gamma distribution, shape:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spShape);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner spOffset = new JSpinner();
        spOffset.setModel(new SpinnerNumberModel(this.offset, -1000.0, 1000.0, 0.1));
        d = spOffset.getPreferredSize();
        d.width = 60;
        spOffset.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Gamma distribution, offset:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spOffset);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                offset = (Double) spOffset.getValue();
                shape = (Double) spShape.getValue();
                rate = (Double) spRate.getValue();
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
