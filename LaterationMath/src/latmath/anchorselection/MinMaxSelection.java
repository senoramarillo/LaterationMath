package latmath.anchorselection;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import latmath.algorithm.LaterationAlgorithm;
import latmath.util.Point2d;
import latmath.util.PointAndRange;
import latmath.util.Releasable;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

// Why is this from location filter?
import latmath.location.filter.LocationFilter;
import latmath.util.dialog.ConfigDialog;

/**
 * Min-Max based anchor selection. Select anchors that would be used
 * for calculating the position in the min-max case.
 *
 * @version 1.0, 2012-02-07
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class MinMaxSelection implements AnchorSelection, Releasable {

    private transient File logFileName;

    private transient PrintWriter logStream;

    private int minAnchors = 3;
    private int maxAnchors = 7;

    private static final String selectionName = "MIN-MAX";
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
	StringBuilder b = new StringBuilder();
	b.append(selectionName);
	b.append("-");
	b.append(minAnchors);
	b.append("-");
	b.append(maxAnchors);
	return b.toString();
    }

    public void setLogFileName(File name) {
	if (logFileName != null) {
	    return;
	}
	logFileName = name;
	try {
	    FileOutputStream stream = new FileOutputStream(logFileName);
	    logStream = new PrintWriter(stream, true);
	} catch (IOException e) {
	    System.err.println(e.toString());
	    logStream = null;
	    logFileName = null;
	}
    }

    @Override
    public void deriveLogFilename(int uid,
				  LaterationAlgorithm algorithm,
				  LocationFilter filter) {
	StringBuilder b = new StringBuilder();
	b.append("log-");
	b.append(uid);
	b.append('-');
	b.append(algorithm.getName().replaceAll(Pattern.quote("|"), "-"));
        if (filter != null) {
            b.append('-');
            b.append(filter.getName());
        }
	b.append(".log");
	this.setLogFileName(new File(b.toString()));
    }

    @Override
    public void finalize() {
	if (logStream != null) {
	    logStream.close();
	    logStream = null;
	}
    }

    private void contributingAnchors(Set<PointAndRange> anchors,
					   Set<PointAndRange> selection) {
	PointAndRange[] cn = new PointAndRange[8];

        // initialise with some element
        Arrays.fill(cn, anchors.iterator().next());

        // Figure out the contributing anchros from the current selection
        // set.
        for (PointAndRange pr : anchors) {
	    // contributing to p[0]
	    if (cn[0].point.x + cn[0].range > pr.point.x + pr.range)
	        cn[0] = pr;
	    if (cn[1].point.y + cn[1].range > pr.point.y + pr.range)
	        cn[1] = pr;
	    // contributing to p[1]
	    if (cn[2].point.x - cn[2].range < pr.point.x - pr.range)
	        cn[2] = pr;
	    if (cn[3].point.y + cn[3].range > pr.point.y + pr.range)
	        cn[3] = pr;
	    // contributing to p[2]
	    if (cn[4].point.x + cn[4].range > pr.point.x + pr.range)
	        cn[4] = pr;
	    if (cn[5].point.y - cn[5].range < pr.point.y - pr.range)
	        cn[5] = pr;
	    // contributing to p[3]
	    if (cn[6].point.x - cn[6].range < pr.point.x - pr.range)
	        cn[6] = pr;
	    if (cn[7].point.y - cn[7].range < pr.point.y - pr.range)
	        cn[7] = pr;
        }
        // Now remove the selected ones from the set of available
        // anchors and add them to the selection, until maxAnchors
        // has been reached.
        for (PointAndRange pr : cn) {
            anchors.remove(pr);
            selection.add(pr);
            if (selection.size() > maxAnchors)
                return;
        }
    }

    private AnchorSelectionResult doSelect(Point2d[] anchors,
						 double[] ranges) {
        if (anchors.length != ranges.length) {
            return null;
        }

        Set<PointAndRange> workset = new TreeSet<>();
        Set<PointAndRange> selection = new TreeSet<>();

        for (int i = 0; i < anchors.length; ++i)
            workset.add(new PointAndRange(anchors[i], ranges[i]));

        final int minAnchors = Math.min(this.minAnchors, anchors.length);

	while (selection.size() < minAnchors) {
	    contributingAnchors(workset, selection);
	}

        Point2d[] a = new Point2d[selection.size()];
        double[] md = new double[selection.size()];
	int j = 0;
        for (PointAndRange pr : selection) {
            a[j] = pr.point;
            md[j] = pr.range;
	    ++j;
        }

	return new AnchorSelectionResult(a, md);
    }

    
    @Override
    public AnchorSelectionResult select(Point2d[] anchors,
					double[] ranges,
					Point2d lastLocation) {
	AnchorSelectionResult result = doSelect(anchors, ranges);
	// Print out result for debugging.
	if (logStream != null) {
	    logStream.println(result);
	}
	return result;
    }

    @Override
    public AnchorSelectionResult select(Point2d[] anchors,
					double[] ranges,
					Point2d lastLocation,
					long ts) {
	AnchorSelectionResult result = doSelect(anchors, ranges);
	// Print out result for debugging.
	if (logStream != null) {
	    logStream.println(Long.toString(ts) + " " + result);
	}
	return result;
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
        final JPanel minPanel = new JPanel();
        minPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        final JSpinner minSpinner = new JSpinner();
        minSpinner.setModel(new SpinnerNumberModel(minAnchors, 3, 100, 1));
        minPanel.add(new JLabel("Minimum number of anchors:"));
        minPanel.add(minSpinner);

        final JPanel maxPanel = new JPanel();
        maxPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        final JSpinner maxSpinner = new JSpinner();
        maxSpinner.setModel(new SpinnerNumberModel(maxAnchors, 4, 100, 1));
        maxPanel.add(new JLabel("Maximum number of anchors:"));
        maxPanel.add(maxSpinner);

        final JPanel content = new JPanel();
        content.add(minPanel);
        content.add(maxPanel);

        final ConfigDialog dialog = new ConfigDialog(parent, true);

        dialog.setContent(content);
        dialog.setOKAction(new ActionListener() {
                @Override
		public void actionPerformed(ActionEvent e) {
		    minAnchors = (Integer) minSpinner.getValue();
		    maxAnchors = (Integer) maxSpinner.getValue();
		    if (maxAnchors < minAnchors) {
			maxAnchors = minAnchors;
		    }
		    dialog.dispose();
		}
	    });
        dialog.setCancelAction(new ActionListener() {
                @Override
		public void actionPerformed(ActionEvent e) {
		    dialog.dispose();
		}
	    });
        dialog.showDialog("Edit " + selectionName + " properties", false);
        return dialog.getDialogResult();
    }
}
