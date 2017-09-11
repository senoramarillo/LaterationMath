package latmath.algorithm;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;
import java.util.Random;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.util.dialog.ConfigDialog;

/**
 * Multilateration using constraint particle filter.
 *
 * @version 1.0, 2014-03-24
 * @author Yubin Zhao <zhaoyubin@inf.fu-berlin.de>
 * @since LatMath 2.0
 */
public final class ConstraintPF extends BasicLaterationAlgorithm implements Releasable {

    /**
     * number of particle
     */
    private int pNum;
    /**
     * the belief factor tune the ranging measurement, which is set as a fixed
     * value
     */
    private double tao;
    /**
     * positive bias Auction: the positive bias is not the real bias. So be
     * careful when you set this value
     */
    private double ave;

    private Point2d pre = null;

    /**
     * Constrain based particle filter. Default set up, which determines the
     * number of anchors, the belief factor, the distribution model (constant
     * bias)
     */
    public ConstraintPF() {
	this(50, 0.8, 0.5);
    }

    /**
     * Set up the main parameter
     */
    public ConstraintPF(int p, double t, double m) {
	pNum = p;
	tao = t;
	ave = m;
    }

    public String getName() {
	return "CPF";
    }

    @Override
    public void reset() {
        super.reset(); //To change body of generated methods, choose Tools | Templates.
        pre = null;
    }

    /**
     * Returns a string representation of this lateration algorithm.
     *
     * @return A string representation of this lateration algorithm.
     */
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
	final JSpinner particleNumbersSpinner = new JSpinner();
	particleNumbersSpinner.setModel(new SpinnerNumberModel(this.pNum, 0, 1000, 1));
	Dimension d = particleNumbersSpinner.getPreferredSize();
	d.width = 60;
	particleNumbersSpinner.setPreferredSize(d);
	// Create label and add control
	JPanel lContainer = new JPanel();
	lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	lContainer.setPreferredSize(new Dimension(lWidth, 30));
	JLabel label = new JLabel("Number of Particles: ");
	lContainer.add(label);
	tmp.add(lContainer);
	tmp.add(particleNumbersSpinner);
	content.add(tmp);

	// Add next control
	tmp = new JPanel();
	tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	// Create control
	final JSpinner trustFactorSpinner = new JSpinner();
	trustFactorSpinner.setModel(new SpinnerNumberModel(this.tao, 0, 10, 0.1));
	d = trustFactorSpinner.getPreferredSize();
	d.width = 60;
	trustFactorSpinner.setPreferredSize(d);
	// Create label and add control
	lContainer = new JPanel();
	lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	lContainer.setPreferredSize(new Dimension(lWidth, 30));
	label = new JLabel("Trust factor:");
	lContainer.add(label);
	tmp.add(lContainer);
	tmp.add(trustFactorSpinner);
	content.add(tmp);

	// Add next control
	tmp = new JPanel();
	tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	// Create control
	final JSpinner aveSpinner = new JSpinner();
	aveSpinner.setModel(new SpinnerNumberModel(this.ave, 0.01, 1000.0, 0.1));
	d = aveSpinner.getPreferredSize();
	d.width = 60;
	aveSpinner.setPreferredSize(d);
	// Create label and add control
	lContainer = new JPanel();
	lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	lContainer.setPreferredSize(new Dimension(lWidth, 30));
	label = new JLabel("AVE:");
	lContainer.add(label);
	tmp.add(lContainer);
	tmp.add(aveSpinner);
	content.add(tmp);

	tmp = new JPanel();
	tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

	// Create label and add control
	lContainer = new JPanel();
	lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
	lContainer.setPreferredSize(new Dimension(lWidth, 30));

	final ConfigDialog dialog = new ConfigDialog(parent, true);

	// Build OK and Cancel actions
	ActionListener okAction = new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		pNum = (Integer) particleNumbersSpinner.getValue();
		tao = (Double) trustFactorSpinner.getValue();
		ave = (Double) aveSpinner.getValue();
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

    public Point2d localize(Point2d[] anchors, double[] ranges, Point2d actualPosition, ErrorModel errorModel, int width, int height) {
	PositionEstimate pe = ConstraintPF.multilaterate(anchors, ranges, this);
	return pe != null ? pe.getLocation() : this.pre;
    }

    public static PositionEstimate multilaterate(Point2d[] anchors, double[] ranges, ConstraintPF cpf) {
	if (ranges.length < 3 || anchors.length != ranges.length) {
	    return null;
	}
	if (cpf == null) {
	    cpf = new ConstraintPF();
	}

	double[] w = new double[cpf.pNum];
	Point2d[] particle;

	particle = constrain(anchors, ranges, cpf);
	if (particle == null) {
	    return null;
	}

	for (int j = 0; j < ranges.length; ++j) {
	    double range_t = cpf.pre.distance(anchors[j]);
	    double range = range_t * cpf.tao + (ranges[j] - cpf.ave) * (1 - cpf.tao);

	    for (int i = 0; i < particle.length; i++) {
		if (particle == null) {
		    continue;
		}
		double t = particle[i].distance(anchors[j]);
		w[i] += (range - t) * (range - t);
	    }
	}

	Point2d pTemp = particle[0];
	double wTemp = w[0];
	for (int i = 1; i < particle.length; i++) {
	    if (wTemp > w[i]) {
		pTemp = particle[i];
		wTemp = w[i];
	    }
	}
	cpf.pre.set(pTemp.x, pTemp.y);

	return new PositionEstimate(pTemp, 0);
    }

    /**
     * This is a particle sampling algorithm. The particles are generated within
     * a constraint region.
     *
     * @return An array of points (Point2d);
     */
    private static Point2d[] constrain(Point2d[] anchors, double[] ranges, ConstraintPF cpf) {
	// Declare north/west/east/south
	double north = 0, west = 0, east = 0, south = 0;

	// Find first anchor which is not null
	north = anchors[0].y - ranges[0];
	west = anchors[0].x - ranges[0];
	east = anchors[0].x + ranges[0];
	south = anchors[0].y + ranges[0];

	for (int i = 1; i < anchors.length; i++) {
	    north = Math.max(north, anchors[i].y - ranges[i]);
	    west = Math.max(west, anchors[i].x - ranges[i]);
	    east = Math.min(east, anchors[i].x + ranges[i]);
	    south = Math.min(south, anchors[i].y + ranges[i]);
	}

	if (west < east) {
	    double t = west;
	    west = east;
	    east = t;
	}

	if (north < south) {
	    double t = north;
	    north = south;
	    south = t;
	}

	if (cpf != null && cpf.pre == null) {
	    double x = 0.5 * east + 0.5 * west;
	    double y = 0.5 * north + 0.5 * south;
	    cpf.pre = new Point2d(x, y);
	}

	Random random = new Random();
	Point2d[] p = new Point2d[cpf.pNum];

	for (int i = 0; i < p.length; ++i) {
	    double x = random.nextDouble() * (east - west) + west;
	    double y = random.nextDouble() * (south - north) + north;
	    p[i] = new Point2d(x, y);
	    //p[i] = new Point2d(Math.random()*(con[1]-con[0]) + con[0], Math.random()*(con[3]-con[2]) + con[2]);
	    //errorModel = new ErrorModelLos(5, 0, 1);
	    //p[i] = new Point2d(pre.x + errorModel.getOffset(0, 0), pre.y + errorModel.getOffset(0, 0));
	}

	return p;
    }
}
