package latmath.util;

/**
 * Incrementally compute the standard deviation using Welfords method.
 *
 * @version 1.0, 2012-06-01
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public abstract class StandardDeviation {

    public abstract void reset();

    public abstract void addSample(double sample);

    public abstract double mean();

    public abstract double variance();

    public double standardDeviation() {
        return Math.sqrt(this.variance());
    }
}
