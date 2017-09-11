package latmath.ranging.filter;

import java.awt.Frame;
import java.util.Random;
import latmath.distribution.NormalDistribution;
import latmath.distribution.ExponentialDistribution;
import latmath.util.Releasable;

/**
 * Ranging filter which simulates Nanotron ranging error.
 *
 * @version 1.0, 2012-05-10
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class MErrorRangingFilter implements RangingFilter, Releasable {

    private NormalDistribution losError;
    private ExponentialDistribution nlosError;
    private Random uniform;
    
    /** serial version UID - don't change, will make saved files invalid */
    private static final long serialVersionUID = 1L;

    public MErrorRangingFilter() {
        this.losError = new NormalDistribution(0.90, 0.56);
	this.nlosError = new ExponentialDistribution(1.0);
	this.uniform = new Random();
    }

    @Override
    public String getName() {
        return "RF-EM";
    }

    @Override
    public double[] filter(double[] measuredDistances, double[] realDistances, long timestamp) {
        double[] result = new double[measuredDistances.length];
        for (int i = 0; i < measuredDistances.length; i++) {
            if (realDistances == null) {
                result[i] = -1;
            } else {
                if (measuredDistances[i] >= 0) {
                    result[i] = realDistances[i] + this.losError.sample();
		    if (realDistances[i] >= 25 &&
                        this.uniform.nextDouble() <= 0.2) {
			result[i] += this.nlosError.sample();
		    }
		} else {
			result[i] = -1.0;
		}
            }
        }
        return result;
    }

    @Override
    public void reset() {}

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public boolean configure(Frame parent) {
        return false;
    }

}

