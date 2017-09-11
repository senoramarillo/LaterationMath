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
 * @version 1.0, 2014-03-26
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class GeometricMedianLocationFilter implements LocationFilter, Releasable {

    private int windowSize = 10;
    private int resetSize = 5;
    private transient int currentResetSize = 0;
    private transient Queue<Point2d> window;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    // Must have constructor with no params!
    public GeometricMedianLocationFilter() {
	this.window = new ArrayDeque<Point2d>(windowSize);
    }

    @Override
    public String getName() {
        return "GEOMETRIC-MEDIAN-" + Integer.toString(windowSize);
    }

    @Override
    public void add(Point2d loc, long timestamp) {
        if (loc == null) {
            ++currentResetSize;
            if (this.currentResetSize == this.resetSize) {
                currentResetSize = 0;
                window.clear();
            }
            return;
        }
	if (window.size() == this.windowSize) {
	    window.poll();
	}
	window.add(loc);
    }

    @Override
    public Point2d get() {
	if (this.window.size() > 0) {
            Point2d[] points = new Point2d[window.size()];
            window.toArray(points);
            return Point2d.geometricMedian(points);
	} else {
	    return null;
	}
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public boolean configure(Frame parent) {
	return false;
    }

}
