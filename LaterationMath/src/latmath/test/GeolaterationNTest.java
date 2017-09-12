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
		final int APPROX_2 = 2;

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

		/*
		 * double[] ranges = new double[] { actual.distance(anchors[0]) + 30,
		 * actual.distance(anchors[1]) + 50, actual.distance(anchors[2]) + 70 };
		 */
		/*
		 * double[] ranges = {0, 0, 13.8400000000000, 3.93000000000000,
		 * 3.74000000000000, 7.30000000000000, 7.79000000000000, 0, 0, 1, 2, 3, 4, 5, 6,
		 * 7, 8, 9};
		 */

		/*
		 * double[][] ranges = { { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, { 4, 5, 6, 7, 8, 9, 1,
		 * 2, 3 }, { 9, 1, 3, 4, 3, 2, 1, 3, 4 } };
		 */

		double[][] ranges;
		double[] simpleRanges = { 0, 0, 13.8400000000000, 3.93000000000000, 3.74000000000000, 7.30000000000000,
				7.79000000000000, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		ranges = splitArray(simpleRanges, 9);

		Point2d estimate;
		double[] temp;
		RealMatrix rm = new Array2DRowRealMatrix(ranges);
		for (int v = 0; v < 2; v++) {
			temp = rm.getRow(v);
			estimate = latmath.algorithm.GeolaterationN.multilaterate(anchors, temp, doApprox, approxMethod, doFilter1,
					doFilter1IncludeApprox, filter1Limit, doFilter2, medianFactor, weightRealIntersection,
					weightApproxIntersection, CENTER_OF_MASS);
			System.out.println("Final estimated position: " + estimate);
			// System.out.println("x: " + estimate.x);
			// System.out.println("y: " + estimate.y);
			/*
			 * for (int i = 0; i < temp.length; i++) { System.out.print(" " + temp[i]); }
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