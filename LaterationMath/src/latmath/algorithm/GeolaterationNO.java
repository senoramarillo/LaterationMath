package latmath.algorithm;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import latmath.errormodel.ErrorModel;
import latmath.util.ArrayUtils;
import latmath.util.Circle;
import latmath.util.IntersectionPoint2d;
import latmath.util.LMath;
import latmath.util.Point2d;
import latmath.util.QuickSelect;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;
import latmath.weighting.GammaWeigher;
import latmath.weighting.Weighable;
import latmath.weighting.WeigherCollection;

/**
 * Geolateration algorithm with unlimited number of anchors and weighting
 * of final intersection points.
 * <p>
 * Copyright (c) 2013 Thomas Hillebrandt and Heiko Will.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited unless expressly permitted.
 *
 * @version 1.00, 2013-05-12
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class GeolaterationNO extends BasicLaterationAlgorithm implements Releasable {

    private boolean doApprox;
    private boolean doFilter1;
    private boolean doFilter1IncludeApprox;
    private int filter1Limit;
    private boolean doFilter2;
    private double medianFactor;
    private int finalPositionAlgorithm;
    
    /** Final position estimation for GEO-N: center of mass */
    public static final int CENTER_OF_MASS = 1;

    /** Final position estimation for GEO-N: geometric median */
    public static final int GEOMETRIC_MEDIAN = 2;
    
    /**
     * Selected weighting method.
     */
    private Weighable weigher;
    
    /**
     * Weigting methods for left intersection points.
     */
    private transient Weighable[] weightingMethods;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
        
    /**
     * Create a new instance of <code>GeolaterationNO</code>
     * with default settings.
     */
    public GeolaterationNO() {
        this(1, CENTER_OF_MASS);
    }

    /**
     * Create a new instance of <code>GeolaterationNO</code>.
     *
     * @param medianFactor Weighting factor for median filter.
     * @param finalPositionAlgorithm Algorithm for final position estimation
     *                               after intersection building and filtering,
     *                               must be either <code>CENTER_OF_MASS</code>
     *                               or <code>GEOMETRIC_MEDIAN</code>.
     */
    public GeolaterationNO(double medianFactor, int finalPositionAlgorithm) {
        super();
        this.doApprox = true;
        this.doFilter1 = true;
        this.doFilter1IncludeApprox = false;
        this.filter1Limit = 2;
        this.doFilter2 = true;
        this.medianFactor = medianFactor;
        this.weightingMethods = WeigherCollection.getWeighersAsCopy();
        this.weigher = WeigherCollection.getByClass(weightingMethods,
                GammaWeigher.class);
        if (finalPositionAlgorithm >= CENTER_OF_MASS &&
                finalPositionAlgorithm <= GEOMETRIC_MEDIAN) {
            this.finalPositionAlgorithm = finalPositionAlgorithm;
        } else {
            this.finalPositionAlgorithm = CENTER_OF_MASS;
        }
    }
    
    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();
            this.weightingMethods = WeigherCollection.getWeighersAsCopy();
            this.weigher = WeigherCollection.getByClass(weightingMethods,
                weigher.getClass());
        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public String getName() {
        String name = "GEO-N";
        name += finalPositionAlgorithm == CENTER_OF_MASS ? " CoM" : " GM";
        if (weigher != null) {
            name += " (" + weigher.getName()+ ")";
        }
        return name;
    }
    
    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        return multilaterate(anchors, ranges, doApprox, doFilter1,
                doFilter1IncludeApprox, filter1Limit,
                doFilter2, medianFactor, finalPositionAlgorithm,
                weigher);
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

        final JButton button = new JButton("Configure...");
        button.setEnabled(weigher.isConfigurable());
        final Frame frame = parent;
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JComboBox wMethod = new JComboBox();
        wMethod.setModel(new DefaultComboBoxModel(weightingMethods));
        wMethod.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Weighable w = (Weighable) e.getItem();
                    button.setEnabled(w.isConfigurable());
                }
            }
        });
        wMethod.setSelectedItem(weigher);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Weighting method:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(wMethod);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Weighable w = (Weighable) wMethod.getSelectedItem();
                if (w.isConfigurable()) {
                    w.configure(frame);
                }
            }
        });
        tmp.add(button);
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
                doFilter1 = cbDoFilter1.isSelected();
                doFilter1IncludeApprox = cbDoFilter1IncludeApprox.isSelected();
                doFilter2 = cbDoFilter2.isSelected();
                filter1Limit = (Integer) filter1Threshold.getValue();
                medianFactor = (Double) medianFactorSpinner.getValue();
                weigher = (Weighable) wMethod.getSelectedItem();
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
     * @param doFilter1 Use minimum circle containment filter.
     * @param doFilter1IncludeApprox Filter also approximated circle intersections.
     * @param filter1Limit Threshold for minimum circle containment filter.
     * @param doFilter2 Use median filter.
     * @param medianFactor Weighting factor for median filter.
     * @param weigher Weighting method for final points.
     * @param mfAttrs MF attributes.
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
            boolean doApprox, boolean doFilter1, boolean doFilter1IncludeApprox, int filter1Limit,
            boolean doFilter2, double medianFactor, int finalPositionAlgorithm,
            Weighable weigher) {
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
                    Point2d[] pft = Circle.getIntersectionsApprox(
                            anchors[i], ranges[i],
                            anchors[j], ranges[j]);
                    if (pft != null) {
                        intersections[num] = new IntersectionPoint2d[pft.length];
                        for (int k = 0; k < pft.length; k++) {
                            intersections[num][k] = IntersectionPoint2d.convert(pft[k], false, weight);
                        }
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
        
        // step 6a: calculate weight for points
        double[] masses = new double[points.length];
        boolean allZero = true;
        for (int i = 0; i < masses.length; i++) {
            masses[i] = weigher.weigh(points[i], anchors, ranges);
            if (masses[i] != 0.0) {
                allZero = false;
            }
        }
        // if all weights would be zero, keep all points with equal weight
        if (allZero) {
            ArrayUtils.fill(masses, 1.0);
        }
        
        // step 6b: calculate final position estimation with given algorithm
        if (finalPositionAlgorithm == CENTER_OF_MASS) {
            return Point2d.centerOfMass(points, masses);
        } else if (finalPositionAlgorithm == GEOMETRIC_MEDIAN) {
            return Point2d.geometricMedian(points, masses);
        } else {
            return null; // can never happen
        }
    }
    
}
