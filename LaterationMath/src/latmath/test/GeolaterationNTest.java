package latmath.test;

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
		medianFactor = CENTER_OF_MASS;

		Point2d actual = new Point2d(300, 700);

		Point2d[] anchors = new Point2d[] { new Point2d(0, 0), new Point2d(999, 999), new Point2d(990, 0) };

		double[] ranges = new double[] { actual.distance(anchors[0]) + 30, actual.distance(anchors[1]) + 50,
				actual.distance(anchors[2]) + 70 };

		for (int i = 0; i < anchors.length; i++) {
			System.out.println(i + ") anchors: " + anchors[i]);
		}

		for (int i = 0; i < ranges.length; i++) {
			System.out.println(i + ") ranges: " + ranges[i]);
		}

		Point2d estimate = latmath.algorithm.GeolaterationN.multilaterate(anchors, ranges, doApprox, approxMethod,
				doFilter1, doFilter1IncludeApprox, filter1Limit, doFilter2, medianFactor, weightRealIntersection,
				weightApproxIntersection, CENTER_OF_MASS);
		
		System.out.println("Final estimated position: " + estimate);

	}

}