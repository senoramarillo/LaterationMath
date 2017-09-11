package latmath.algorithm;

import java.util.logging.Logger;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import latmath.errormodel.ErrorModel;
import latmath.util.Point2d;
import latmath.util.PositionEstimate;
import latmath.util.dialog.ConfigDialog;

/**
 * Multilateration using maximum likelihood solution.
 *
 * @version 1.0, 2013-02-13
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public final class MLEGauss extends BasicLaterationAlgorithm {

    private static final Logger logger =
        Logger.getLogger("latmath.algorithm.MLEGauss");

    public static final boolean GRADIENT_OPTIMIZER = false;

    /**
     * 
     */
    protected double median = 3.04;  // Must be positive

    /**
     * 
     */
    protected double deviation = 3.55; // Must be positive
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;
    
    @Override
    public String getName() {
        return "MLE Gauss(" + this.median + ", " + this.deviation + ")";
    }

    @Override
    public Point2d localize(Point2d[] anchors, double[] ranges,
            Point2d actualPosition, ErrorModel errorModel, int width, int height) {
        PositionEstimate pe =
            MLEGauss.multilaterate(anchors, ranges, this.median, this.deviation);
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
        final JSpinner spMedian = new JSpinner();
        spMedian.setModel(new SpinnerNumberModel(this.median, -1000.0, 1000.0, 0.1));
        Dimension d = spMedian.getPreferredSize();
        d.width = 60;
        spMedian.setPreferredSize(d);
        // Create label and add control
        JPanel lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        JLabel label = new JLabel("Gauss distribution, median:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spMedian);
        content.add(tmp);
        
        // Add next control
        tmp = new JPanel();
        tmp.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        // Create control
        final JSpinner spDeviation = new JSpinner();
        spDeviation.setModel(new SpinnerNumberModel(this.deviation, 0.0, 1000.0, 1.0));
        d = spDeviation.getPreferredSize();
        d.width = 60;
        spDeviation.setPreferredSize(d);
        // Create label and add control
        lContainer = new JPanel();
        lContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        lContainer.setPreferredSize(new Dimension(lWidth, 30));
        label = new JLabel("Gauss distribution, standard deviation:");
        lContainer.add(label);
        tmp.add(lContainer);
        tmp.add(spDeviation);
        content.add(tmp);
        
        final ConfigDialog dialog = new ConfigDialog(parent, true);

        // Build OK and Cancel actions
        ActionListener okAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                median = (Double) spMedian.getValue();
                deviation = (Double) spDeviation.getValue();
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
        private final double median;
        private final double deviation;


        public LikelihoodFunction(Point2d[] anchors, double[] ranges,
                                  double median, double deviation) {
            this.anchors = anchors;
            this.ranges = ranges;
            this.median = median;
            this.deviation = deviation;
        }


        public static final double value(int i, double[] point, Point2d[] anchors, double[] ranges, double median, double deviation) {
            final double distance =
                Math.sqrt((anchors[i].x - point[0]) * (anchors[i].x - point[0]) +
                          (anchors[i].y - point[1]) * (anchors[i].y - point[1]));
            final double Z = ranges[i];
            final double median2 = median * median;
            final double deviation2 = deviation * deviation;
            double logProbability;
            logProbability = -(Z * Z);
            logProbability += (2 * Z - 2 * median) * distance;
            logProbability += 2 * median * Z;
            logProbability -= distance * distance;
            logProbability -= deviation2 * Math.log(Math.PI) + median2 + 2 *
                deviation2 * Math.log(deviation) + Math.log(2) * deviation2;
            logProbability /= 2 * deviation2;
            return logProbability;
        }


        @Override
        public double value(double[] point) {
            double result = 1.0;
            for (int i = 0; i < this.anchors.length; i++) {
                final double logProbability =
                    LikelihoodFunction.value(i, point, this.anchors, this.ranges, this.median, this.deviation);
                result += logProbability;
            }
            MLEGauss.logger.finest("L(" + point[0] + ", " + point[1] + ") = " + result);
            return result;
        }
    }


    public static class LikelihoodGradientFunction implements MultivariateVectorFunction {
        private final Point2d[] anchors;
        private final double[] ranges;
        private final double median;
        private final double deviation;

        public LikelihoodGradientFunction(Point2d[] anchors, double[] ranges,
                                          double median, double deviation) {
            this.anchors = anchors;
            this.ranges = ranges;
            this.median = median;
            this.deviation = deviation;
        }

        /**
         * Compute the derivative of the probability for the ith anchor
         * and the jth component.
         */
        public static final double value(int i, int j, double[] point, Point2d[] anchors, double[] ranges, double median, double deviation) {
            
            final double distance =
                Math.sqrt((anchors[i].x - point[0]) * (anchors[i].x - point[0]) +
                          (anchors[i].y - point[1]) * (anchors[i].y - point[1]));
            final double sigma2 = deviation * deviation;
            final double t = ((j == 0) ? anchors[i].x : anchors[i].y) - point[j];
            double result;
            result = sigma2 * t * (ranges[i] - distance - median) / distance;
            return result;
        }


        @Override
        public double[] value(double[] point) throws IllegalArgumentException {
            double[] result = new double[] { 0.0, 0.0 };
            for (int i = 0; i < this.anchors.length; i++) {
                result[0] += LikelihoodGradientFunction.value(i, 0, point, this.anchors, this.ranges, this.median, this.deviation);
                result[1] += LikelihoodGradientFunction.value(i, 1, point, this.anchors, this.ranges, this.median, this.deviation);
            }
            MLEGauss.logger.finest("dL(" + point[0] + ", " + point[1] + ") = (" + result[0] + ", " +
                                   result[1] + ")");
            return result;
        }
    }

    private static final double defaultEpsilon = 1e-6;
    private static final int defaultIterations = 4000;
    
    // Specify a convergence criterion
    public static class MLEConvergenceChecker
        implements ConvergenceChecker<PointValuePair> {

        private final double epsilon2;
        private final int iterations;

        public MLEConvergenceChecker() {
            this(MLEGauss.defaultEpsilon, MLEGauss.defaultIterations);
        }

        public MLEConvergenceChecker(double epsilon) {
            this(epsilon, MLEGauss.defaultIterations);
        }


        public MLEConvergenceChecker(int iterations) {
            this(MLEGauss.defaultEpsilon, iterations);
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
                MLEGauss.logger.finest("Converged with " + d);
                return (d <= this.epsilon2);
            } else {
                MLEGauss.logger.info("Maximum iteration count reached.");
                return true;
            }
        }
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
     *
     * @return The estimated position of the mobile node to be located or
     *         <code>null</code> if no position could be calculated, e.g.
     *         localization failed.
     */
    public static PositionEstimate multilaterate(Point2d[] anchors,
						 double[] ranges, double median,
						 double deviation) {
        // sanity check
        if (anchors.length != ranges.length) {
            return null;
        }

	Point2d[] guessPoints = new Point2d[] { Point2d.centerOfMass(anchors),
						LinearLeastSquares.multilaterate(anchors, ranges).getLocation(),
						MinMax.multilaterate(anchors, ranges)
						 };

	MLEGauss.ResultPair bestResult = null;

	for (Point2d guessPoint : guessPoints) {
	    MLEGauss.ResultPair result = multilaterate(guessPoint, anchors,
						       ranges,
                                                       median, deviation);
	    if (result != null && (bestResult == null || bestResult.getLikelihood() < result.getLikelihood())) {
		bestResult = result;
	    }
	}

	return new PositionEstimate(bestResult.getPosition(),
				    PositionEstimate.calculateResidualError(anchors,
									    ranges,
									    bestResult.getPosition()));
    }

    private final static MLEGauss.ResultPair multilaterate(Point2d guessPoint,
							   Point2d[] anchors,
							   double[] ranges,
							   double median,
							   double deviation) {
        // If there is no guess, we cannot estimate anything.
        if (guessPoint == null) {
            MLEGauss.logger.fine("Initial guess: null");
            return null;
        }

        // The likelihood function
        MultivariateFunction function =
            new MLEGauss.LikelihoodFunction(anchors, ranges, median, deviation);

        ObjectiveFunction objectiveFunction = new ObjectiveFunction(function);

        MultivariateVectorFunction gradient =
            new MLEGauss.LikelihoodGradientFunction(anchors, ranges, 
                                                    median, deviation);

        ObjectiveFunctionGradient gradientFunction =
            new ObjectiveFunctionGradient(gradient);

        MultivariateOptimizer optimizer;
        if (MLEGauss.GRADIENT_OPTIMIZER) {
            optimizer =
                new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
                                                        new MLEGauss.MLEConvergenceChecker());
        } else {
            optimizer =
                new SimplexOptimizer(new MLEGauss.MLEConvergenceChecker());
        }

        // Compute an initial guess
        final double[] guess = new double[] { guessPoint.x, guessPoint.y };

        PointValuePair res = optimizer.optimize(objectiveFunction,
                                                gradientFunction,
                                                MaxEval.unlimited(),
                                                SimpleBounds.unbounded(2),
                                                GoalType.MAXIMIZE,
                                                new InitialGuess(guess),
                                                new MultiDirectionalSimplex(2),
                                                new NonLinearConjugateGradientOptimizer.BracketingStep(1e-3));

        double[] point = res.getPointRef();
        Point2d result = new Point2d(point[0], point[1]);
        double likelihoodValue, residual;

        {
            likelihoodValue = function.value(guess);
            residual = PositionEstimate.calculateResidualError(anchors, ranges, guessPoint);
            double[] gradientValue = gradient.value(guess);
            StringBuffer buffer = new StringBuffer();
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

            likelihoodValue = function.value(point);
            gradientValue = gradient.value(point);
            residual = PositionEstimate.calculateResidualError(anchors, ranges, result);

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
            MLEGauss.logger.fine(buffer.toString());
        }

        return new MLEGauss.ResultPair(result, likelihoodValue);
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
