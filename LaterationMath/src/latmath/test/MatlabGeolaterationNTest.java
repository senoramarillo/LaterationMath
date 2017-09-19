package latmath.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import latmath.util.Point2d;

public class MatlabGeolaterationNTest {
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

	public static void main(String[] args) throws FileNotFoundException, IOException {

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
				new Point2d(30.6951496849370, 1.20326677881980), new Point2d(26.5792387026600, 1.27245974100970) };

		MatFileReader matfilereader = new MatFileReader("distance_7_398.mat");
		double[][] mlArrayDouble = ((MLDouble) matfilereader.getMLArray("distance_to_anchor")).getArray();
		Point2d estimate;
		double[] temp;
		RealMatrix rm = new Array2DRowRealMatrix(mlArrayDouble);
		for (int v = 0; v < mlArrayDouble.length; v++) {
			temp = rm.getRow(v);
			estimate = latmath.algorithm.GeolaterationN.multilaterate(anchors, temp, doApprox, approxMethod, doFilter1,
					doFilter1IncludeApprox, filter1Limit, doFilter2, medianFactor, weightRealIntersection,
					weightApproxIntersection, CENTER_OF_MASS);

			System.out.println(v + 1 + ")" + " Final estimated position: " + estimate);
			//System.out.println(estimate.x);
			//System.out.println(estimate.y);
		}
	}
}