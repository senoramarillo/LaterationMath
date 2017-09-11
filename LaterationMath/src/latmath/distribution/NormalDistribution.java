package latmath.distribution;

import java.io.Serializable;
import java.util.Random;

/**
 * Normal (Gauss) Distribution.
 * <p>
 * References:
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/NormalDistribution.html">
 * Normal Distribution</a></li>
 * </ul>
 * 
 * @version 1.0, 2011-08-04
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class NormalDistribution implements Serializable {

    /** Mean of this distribution. */
    private final double mean;

    /** Standard deviation of this distribution. */
    private final double standardDeviation;

    /** The random number generator */
    private final Random rand;

    /**
     * Create a normal distribution with mean equal to zero and standard
     * deviation equal to one.
     */
    public NormalDistribution() {
        this(0, 1);
    }

    /**
     * Create a normal distribution using the given mean and standard deviation.
     *
     * @param mean Mean for this distribution.
     * @param standardDeviation Standard deviation for this distribution, must
     *                          be greater zero.
     */
    public NormalDistribution(double mean, double standardDeviation) {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("Standard deviation <= 0");
        }
        this.mean = mean;
        this.standardDeviation = standardDeviation;
        rand = new Random();
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
     * Get the standard deviation of this distribution.
     *
     * @return The standard deviation of this distribution.
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Generate a random value sampled from this distribution.
     *
     * @return a random value.
     */
    public double sample() {
        return standardDeviation * rand.nextGaussian() + mean;
    }

}
