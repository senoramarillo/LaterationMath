package latmath.algorithm;

import java.util.logging.Logger;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunctionGradient;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.gradient.NonLinearConjugateGradientOptimizer;

import org.apache.commons.math3.special.Gamma;

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.Releasable;
import latmath.util.dialog.ConfigDialog;

/**
 * Multilateration using maximum likelihood solution.
 *
 * @version 1.0, 2013-02-13
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public final class MLEGamma extends BasicLaterationAlgorithm implements Releasable {

    private static final Logger logger =
        Logger.getLogger("latmath.algorithm.MLEGamma");
    
    private static boolean loggingEnabled = false;

    public static final boolean GRADIENT_OPTIMIZER = false;

    /**
     * 
     */
    protected double shape = 3.3;  // Must be positive

    /**
     * 
     */
    protected double rate = 0.576; // Must be positive

    /**
     * 
     */
    protected double offset = 3.31060119642765;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return "MLE Gamma(" +String.format(Locale.ENGLISH, "%.2f", shape)+ ", "
                + String.format(Locale.ENGLISH, "%.2f", rate) + ") + "
                + String.format(Locale.ENGLISH, "%.2f", offset);
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        PositionEstimate pe =
            MLEGamma.multilaterate(anchors, ranges, shape, rate, offset);
        return pe != null ? pe.getLocation() : null;
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
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JCheckBox cbEnableLogging = new JCheckBox();
        cbEnableLogging.setSelected(loggingEnabled);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Enable logging (class wide!):");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(cbEnableLogging);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                offset = (Double) spOffset.getValue();
                shape = (Double) spShape.getValue();
                rate = (Double) spRate.getValue();
                loggingEnabled = cbEnableLogging.isSelected();
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


    public static class LikelihoodFunction implements MultivariateFunction {
        private final Point2d[] anchors;
        private final double[] ranges;
        private final double shape;
        private final double rate;
        private final double offset;
        private final double gamma;


        public LikelihoodFunction(Point2d[] anchors, double[] ranges,
                                  double shape, double rate, double offset) {
            this.anchors = anchors;
            this.ranges = ranges;
            this.shape = shape;
            this.rate = rate;
            this.offset = offset;
            this.gamma =
                Math.pow(this.rate, this.shape) / Gamma.gamma(this.shape);
        }


        public static final double value(int i, double[] point, Point2d[] anchors, double[] ranges, double shape, double rate, double offset) {
            final double distance =
                Math.sqrt((anchors[i].x - point[0]) * (anchors[i].x - point[0]) +
                          (anchors[i].y - point[1]) * (anchors[i].y - point[1]));
            final double x = ranges[i] + offset - distance;
            if (x <= 0.0) {
                if (MLEGamma.loggingEnabled) {
                    StringBuilder buffer = new StringBuilder();
                    buffer.append("Point (");
                    buffer.append(point[0]);
                    buffer.append(", ");
                    buffer.append(point[1]);
                    buffer.append(") measures too close to anchor ");
                    buffer.append(i);
                    buffer.append("; distance: ");
                    buffer.append(distance);
                    buffer.append(" , measurement: ");
                    buffer.append(ranges[i]);
                    buffer.append(" , difference: ");
                    buffer.append(x);
                    MLEGamma.logger.finer(buffer.toString());
                }
                return 0.0;
            }
            final double c = Math.pow(rate, shape) / Gamma.gamma(shape);
            final double probability =
                c * Math.pow(x, shape - 1.0) * Math.exp(-rate * x);
            return probability;
        }


        @Override
        public double value(double[] point) {
            double result = 1.0;
            for (int i = 0; i < this.anchors.length; i++) {
                final double probability =
                    LikelihoodFunction.value(i, point, this.anchors, this.ranges, this.shape, this.rate, this.offset);
                result *= probability;
            }
            if (MLEGamma.loggingEnabled) {
                MLEGamma.logger.finest("L(" + point[0] + ", " + point[1] + ") = " + result);
            }
            return result;
        }
    }


    public static class LikelihoodGradientFunction implements MultivariateVectorFunction {
        private final Point2d[] anchors;
        private final double[] ranges;
        private final double shape;
        private final double rate;
        private final double offset;

        public LikelihoodGradientFunction(Point2d[] anchors, double[] ranges,
                                          double shape, double rate,
                                          double offset) {
            this.anchors = anchors;
            this.ranges = ranges;
            this.shape = shape;
            this.rate = rate;
            this.offset = offset;
        }

        /**
         * Compute the derivative of the probability for the ith anchor
         * and the jth component.
         */
        public static final double value(int i, int j, double[] point, Point2d[] anchors, double[] ranges, double shape, double rate, double offset) {
            
            final double distance =
                Math.sqrt((anchors[i].x - point[0]) * (anchors[i].x - point[0]) +
                          (anchors[i].y - point[1]) * (anchors[i].y - point[1]));
            final double c = Math.pow(rate, shape) / Gamma.gamma(shape);
            final double z = ranges[i] + offset - distance;
            final double A = (j == 0) ? anchors[i].x : anchors[i].y;
            if (z <= 0.0) {
                return 0.0;
            }
            double result;
            result = (shape - 1.0) * Math.pow(rate, shape) *
                (A - point[j]) * Math.pow(z, shape - 2.0) * Math.exp(-rate * z);
            result -= Math.pow(rate, shape + 1.0) * (A - point[j]) *
                Math.pow(z, shape - 1.0) * Math.exp(-rate * z);
            result /= c * distance;
            return result;
        }


        @Override
        public double[] value(double[] point) throws IllegalArgumentException {
            double[] result = new double[] { 0.0, 0.0 };
            for (int i = 0; i < this.anchors.length; i++) {
                double factor[] = new double[] {
                    LikelihoodGradientFunction.value(i, 0, point, this.anchors, this.ranges, this.shape, this.rate, this.offset),
                    LikelihoodGradientFunction.value(i, 1, point, this.anchors, this.ranges, this.shape, this.rate, this.offset)
                };
                for (int j = 0; j < this.anchors.length; j++) {
                    if (j == i) {
                        continue;
                    }
                    final double f = LikelihoodFunction.value(j, point, this.anchors, this.ranges, this.shape, this.rate, this.offset);
                    factor[0] *= f;
                    factor[1] *= f;
                }
                result[0] += factor[0];
                result[1] += factor[1];
            }
            if (MLEGamma.loggingEnabled) {
                MLEGamma.logger.finest("dL(" + point[0] + ", " + point[1] + ") = (" + result[0] + ", " +
                                   result[1] + ")");
            }
            return result;
        }
    }

    private static final double defaultEpsilon = 1e-2;
    private static final int defaultIterations = 250;
    
    // Specify a convergence criterion
    public static class MLEConvergenceChecker
        implements ConvergenceChecker<PointValuePair> {

        private final double epsilon2;
        private final int iterations;

        public MLEConvergenceChecker() {
            this(MLEGamma.defaultEpsilon, MLEGamma.defaultIterations);
        }

        public MLEConvergenceChecker(double epsilon) {
            this(epsilon, MLEGamma.defaultIterations);
        }


        public MLEConvergenceChecker(int iterations) {
            this(MLEGamma.defaultEpsilon, iterations);
        }

        public MLEConvergenceChecker(double epsilon, int iterations) {
            this.epsilon2 = epsilon * epsilon;
            this.iterations = iterations;
        }

        @Override
        public boolean converged(int iterations,
                                 PointValuePair previous,
                                 PointValuePair current) {
            if (iterations <= this.iterations) {
                final double p[] = previous.getPointRef();
                final double c[] = current.getPointRef();
                final double d = (p[0] - c[0]) * (p[0] - c[0]) +
                                 (p[1] - c[1]) * (p[1] - c[1]);
                if (MLEGamma.loggingEnabled) {
                    MLEGamma.logger.finest("Converged with " + d);
                }
                return (d <= this.epsilon2);
            } else {
                if (MLEGamma.loggingEnabled) {
                    MLEGamma.logger.info("Maximum iteration count reached.");
                }
                return true;
            }
        }
    }

    /**
     * A static instance of the convergence checker that uses default settings.
     */
    private static final ConvergenceChecker defaultConvergenceChecker =
        new MLEGamma.MLEConvergenceChecker();

    /**
     * Static call to this lateration algorithm.
     * <p>
     * This might be useful if one is only interested in the result and not
     * the additional features of the <code>BasicLaterationAlgorithm</code>
     * class.
     *
     * @param anchors The anchor/reference nodes.
     * @param ranges The measured distances to the anchor/reference nodes.
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static PositionEstimate multilaterate(Point2d[] anchors,
						 double[] ranges, double shape,
						 double rate, double offset) {
        // sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

        PositionEstimate pe = LinearLeastSquares.multilaterate(anchors, ranges);
        Point2d[] guessPoints;
        if (pe != null) {
            guessPoints = new Point2d[] {
                Point2d.centerOfMass(anchors),
                pe.getLocation(),
                MinMax.multilaterate(anchors, ranges)
            };
        } else {
            guessPoints = new Point2d[] {
                Point2d.centerOfMass(anchors),
                MinMax.multilaterate(anchors, ranges)
            };
        }

	MLEGamma.ResultPair bestResult = null;

	for (Point2d guessPoint : guessPoints) {
	    MLEGamma.ResultPair result = multilaterate(guessPoint, anchors,
						       ranges, shape, rate,
						       offset);
	    if (result != null && (bestResult == null || bestResult.getLikelihood() < result.getLikelihood())) {
		bestResult = result;
	    }
	}


	return new PositionEstimate(bestResult.getPosition(),
				    PositionEstimate.calculateResidualError(anchors,
									    ranges,
									    bestResult.getPosition()));
    }

    private final static MLEGamma.ResultPair multilaterate(Point2d guessPoint,
							   Point2d[] anchors,
							   double[] ranges,
							   double shape,
							   double rate,
							   double offset) {
        // If there is no guess, we cannot estimate anything.
        if (guessPoint == null) {
            if (MLEGamma.loggingEnabled) {
                MLEGamma.logger.fine("Initial guess: null");
            }
            return null;
        }

        // The likelihood function
        MultivariateFunction function =
            new MLEGamma.LikelihoodFunction(anchors, ranges, shape, rate,
                                            offset);

        ObjectiveFunction objectiveFunction = new ObjectiveFunction(function);


        MultivariateVectorFunction gradient = null;
        ObjectiveFunctionGradient gradientFunction = null;

        if (MLEGamma.GRADIENT_OPTIMIZER || MLEGamma.loggingEnabled) {
            gradient =
                new MLEGamma.LikelihoodGradientFunction(anchors, ranges, shape,
                                                        rate, offset);
        }

        if (MLEGamma.GRADIENT_OPTIMIZER) {
            gradientFunction =
                new ObjectiveFunctionGradient(gradient);
        }

        MultivariateOptimizer optimizer;
        if (MLEGamma.GRADIENT_OPTIMIZER) {
            optimizer =
                new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                        MLEGamma.defaultConvergenceChecker);
        } else {
            optimizer =
                new SimplexOptimizer(MLEGamma.defaultConvergenceChecker);
        }

        final double[] guess = new double[] { guessPoint.x, guessPoint.y };

        PointValuePair res;

        if (MLEGamma.GRADIENT_OPTIMIZER) {
            res = optimizer.optimize(objectiveFunction,
                                     gradientFunction,
                                     MaxEval.unlimited(),
                                     SimpleBounds.unbounded(2),
                                     GoalType.MAXIMIZE,
                                     new InitialGuess(guess),
                                     new NonLinearConjugateGradientOptimizer.BracketingStep(1e-3));
        } else {
            res = optimizer.optimize(objectiveFunction,
                                     MaxEval.unlimited(),
                                     SimpleBounds.unbounded(2),
                                     GoalType.MAXIMIZE,
                                     new InitialGuess(guess),
                                     new MultiDirectionalSimplex(2));
        }

        double[] point = res.getPointRef();
        Point2d result = new Point2d(point[0], point[1]);
        double likelihoodValue, residual;
        double[] gradientValue = null;
        StringBuilder buffer = null;

        if (MLEGamma.loggingEnabled) {
            likelihoodValue = function.value(guess);
            residual = PositionEstimate.calculateResidualError(anchors, ranges,
                                                               guessPoint);
            gradientValue = gradient.value(guess);
            buffer = new StringBuilder();
            buffer.append("    Initial guess: ");
            buffer.append(guessPoint.toString());
            buffer.append(", residual: ");
            buffer.append(residual);
            buffer.append(", likelihood: ");
            buffer.append(likelihoodValue);
            buffer.append(", gradient: (");
            buffer.append(gradientValue[0]);
            buffer.append(", ");
            buffer.append(gradientValue[1]);
            buffer.append(")\n");
        }

        likelihoodValue = function.value(point);

        if (MLEGamma.loggingEnabled) {
            gradientValue = gradient.value(point);
            residual = PositionEstimate.calculateResidualError(anchors, ranges,
                                                               result);
            buffer.append("    Result: ");
            buffer.append(result.toString());
            buffer.append(", residual: ");
            buffer.append(residual);
            buffer.append(", likelihood: ");
            buffer.append(likelihoodValue);
            buffer.append(", gradient: (");
            buffer.append(gradientValue[0]);
            buffer.append(", ");
            buffer.append(gradientValue[1]);
            buffer.append(")");
            MLEGamma.logger.fine(buffer.toString());
        }

        return new MLEGamma.ResultPair(result, likelihoodValue);
    }

    public static final class ResultPair {
	private final Point2d position;
	private final double likelihood;

	public ResultPair(Point2d position, double likelihood) {
	    this.position = position;
	    this.likelihood = likelihood;
	}

	public Point2d getPosition() {
	    return this.position;
	}

	public double getLikelihood()  {
	    return this.likelihood;
	}
    }
}
