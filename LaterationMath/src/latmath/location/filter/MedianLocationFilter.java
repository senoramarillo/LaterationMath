package latmath.location.filter;

import java.util.Queue;
import java.util.Arrays;
import java.util.ArrayDeque;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import latmath.util.dialog.ConfigDialog;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Location filter based on Median filter.
 *
 * @version 1.0, 2014-03-25
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class MedianLocationFilter implements LocationFilter, Releasable {

    private int windowSize;
    private transient Queue<Double> windowX;
    private transient Queue<Double> windowY;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    // Must have constructor with no params!
    public MedianLocationFilter() {
        windowSize = 10;
	windowX = new ArrayDeque(windowSize);
	windowY = new ArrayDeque(windowSize);
    }

    @Override
    public String getName() {
        return "MEDIAN-" + Integer.toString(windowSize);
    }

    @Override
    public void add(Point2d loc, long timestamp) {
        if (loc == null) {
            return;
        }
	while (windowX.size() >= this.windowSize) {
	    windowX.poll();
	    windowY.poll();
	}
	windowX.add(loc.x);
	windowY.add(loc.y);
    }

    @Override
    public Point2d get() {
	while (windowX.size() > this.windowSize) {
	    windowX.poll();
	    windowY.poll();
	}
	if (this.windowX.size() > 0) {
	    Double[] array = new Double[this.windowX.size()];
	    windowX.toArray(array);
	    Arrays.sort(array);
	    double x = array[windowX.size() / 2];
	    windowY.toArray(array);
	    Arrays.sort(array);
	    double y = array[windowY.size() / 2];
	    return new Point2d(x, y);
	} else {
	    return null;
	}	    
    }

    @Override
    public void reset() {
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
        meSpinner.setModel(new SpinnerNumberModel(this.windowSize, 0, Integer.MAX_VALUE, 1));
        Dimension d = meSpinner.getPreferredSize();
        d.width = 80;
        meSpinner.setPreferredSize(d);
        JLabel label = new JLabel("Set window size:");
        content.add(label);
        content.add(meSpinner);

        final ConfigDialog dialog = new ConfigDialog(
                parent, true);
        
        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                windowSize = (Integer) meSpinner.getValue();
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
