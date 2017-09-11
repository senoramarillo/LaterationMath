package latmath.util;

/**
 * Incrementally compute the standard deviation using Welfords method.
 *
 * @version 1.0, 2012-06-01
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class WelfordStandardDeviation extends StandardDeviation {

    private double M;
    private double S;
    private int count;

    public WelfordStandardDeviation () {
        reset0();
    }

    private void reset0() {
        this.M = 0.0;
        this.S = 0.0;
        this.count = 0;
    }

    @Override
    public void reset() {
        reset0();
    }

    @Override
    public void addSample(double sample) {
        double M_old = M;
        this.count++;
        this.M += (sample - this.M) / this.count;
        this.S += (sample - M_old) * (sample - this.M);
    }

    @Override
    public double mean() {
        return this.M;
    }

    @Override
    public double variance() {
        return (this.count > 1) ? this.S / (this.count - 1) : 0.0;
    }

    @Override
    public double standardDeviation() {
        return Math.sqrt(this.variance());
    }
}
