package latmath.filter;

import java.util.ArrayList;
import java.util.List;
import latmath.util.LUDecomposition;
import latmath.util.Matrix;

/**
 * Savitzky-Golay filter.
 *
 * http://www.statistics4u.com/fundstat_germ/cc_filter_savgolay.html
 *
 * http://www.wire.tu-bs.de/OLDWEB/mameyer/cmr/savgol.pdf
 *
 * @version 1.0, 2012-05-04
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class SavitzkyGolayFilter {

    private int size;
    private List data;
    private int flush;
    private int flushLimit;
    private int degree;
    private double[] coeffs;
    private int lastCoeffCalc = -1;

    public SavitzkyGolayFilter(int degree, int size, int flushLimit) {
        this.size = size;
        this.flushLimit = flushLimit;
        this.degree = degree;
        data = new ArrayList();
    }

    /**
     * Derivative order is zero for curve smoothing.
     */
    private void computeSGCoefficients(int degree, int nl, int nr) {
        Matrix matrix = new Matrix(degree+1, degree+1);
        double[][] a = matrix.getArray();
        double sum;
        for (int i = 0; i <= degree; i++) {
            for (int j = 0; j <= degree; j++) {
                sum = (i == 0 && j == 0) ? 1 : 0;
                for (int k = 1; k <= nr; k++) {
                    sum += Math.pow(k, i + j);
                }
                for (int k = 1; k <= nl; k++) {
                    sum += Math.pow(-k, i + j);
                }
                a[i][j] = sum;
            }
        }

        double[][] b2 = new double[degree + 1][1];
        b2[0][0] = 1;
        Matrix bm = new Matrix(b2);
        LUDecomposition ludec = new LUDecomposition(matrix);
        bm = ludec.solve(bm);
        double[] b = new double[bm.getArray().length];
        for (int i = 0; i < b.length; i++) {
            b[i] = bm.cell(i, 0);
        }

        coeffs = new double[nl + nr + 1];
        for (int n = -nl; n <= nr; n++) {
            sum = b[0];
            for (int m = 1; m <= degree; m++) {
                sum += b[m] * Math.pow(n, m);
            }
            coeffs[n + nl] = sum;
        }
    }

    public void add(double d) {
        data.add(d);
        if (data.size() > size) {
            data.remove(0);
        }
        flush = 0;
        if (this.lastCoeffCalc != data.size() &&
                data.size() > this.degree) {
            computeSGCoefficients(degree, data.size()-1, 0);
            this.lastCoeffCalc = data.size();
        }
    }

    public void incFlush() {
        flush++;
        if (flush > flushLimit) {
            flush = 0;
            data.clear();
        }
    }

    public double get() {
        if (data.isEmpty()) {
            return -1.0;
        }
        if (data.size() > this.degree) {
            double d = 0.0;
            for (int i = 0; i < data.size(); i++) {
                d += coeffs[i] * (Double) data.get(i);
            }
            return d;
        } else {
            double d = 0.0;
            for (int i = 0; i < data.size(); i++) {
                d += (Double) data.get(i);
            }
            return d/data.size();
        }
    }

}
