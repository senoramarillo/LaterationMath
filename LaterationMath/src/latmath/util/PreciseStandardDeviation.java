package latmath.util;

import java.util.ArrayList;

/**
 * Precisely compute the standard deviation.
 * <p>
 * To avoid java.util.ConcurrentModificationException of list iterator,
 * need to synchronize methods (performance decreases).
 *
 * @version 1.0, 2012-06-01
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class PreciseStandardDeviation extends StandardDeviation {

    private ArrayList<Double> samples;

    public PreciseStandardDeviation() {
        this.samples = new ArrayList<>();
    }

    public ArrayList<Double> getSamples() {
        return this.samples;
    }
    
    @Override
    public synchronized void reset() {
        this.samples.clear();
    }

    @Override
    public synchronized void addSample(double sample) {
        this.samples.add(sample);
    }

    @Override
    public synchronized double mean() {
        if (this.samples.size() < 1)
            return 0.0;
        double sum = 0.0;
        for (double d : this.samples)
            sum += d;
        return sum / this.samples.size();
    }

    @Override
    public synchronized double variance() {
        if (this.samples.size() < 2)
            return 0.0;
        double mean = this.mean();
        double sum = 0.0;
        for (double d : this.samples)
           sum += (d - mean) * (d - mean);
        return sum / (this.samples.size() - 1);
    }

    @Override
    public synchronized double standardDeviation() {
        return Math.sqrt(this.variance());
    }

}
