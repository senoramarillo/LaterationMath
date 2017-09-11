package latmath.distribution;

/**
 * Gamma Distribution.
 * <p>
 * References:
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/GammaDistribution.html">
 * Gamma Distribution</a></li>
 * </ul>
 * <p>
 * (c) Apache Software Foundation (ASF), Apache commons math project
 *
 * @version 1.00, 2012-03-28
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class GammaDistribution {

    /** Random number generator */
    private java.util.Random random;

     /** The shape parameter. */
    private final double alpha;

    /** The scale parameter. */
    private final double beta;

    /** Napier's constant e, base of the natural logarithm. */
    private static final double E = 2850325.0 / 1048576.0 + 8.254840070411028747e-8;

    /**
     * Create a new gamma distribution with the given {@code alpha} and
     * {@code beta} values.
     *
     * @param alpha Shape parameter.
     * @param beta Scale parameter.
     *
     * @throws IllegalArgumentException if {@code alpha <= 0} or
     * {@code beta <= 0}.
     */
    public GammaDistribution(double alpha, double beta) {
        if (alpha <= 0) {
            throw new IllegalArgumentException("alpha <= 0");
        }
        if (beta <= 0) {
            throw new IllegalArgumentException("beta <= 0");
        }
        this.alpha = alpha;
        this.beta = beta;
        this.random = new java.util.Random();
    }

    /**
     * Access the {@code alpha} shape parameter.
     *
     * @return {@code alpha}.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Access the {@code beta} scale parameter.
     *
     * @return {@code beta}.
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Generate a random value sampled from this distribution.
     *
     * @return a random value.
     */
    public double sample() {
        return nextGamma(alpha, beta);
    }

    /**
     * <p>Generates a random value from the Gamma Distribution.</p>
     *
     * <p>This implementation uses the following algorithms: </p>
     *
     * <p>For 0 < shape < 1: <br/>
     * Ahrens, J. H. and Dieter, U., <i>Computer methods for
     * sampling from gamma, beta, Poisson and binomial distributions.</i>
     * Computing, 12, 223-246, 1974.</p>
     *
     * <p>For shape >= 1: <br/>
     * Marsaglia and Tsang, <i>A Simple Method for Generating
     * Gamma Variables.</i> ACM Transactions on Mathematical Software,
     * Volume 26 Issue 3, September, 2000.</p>
     *
     * @param shape the median of the Gamma distribution
     * @param scale the scale parameter of the Gamma distribution
     *
     * @return random value sampled from the Gamma(shape, scale) distribution
     */
    public double nextGamma(double shape, double scale) {
        if (shape < 1) {
            // [1]: p. 228, Algorithm GS

            while (true) {
                // Step 1:
                final double u = ExponentialDistribution.nextUniform(random, 0, 1);
                final double bGS = 1 + shape / E;
                final double p = bGS * u;

                if (p <= 1) {
                    // Step 2:

                    final double x = Math.pow(p, 1 / shape);
                    final double u2 = ExponentialDistribution.nextUniform(random, 0.0, 1);

                    if (u2 > Math.exp(-x)) {
                        // Reject
                        continue;
                    } else {
                        return scale * x;
                    }
                } else {
                    // Step 3:

                    final double x = -1 * Math.log((bGS - p) / shape);
                    final double u2 = ExponentialDistribution.nextUniform(random, 0, 1);

                    if (u2 > Math.pow(x, shape - 1)) {
                        // Reject
                        continue;
                    } else {
                        return scale * x;
                    }
                }
            }
        }

        // Now shape >= 1

        final double d = shape - 0.333333333333333333;
        final double c = 1.0 / (3 * Math.sqrt(d));

        while (true) {
            final double x = random.nextGaussian();
            final double v = (1 + c * x) * (1 + c * x) * (1 + c * x);

            if (v <= 0) {
                continue;
            }

            final double xx = x * x;
            final double u = ExponentialDistribution.nextUniform(random, 0, 1);

            // Squeeze
            if (u < 1 - 0.0331 * xx * xx) {
                return scale * d * v;
            }

            if (Math.log(u) < 0.5 * xx + d * (1 - v + Math.log(v))) {
                return scale * d * v;
            }
        }
    }

}
