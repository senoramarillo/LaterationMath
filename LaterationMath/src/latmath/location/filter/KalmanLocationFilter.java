package latmath.location.filter;

import latmath.util.dialog.ConfigDialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Location filter based on Kalman filter.
 *
 * @version 1.0, 2012-01-29
 * @author  Heiko Will <hwill@inf.fu-berlin.de>
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class KalmanLocationFilter implements LocationFilter, Releasable {

    private transient double lastx = -1;
    private transient double lasty = -1;
    private transient double statusx = -1;
    private transient double statusy = -1;
    private transient double x, y;
    
    private double model = 0.1;
    private double measure = 0.08;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    // Must have constructor with no params!
    public KalmanLocationFilter() {}

    @Override
    public String getName() {
        return "KALMAN";
    }

    @Override
    public void add(Point2d loc, long timestamp) {
        if (loc == null) {
            return;
        }
        // Filtering
        if (lastx==-1) {
            // Init Position on first run
            x = lastx = loc.x;
            y = lasty = loc.y;
        } else {
            // Init Status on second run
            if (statusx == -1) {
                //var speed = Math.sqrt((lastx-input.tagLocations[i].x)*(lastx-input.tagLocations[i].x) + (lasty-input.tagLocations[i].y)*(lasty-input.tagLocations[i].y));
                statusx =  (loc.x-lastx);
                statusy =  (loc.y-lasty);
            }
            if (statusx != -1) {
                // Apply Filter
                x = lastx + (statusx * this.model) + ((loc.x-lastx) * this.measure);
                y = lasty + (statusy * this.model) + ((loc.y-lasty) * this.measure);  
                statusx = (x-lastx);
                statusy = (y-lasty);
                lastx = x;
                lasty = y;
            }
        }
    }

    @Override
    public Point2d get() {
        return new Point2d(x, y);
    }

    @Override
    public void reset() {
        lastx = -1;
        lasty = -1;
        statusx = -1;
        statusy = -1;
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
        meSpinner.setModel(new SpinnerNumberModel(this.measure, 0.0, 1.0, 0.01));
        Dimension d = meSpinner.getPreferredSize();
        d.width = 80;
        meSpinner.setPreferredSize(d);
        JLabel label = new JLabel("Set measurement trust:");
        content.add(label);
        content.add(meSpinner);

        final JSpinner moSpinner = new JSpinner();
        moSpinner.setModel(new SpinnerNumberModel(this.model, 0.0, 1.0, 0.01));
        d = moSpinner.getPreferredSize();
        d.width = 80;
        moSpinner.setPreferredSize(d);
        label = new JLabel("Set model trust:");
        content.add(label);
        content.add(moSpinner);

        final ConfigDialog dialog = new ConfigDialog(
                parent, true);
        
        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model = (Double) moSpinner.getValue();
                measure = (Double) meSpinner.getValue();
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
