package latmath.location.filter;

import latmath.util.dialog.ConfigDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.util.Point2d;
import latmath.util.Releasable;

/**
 * Location filter based on curve fitting.
 *
 * @version 1.0, 2012-01-29
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class CurveFittingLocationFilter implements LocationFilter, Releasable {

    private int size;
    private transient List<PositionTime> list;

    private transient double sumX;
    private transient double sumY;
    private transient double sumXTimesY;
    private transient double sumXSquared;

    /** Internal global variables, don't need reset */
    private transient double m;
    private transient double b;

    private static final int DEFAULT_SIZE = 40;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    private class PositionTime {

        private Point2d location;
        private long timestamp;

        public PositionTime(Point2d location, long timestamp) {
            this.location = location;
            this.timestamp = timestamp;
        }

        public Point2d getLocation() {
            return location;
        }

        public long getTimestamp() {
            return timestamp;
        }

    }

    public CurveFittingLocationFilter() {
        size = DEFAULT_SIZE;
        list = new ArrayList<>();
    }
    
    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();
            list = new ArrayList<>();
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Returns current direction vector scaled by velocity.
     *
     * @return Current direction vector or {@code null} if too few point are in
     *         the filter or no timestamps are available.
     */
    public Point2d getScaledDirectionVector() {
        int n = list.size();

        if (n <= 1) {
            return null;
        }

        // Get first and last position, check for timestamps
        PositionTime ptFirst = list.get(0);
        PositionTime ptLast = list.get(n-1);
        if (ptFirst.getTimestamp() == -1 || ptLast.getTimestamp() == -1) {
            return null;
        }

        // Calculate time between first and last point in seconds
        double time = (ptLast.getTimestamp() - ptFirst.getTimestamp()) / 1000.0;

        // Update line equation with new position data
        updateLineEquation(n);

        // Project positions onto current line
        Point2d pFirst = getPerpendicularFoot(ptFirst.getLocation(), m, b);
        Point2d pLast = getPerpendicularFoot(ptLast.getLocation(), m, b);

        Point2d vec = new Point2d();
        vec.x = (pLast.x - pFirst.x) / time;
        vec.y = (pLast.y - pFirst.y) / time;
        return vec;
    }

    @Override
    public String getName() {
        return "CURVE-FITTING";
    }

    @Override
    public void add(Point2d location, long timestamp) {
        if (location == null) {
            return;
        }
        list.add(new PositionTime(location, timestamp));
        if (list.size() > size) {
            Point2d p = list.remove(0).getLocation();
            sumX -= p.x;
            sumY -= p.y;
            sumXTimesY -= (p.x * p.y);
            sumXSquared -= (p.x * p.x);
        }
        sumX += location.x;
        sumY += location.y;
        sumXTimesY += (location.x * location.y);
        sumXSquared += (location.x * location.x);
    }

    @Override
    public Point2d get() {
        int n = list.size();

        if (n == 0) {
            return null;
        }

        if (n == 1) {
            return list.get(0).getLocation();
        }

        // Update line equation with new position data
        updateLineEquation(n);

        // Find the perpendicular foot of line and the
        // last point added to the filter
        return getPerpendicularFoot(list.get(n-1).getLocation(), m, b);
    }

    /**
     * Updates the line equation.
     *
     * @param n The number of positions in the filter.
     */
    private void updateLineEquation(int n) {
        double meanX = sumX / n;
        double meanY = sumY / n;
        // Calculate slope "m" of the fitted line
        m = (n * sumXTimesY - sumX * sumY) / (n * sumXSquared - sumX * sumX);
        // Calculate intercept "b" of the fitted line
        b = meanY - (m * meanX);
    }

    /**
     * Gets the foot of the perpendicular line of the given line.
     * 
     * @param p Point on the perpendicular line.
     * @param m The slope of the given line.
     * @param b The intercept of the given line.
     *
     * @return The foot of the perpendicular line.
     */
    private Point2d getPerpendicularFoot(Point2d p, double m, double b) {
        Point2d foot = new Point2d();
        foot.x = (m * p.y + p.x - m * b) / (m*m + 1);
        foot.y = (m * m * p.y + m * p.x + b) / (m*m + 1);
        return foot;
    }

    @Override
    public void reset() {
        sumX = 0;
        sumY = 0;
        sumXTimesY = 0;
        sumXSquared = 0;
        list.clear();
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
        spinner.setModel(new SpinnerNumberModel(size, 10, 1000, 1));
        JLabel label = new JLabel("Set filter size:");
        content.add(label);
        content.add(spinner);

        final ConfigDialog dialog = new ConfigDialog(
                parent, true);
        
        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                size = (Integer) spinner.getValue();
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
