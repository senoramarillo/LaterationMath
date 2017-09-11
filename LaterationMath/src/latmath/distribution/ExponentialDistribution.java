package latmath.distribution;

import java.io.Serializable;
import latmath.util.LMath;

/**
 * Exponential Distribution.
 * <p>
 * References:
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/ExponentialDistribution.html">
 * Exponential Distribution</a></li>
 * </ul>
 * <p>
 * (c) Apache Software Foundation (ASF), Apache commons math project
 *
 * @version 1.01, 2011-12-09
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class ExponentialDistribution implements Serializable {

    /** The mean of this distribution. */
    private double mean;

    /** Random number generator */
    private java.util.Random random;

    /** Used when generating Exponential samples
     * [1] writes:
     * One table containing the constants
     * q_i = sum_{j=1}^i (ln 2)^j/j! = ln 2 + (ln 2)^2/2 + ... + (ln 2)^i/i!
     * until the largest representable fraction below 1 is exceeded.
     *
     * Note that
     * 1 = 2 - 1 = exp(ln 2) - 1 = sum_{n=1}^infty (ln 2)^n / n!
     * thus q_i -> 1 as i -> infty,
     * so the higher 1, the closer to one we get (the series is not alternating).
     *
     * By trying, n = 16 in Java is enough to reach 1.0.
     */
    private static double[] EXPONENTIAL_SA_QI = null;

    /**
     * Initialize tables
     */
    static {
        /**
         * Filling EXPONENTIAL_SA_QI table.
         * Note that we don't want qi = 0 in the table.
         */
        final double LN2 = Math.log(2);
        double qi = 0;
        int i = 1;

        /**
         * a priori, we know that there will be 16 elements:
         */
        double[] da = new double[16];

        while (qi < 1) {
            qi += Math.pow(LN2, i) / LMath.factorial(i);
            da[i-1] = qi;
            ++i;
        }

        EXPONENTIAL_SA_QI = da;
    }

    /**
     * Create a exponential distribution with the given mean.
     *
     * @param mean mean of this distribution, must be greater zero.
     */
    public ExponentialDistribution(double mean) {
        if (mean <= 0) {
            throw new IllegalArgumentException("Mean <= 0");
        }
        this.mean = mean;
	this.random = new java.util.Random();
    }

    /**
     * Get the mean of this distribution.
     *
     * @return The mean of this distribution.
     */
    public double getMean() {
        return mean;
    }
    
    /**
     * Sets the mean of this distribution.
     * 
     * @param mean The new mean of this distribution.
     */
    public void setMean(double mean) {
        this.mean = mean;
    }

    /**
     * Generates a random value sampled from this distribution.
     * <p>
     * <strong>Algorithm Description</strong>: Uses the Algorithm SA (Ahrens)
     * from p. 876 in:
     * [1]: Ahrens, J. H. and Dieter, U. (1972). Computer methods for
     * sampling from the exponential and normal distributions.
     * Communications of the ACM, 15, 873-882.
     * </p>
     *
     * @return a random value.
     */
    public double sample() {
        // Step 1:
        double a = 0;
        double u = nextUniform(random, 0, 1);

        // Step 2 and 3:
        while (u < 0.5) {
            a += EXPONENTIAL_SA_QI[0];
            u *= 2;
        }

        // Step 4 (now u >= 0.5):
        u += u - 1;

        // Step 5:
        if (u <= EXPONENTIAL_SA_QI[0]) {
            return mean * (a + u);
        }

        // Step 6:
        int i = 0; // Should be 1, be we iterate before it in while using 0
        double u2 = nextUniform(random, 0, 1);
        double umin = u2;

        // Step 7 and 8:
        do {
            ++i;
            u2 = nextUniform(random, 0, 1);

            if (u2 < umin) {
                umin = u2;
            }

            // Step 8:
        } while (u > EXPONENTIAL_SA_QI[i]); // Ensured to exit since EXPONENTIAL_SA_QI[MAX] = 1

        return mean * (a + umin * EXPONENTIAL_SA_QI[0]);
    }

    /**
     * Generates a uniformly distributed random value from the open interval
     * (<code>lower</code>,<code>upper</code>) (i.e., endpoints excluded).
     * <p>
     * <strong>Algorithm Description</strong>: scales the output of
     * Random.nextDouble(), but rejects 0 values (i.e., will generate another
     * random double if Random.nextDouble() returns 0). This is necessary to
     * provide a symmetric output interval (both endpoints excluded).
     * </p>
     *
     * @param rand The random number generator.
     * @param lower the lower bound.
     * @param upper the upper bound.
     *
     * @return a uniformly distributed random value from the interval
     *         (lower, upper).
     */
    public static double nextUniform(java.util.Random rand, double lower, double upper) {
        // ensure rand.nextDouble() isn't 0.0
        double u = rand.nextDouble();
        while (u <= 0.0) {
            u = rand.nextDouble();
        }
        return lower + u * (upper - lower);
    }

}
