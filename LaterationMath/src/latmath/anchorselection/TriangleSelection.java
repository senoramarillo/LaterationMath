package latmath.anchorselection;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import latmath.algorithm.LaterationAlgorithm;
import latmath.util.Point2d;
import latmath.util.Releasable;
import latmath.util.Combinations;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

// Why is this from location filter?
import latmath.location.filter.LocationFilter;
import latmath.util.dialog.ConfigDialog;

/**
 * Triangle based anchor selection.
 *
 * Select anchors based on consistent triangles.
 *
 * @version 1.0, 2012-03-08
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class TriangleSelection implements AnchorSelection, Releasable {

    private transient File logFileName;

    private transient PrintWriter logStream;

    private int minAnchors = 3;

    private static final String selectionName = "TRIANGLE";
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
	StringBuilder b = new StringBuilder();
	b.append(selectionName);
	b.append('(');
	b.append(minAnchors);
	b.append(')');
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

    private AnchorSelectionResult doSelect(Point2d[] anchors,
						 double[] ranges) {
        if (anchors.length != ranges.length && anchors.length <= 3) {
            return null;
        }

        // Number of times an anchor is involved in an inconsistent
        // pairing
        int[] votes = new int[anchors.length];

	// Check for consistent triangle
	for (int[] comb : new Combinations(anchors.length, 2)) {
            final double d01 = anchors[comb[0]].distance(anchors[comb[1]]);
	    final double r0 = ranges[comb[0]];
	    final double r1 = ranges[comb[1]];
            if (Math.max(r0, r1) <= d01 + Math.min(r0, r1)) {
		// inconsistent triangle.
                votes[comb[0]]++;
                votes[comb[1]]++;
            }
        }

	// Count the zeroes
	int zeroes = 0;
	for (int v: votes)
            if (v == 0) zeroes++;

	// Add anchors with minimum number of votes and shortest range to
	// get at least three anchors.
	while (zeroes < Math.min(anchors.length, minAnchors)) {
	    int p = -1;
	    int v = Integer.MAX_VALUE;
	    double r = Double.MAX_VALUE;
	    for (int i = 0; i < votes.length; i++) {
		if (votes[i] > 0 && votes[i] <= v && ranges[i] < r) {
		    p = i;
		    v = votes[p];
		    r = ranges[p];
		}
	    }
	    assert p != -1;
	    votes[p] = 0;
	    zeroes++;
	}
        Point2d[] a = new Point2d[zeroes];
        double[] md = new double[zeroes];
	int j = 0;
        for (int i = 0; i < zeroes; ++i) {
            while (votes[j] > 0) ++j;
	    assert j < votes.length && votes[j] == 0;
            a[i] = anchors[j];
            md[i] = ranges[j];
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

        final JPanel content = new JPanel();
        content.add(minPanel);

        final ConfigDialog dialog = new ConfigDialog(parent, true);

        dialog.setContent(content);
        dialog.setOKAction(new ActionListener() {
                @Override
		public void actionPerformed(ActionEvent e) {
		    minAnchors = (Integer) minSpinner.getValue();
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
