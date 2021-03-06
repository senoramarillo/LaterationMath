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
		// final int APPROX_2 = 2;

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

		double[] distanceRanges = { 3.46000000000000, 6.94000000000000, 8.73000000000000, 6.12000000000000,
				7.54000000000000, 4.54000000000000, 2.33000000000000, 4.56000000000000, 0.120000000000000,
				3.71000000000000, 10.4800000000000, 10.3000000000000, 8, 7.80000000000000, 4.01000000000000,
				2.41000000000000, 10.5000000000000, 0.170000000000000 };

		ranges = splitArray(distanceRanges, 9);

		Point2d estimate;
		double[] temp;
		RealMatrix rm = new Array2DRowRealMatrix(ranges);
		for (int v = 0; v < ranges.length; v++) {
			temp = rm.getRow(v);

			estimate = latmath.algorithm.GeolaterationN.multilaterate(anchors, temp, doApprox, approxMethod, doFilter1,
					doFilter1IncludeApprox, filter1Limit, doFilter2, medianFactor, weightRealIntersection,
					weightApproxIntersection, CENTER_OF_MASS);

			System.out.println(v + 1 + ")" + " Final estimated position: " + estimate);
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