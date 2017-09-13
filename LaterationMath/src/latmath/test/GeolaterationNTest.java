package latmath.test;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import java.util.Arrays;
import latmath.util.Point2d;

public class GeolaterationNTest {
	static boolean doApprox;
	static int approxMethod;
	static boolean doFilter1;
	static boolean doFilter1IncludeApprox;
	static int filter1Limit;
	static boolean doFilter2;
	static double medianFactor;
	static double weightRealIntersection;
	static double weightApproxIntersection;
	static int finalPositionAlgorithm;

	public static void main(String[] args) {

		/** Final position estimation for GEO-N: center of mass */
		final int CENTER_OF_MASS = 1;

		/** Final position estimation for GEO-N: geometric median */
		final int GEOMETRIC_MEDIAN = 2;

		/** Default Geo-n approx method */
		final int APPROX_1 = 1;

		/** Bilateration approx method */
		//final int APPROX_2 = 2;

		doApprox = true;
		approxMethod = APPROX_1;
		doFilter1 = true;
		doFilter1IncludeApprox = false;
		filter1Limit = 2;
		doFilter2 = true;
		weightRealIntersection = 3.0;
		weightApproxIntersection = 3.0;
		medianFactor = GEOMETRIC_MEDIAN;

		Point2d[] anchors = new Point2d[] { new Point2d(24.5626016260160, 2.29970731707320),
				new Point2d(23.1605118679050, 8.57622125902990), new Point2d(24.7228648090820, 12.1175545923630),
				new Point2d(26.0127689243310, 9.01743205528970), new Point2d(30.8588226401950, 9.19865358007020),
				new Point2d(30.6951496849370, 1.20326677881980), new Point2d(26.5792387026600, 1.27245974100970),
				new Point2d(24.9928307764210, 6.69500716668635), new Point2d(27.0096672540500, 6.66041068559140) };

		double[][] ranges;

		double[] distanceRanges = { 4.05000000000000, 7.96000000000000, 9.97000000000000, 0, 8.56000000000000,
				4.24000000000000, 2.24000000000000, 0, 0.360000000000000, 3.46000000000000, 7.89000000000000,
				9.05000000000000, 7.66000000000000, 8.81000000000000, 3.87000000000000, 2.41000000000000, 0,
				0.370000000000000, 3.75000000000000, 6.76000000000000, 9.40000000000000, 8.98000000000000,
				8.44000000000000, 4.28000000000000, 2.16000000000000, 0, 0.190000000000000, 0, 6.21000000000000, 0,
				7.62000000000000, 8.46000000000000, 3.93000000000000, 2.22000000000000, 0, 0.340000000000000,
				3.79000000000000, 6.48000000000000, 9.38000000000000, 7.43000000000000, 7.75000000000000,
				3.70000000000000, 1.83000000000000, 0, 0, 3.10000000000000, 0, 9.13000000000000, 8.42000000000000,
				7.73000000000000, 4.01000000000000, 1.92000000000000, 0, 0.320000000000000, 3.46000000000000,
				6.94000000000000, 8.73000000000000, 6.12000000000000, 7.54000000000000, 4.54000000000000,
				2.33000000000000, 4.56000000000000, 0.120000000000000, 3.61000000000000, 7.24000000000000, 0,
				6.72000000000000, 7.80000000000000, 4.11000000000000, 2.78000000000000, 10.5200000000000,
				0.0600000000000000, 3.71000000000000, 10.4800000000000, 10.3000000000000, 8, 7.80000000000000,
				4.01000000000000, 2.41000000000000, 10.5000000000000, 0.170000000000000, 3.62000000000000, 0,
				10.2800000000000, 8.02000000000000, 7.47000000000000, 3.88000000000000, 2.02000000000000,
				10.3500000000000, 0.0400000000000000, 0, 0, 10.1400000000000, 6.16000000000000, 7.99000000000000,
				4.08000000000000, 0, 0, 0.180000000000000, 3.35000000000000, 8.77000000000000, 11.8100000000000,
				6.48000000000000, 8.11000000000000, 4.55000000000000, 2.38000000000000, 9.77000000000000, 0,
				6.08000000000000, 8.57000000000000, 9.79000000000000, 6.07000000000000, 6.95000000000000,
				4.43000000000000, 0, 0, 4.62000000000000, 2.91000000000000, 0, 0, 5.34000000000000, 8.24000000000000,
				4.49000000000000, 2.50000000000000, 0, 0, 9.17000000000000, 9.05000000000000, 15.4000000000000, 0, 0,
				4.03000000000000, 0, 0, 0, 0, 9.03000000000000, 0, 6.45000000000000, 8.23000000000000, 4.34000000000000,
				1.92000000000000, 0, 0, 0, 8.86000000000000, 0, 5.95000000000000, 9.59000000000000, 4.34000000000000, 0,
				0, 4.70000000000000, 3.84000000000000, 8.97000000000000, 9.46000000000000, 5.93000000000000,
				8.35000000000000, 3.84000000000000, 2.32000000000000, 10.8200000000000, 0.150000000000000,
				3.79000000000000, 8.15000000000000, 0, 5.78000000000000, 7.54000000000000, 4.06000000000000,
				2.01000000000000, 10.6600000000000, 0.300000000000000, 5.83000000000000, 9.13000000000000, 0, 6,
				8.32000000000000, 0, 14.6600000000000, 0, 0, 3.80000000000000, 8.83000000000000, 9.73000000000000,
				4.96000000000000, 8.24000000000000, 3.94000000000000, 2.32000000000000, 8.88000000000000,
				0.0300000000000000, 7.14000000000000, 9.36000000000000, 0, 6.61000000000000, 8.85000000000000,
				4.58000000000000, 0, 0, 0 };

		ranges = splitArray(distanceRanges, 9);

		Point2d estimate;
		double[] temp;
		RealMatrix rm = new Array2DRowRealMatrix(ranges);
		for (int v = 0; v < ranges.length; v++) {
			temp = rm.getRow(v);

			estimate = latmath.algorithm.GeolaterationN.multilaterate(anchors, temp, doApprox, approxMethod, doFilter1,
					doFilter1IncludeApprox, filter1Limit, doFilter2, medianFactor, weightRealIntersection,
					weightApproxIntersection, CENTER_OF_MASS);

			System.out.println(v + ")" + " Final estimated position: " + estimate);
			// System.out.println(estimate.x);
			// System.out.println(estimate.y);

			/*
			 * for (int i = 0; i < temp.length; i++) { System.out.print(" " + temp[i]); }
			 * System.out.println();
			 */

		}
	}

	public static double[][] splitArray(double[] arrayToSplit, int chunkSize) {

		if (chunkSize <= 0) {
			return null;
		}

		int rest = arrayToSplit.length % chunkSize;
		int chunks = arrayToSplit.length / chunkSize + (rest > 0 ? 1 : 0);
		double[][] arrays = new double[chunks][];

		for (int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++) {
			arrays[i] = Arrays.copyOfRange(arrayToSplit, i * chunkSize, i * chunkSize + chunkSize);
		}

		if (rest > 0) {
			arrays[chunks - 1] = Arrays.copyOfRange(arrayToSplit, (chunks - 1) * chunkSize,
					(chunks - 1) * chunkSize + rest);
		}
		return arrays;
	}
}