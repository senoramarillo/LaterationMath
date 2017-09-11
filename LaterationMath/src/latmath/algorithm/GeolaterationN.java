package latmath.algorithm;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.errormodel.ErrorModel;
import latmath.util.Circle;
import latmath.util.IntersectionPoint2d;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.QuickSelect;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Geolateration algorithm with unlimited number of anchors.
 * <p>
 * Copyright (c) 2011 Thomas Hillebrandt and Heiko Will.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited unless expressly permitted.
 *
 * @version 1.0, 2011-08-30
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class GeolaterationN extends BasicLaterationAlgorithm implements Releasable {

    private boolean doApprox;
    private int approxMethod;
    private boolean doFilter1;
    private boolean doFilter1IncludeApprox;
    private int filter1Limit;
    private boolean doFilter2;
    private double medianFactor;
    private double weightRealIntersection;
    private double weightApproxIntersection;
    private int finalPositionAlgorithm;

    /** Final position estimation for GEO-N: center of mass */
    public static final int CENTER_OF_MASS = 1;

    /** Final position estimation for GEO-N: geometric median */
    public static final int GEOMETRIC_MEDIAN = 2;
    
    /** Default Geo-n approx method */
    public static final int APPROX_1 = 1;
    
    /** Bilateration approx method */
    public static final int APPROX_2 = 2;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    /**
     * Create a new instance of <code>GeolaterationN</code>
     * with default settings.
     */
    public GeolaterationN() {
        this(1, CENTER_OF_MASS);
    }

    /**
     * Create a new instance of <code>GeolaterationN</code>.
     *
     * @param medianFactor Weighting factor for median filter.
     * @param finalPositionAlgorithm Algorithm for final position estimation
     *                               after intersection building and filtering,
     *                               must be either <code>CENTER_OF_MASS</code>
     *                               or <code>GEOMETRIC_MEDIAN</code>.
     */
    public GeolaterationN(double medianFactor, int finalPositionAlgorithm) {
        super();
        this.doApprox = true;
        this.approxMethod = APPROX_1;
        this.doFilter1 = true;
        this.doFilter1IncludeApprox = false;
        this.filter1Limit = 2;
        this.doFilter2 = true;
        this.weightRealIntersection = 3.0;
        this.weightApproxIntersection = 3.0;
        this.medianFactor = medianFactor;
        if (finalPositionAlgorithm >= CENTER_OF_MASS &&
                finalPositionAlgorithm <= GEOMETRIC_MEDIAN) {
            this.finalPositionAlgorithm = finalPositionAlgorithm;
        } else {
            this.finalPositionAlgorithm = CENTER_OF_MASS;
        }
    }

    @Override
    public String getName() {
        String name = "GEO-N";
        name += finalPositionAlgorithm == CENTER_OF_MASS ? " CoM" : " GM";
        return name;
    }
    
    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, doApprox, approxMethod, doFilter1,
                doFilter1IncludeApprox, filter1Limit,
                doFilter2, medianFactor, weightRealIntersection,
                weightApproxIntersection, finalPositionAlgorithm);
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
        final JCheckBox cbDoApprox = new JCheckBox();
        cbDoApprox.setSelected(doApprox);
        // Create label and add control
        JPanel lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        JLabel label = new JLabel("Use circle approx:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(cbDoApprox);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JComboBox apm = new JComboBox();
        String[] apmNames = new String[] {"Approx Geo-n", "Approx Bilateration"};
        apm.setModel(new DefaultComboBoxModel(apmNames));
        apm.setSelectedIndex(approxMethod - 1);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Set approx method:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(apm);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JCheckBox cbDoFilter1 = new JCheckBox();
        cbDoFilter1.setSelected(doFilter1);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Use min circle filter:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(cbDoFilter1);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JCheckBox cbDoFilter1IncludeApprox = new JCheckBox();
        cbDoFilter1IncludeApprox.setSelected(doFilter1IncludeApprox);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Use min circle filter on approx:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(cbDoFilter1IncludeApprox);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner filter1Threshold = new JSpinner();
        filter1Threshold.setModel(new SpinnerNumberModel(this.filter1Limit, 0, 1000, 1));
        Dimension d = filter1Threshold.getPreferredSize();
        d.width = 60;
        filter1Threshold.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Min circle filter value:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(filter1Threshold);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JCheckBox cbDoFilter2 = new JCheckBox();
        cbDoFilter2.setSelected(doFilter2);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Use median filter:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(cbDoFilter2);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner medianFactorSpinner = new JSpinner();
        medianFactorSpinner.setModel(new SpinnerNumberModel(this.medianFactor, 0, 10, 0.1));
        d = medianFactorSpinner.getPreferredSize();
        d.width = 60;
        medianFactorSpinner.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Set median factor:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(medianFactorSpinner);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner pointRealWeight = new JSpinner();
        pointRealWeight.setModel(new SpinnerNumberModel(this.weightRealIntersection, 0.01, 1000.0, 0.1));
        d = pointRealWeight.getPreferredSize();
        d.width = 60;
        pointRealWeight.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Weight scale (real):");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(pointRealWeight);
        content.add(tmp);
        
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner pointApproxWeight = new JSpinner();
        pointApproxWeight.setModel(new SpinnerNumberModel(this.weightApproxIntersection, 0.01, 1000.0, 0.1));
        d = pointApproxWeight.getPreferredSize();
        d.width = 60;
        pointApproxWeight.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Weight scale (approx):");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(pointApproxWeight);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JComboBox fpa = new JComboBox();
        String[] fpaNames = new String[] {"Center of mass", "Geometric median"};
        fpa.setModel(new DefaultComboBoxModel(fpaNames));
        fpa.setSelectedIndex(finalPositionAlgorithm - 1);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Set final position alg.:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(fpa);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doApprox = cbDoApprox.isSelected();
                approxMethod = apm.getSelectedIndex() + 1;
                doFilter1 = cbDoFilter1.isSelected();
                doFilter1IncludeApprox = cbDoFilter1IncludeApprox.isSelected();
                doFilter2 = cbDoFilter2.isSelected();
                filter1Limit = (Integer) filter1Threshold.getValue();
                medianFactor = (Double) medianFactorSpinner.getValue();
                weightRealIntersection = (Double) pointRealWeight.getValue();
                weightApproxIntersection = (Double) pointApproxWeight.getValue();
                finalPositionAlgorithm = fpa.getSelectedIndex() + 1;
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

    /**
     * Static call to this lateration algorithm.
     * <p>
     * This might be useful if one is only interested in the result and not
     * the additional features of the <code>BasicLaterationAlgorithm</code>
     * class.
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     * @param doApprox Use approximation for circle intersections.
     * @param approxMethod The method to use for approximation of circle intersections.
     * @param doFilter1 Use minimum circle containment filter.
     * @param doFilter1IncludeApprox Filter also approximated circle intersections.
     * @param filter1Limit Threshold for minimum circle containment filter.
     * @param doFilter2 Use median filter.
     * @param medianFactor Weighting factor for median filter.
     * @param weightRealIntersection Weight scale of real intersection point.
     * @param weightApproxIntersection Weight scale of approximated intersection point.
     * @param finalPositionAlgorithm Algorithm for final position estimation
     *                               after intersection building and filtering,
     *                               must be either <code>CENTER_OF_MASS</code>
     *                               or <code>GEOMETRIC_MEDIAN</code>.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static Point2d multilaterate(Point2d[] anchors, double[] ranges,
            boolean doApprox, int approxMethod, boolean doFilter1, boolean doFilter1IncludeApprox, int filter1Limit,
            boolean doFilter2, double medianFactor, double weightRealIntersection,
            double weightApproxIntersection, int finalPositionAlgorithm) {
        // step 0: sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

        // step 1: calculate circle intersections
        int n = anchors.length;
        int binom = LMath.binom(n, 2);
        IntersectionPoint2d[][] intersections = new IntersectionPoint2d[binom][];

        // build all k-permutations
        int num = 0;
        for (int i = 0; i < anchors.length-1; i++) {
            for (int j = i+1; j < anchors.length; j++) {
                // set weight for intersection to one, scale later
                double weight = 1;
                // Berechne Schnittpunkte mit aktueller Permutation
                intersections[num] = IntersectionPoint2d.convertA(
                        Circle.getIntersection(anchors[i],
                        ranges[i], anchors[j],
                        ranges[j]), weight);
                // no intersection => try to get approximated intersection
                if (doApprox && intersections[num] == null) {
                    Point2d pft;
                    if (approxMethod == APPROX_1) {
                        pft = Circle.getIntersectionApprox(
                            anchors[i], ranges[i],
                            anchors[j], ranges[j]);
                    } else {
                        pft = Circle.getIntersectionApprox2(
                            anchors[i], ranges[i],
                            anchors[j], ranges[j]);
                    }
                    if (pft != null) {
                        intersections[num] = new IntersectionPoint2d[] {
                            IntersectionPoint2d.convert(pft, false, weight)
                        };
                    }
                }
                if (intersections[num] != null) {
                    num++;
                }
            }
        }

        // step 2: copy intersections into one array
        int totalLength = 0;
        int currentLength = 0;
        for (int i = 0; i < num; i++) {
            totalLength += intersections[i].length;
        }
        IntersectionPoint2d[] points = new IntersectionPoint2d[totalLength];
        for (int i = 0; i < num; i++) {
            System.arraycopy(intersections[i], 0, points, currentLength, intersections[i].length);
            currentLength += intersections[i].length;
        }

        // step 3: filter intersections points: only keep points which are
        //         contained in anchor length - 2 circles.
        if (doFilter1) {
            int limit = Math.max(anchors.length - filter1Limit, 0);
            points = Circle.minimumCircleContainment(anchors, ranges, points,
                    limit, doFilter1IncludeApprox);
        }
        
        // step 4: if there are n*(n-1)/2 points which are very close together
        //          => no ranging error, take one of them as result
        int closeCount = (anchors.length * (anchors.length - 1)) / 2;
        for (int i = 0; i < points.length; i++) {
            int currentCloseCount = 1;
            for (int j = 0; j < points.length; j++) {
                if (i != j && points[i].distance(points[j]) < 0.1) {
                    currentCloseCount++;
                }
            }
            if (currentCloseCount >= closeCount) {
                return points[i];
            }
        }

        // step 5a: only use second filter if enough points are remaining
        if (points.length >= 3) {
            if (doFilter2) {
                // step 5b: apply median filter on remaining points
                double[] distance = new double[points.length];
                for (int i = 0; i < distance.length; i++) {
                    for (int j = 0; j < distance.length; j++) {
                        if (i != j) {
                            distance[i] += points[i].distance(points[j]);
                        }
                    }
                }

                Double[] copy = new Double[distance.length];
                for (int i = 0; i < copy.length; i++) {
                    copy[i] = distance[i];
                }
                double median = (Double) QuickSelect.select(copy, distance.length/2+1);

                ArrayList<IntersectionPoint2d> v = new ArrayList<>();
                for (int i = 0; i < distance.length; i++) {
                    if (distance[i] <= median * medianFactor) {
                        v.add(points[i]);
                    }
                }
                points = v.toArray(new IntersectionPoint2d[0]);
            }
        }
        
        // step 6: calculate final position estimation with given algorithm
        double[] masses = new double[points.length];
        for (int i = 0; i < masses.length; i++) {
            if (points[i].isRealIntersection()) {
                masses[i] = weightRealIntersection * points[i].getWeight();
            } else {
                masses[i] = weightApproxIntersection * points[i].getWeight();
            }
        }
        if (finalPositionAlgorithm == CENTER_OF_MASS) {
            return Point2d.centerOfMass(points, masses);
        } else if (finalPositionAlgorithm == GEOMETRIC_MEDIAN) {
            return Point2d.geometricMedian(points, masses);
        } else {
            return null; // can never happen
        }
    }
    
}
