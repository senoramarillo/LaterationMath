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

		/* Experiment anchors */

		Point2d[] anchors = new Point2d[] { new Point2d(24.5626016260160, 2.29970731707320),
				new Point2d(23.1605118679050, 8.57622125902990), new Point2d(24.7228648090820, 12.1175545923630),
				new Point2d(26.0127689243310, 9.01743205528970), new Point2d(30.8588226401950, 9.19865358007020),
				new Point2d(30.6951496849370, 1.20326677881980), new Point2d(26.5792387026600, 1.27245974100970) };

		/* Simluation 1 anchors */

		/*
		 * Point2d[] anchors = new Point2d[] { new Point2d(81.4723686393179,
		 * 15.7613081677548), new Point2d(90.5791937075619, 97.0592781760616), new
		 * Point2d(12.6986816293506, 95.7166948242946), new Point2d(91.3375856139019,
		 * 48.5375648722841), new Point2d(63.2359246225410, 80.0280468888800), new
		 * Point2d(9.75404049994095, 14.1886338627215), new Point2d(27.8498218867048,
		 * 42.1761282626275), new Point2d(54.6881519204984, 91.5735525189067), new
		 * Point2d(95.7506835434298, 79.2207329559554), new Point2d(96.4888535199277,
		 * 95.9492426392903) };
		 */

		/* Simluation 2 anchors */

		/*
		 * Point2d[] anchors = new Point2d[] { new Point2d(60.3073702165264,
		 * 35.2175659844817), new Point2d(81.5841716501210, 8.33270573668292), new
		 * Point2d(3.13157665105628, 85.5091116734689), new Point2d(86.7817347885163,
		 * 46.4432866305852), new Point2d(59.1961801162065, 25.4925550820527), new
		 * Point2d(68.7500493088393, 39.6738849688838), new Point2d(73.7707606697940,
		 * 12.2229464759885), new Point2d(21.5991846061765, 70.9440107159869), new
		 * Point2d(71.5132870399859, 38.3735161565301), new Point2d(3.61823517774231,
		 * 97.3814426907805), };
		 */

		MatFileReader matfilereader = new MatFileReader("distances_3210_7_398.mat");
		double[][] mlArrayDouble = ((MLDouble) matfilereader.getMLArray("distance_to_anchor")).getArray();
		Point2d estimate;
		double[] temp;
		RealMatrix rm = new Array2DRowRealMatrix(mlArrayDouble);

		for (int v = 0; v < mlArrayDouble.length; v++) {
			temp = rm.getRow(v);
			estimate = latmath.algorithm.GeolaterationN.multilaterate(anchors, temp, doApprox, approxMethod, doFilter1,
					doFilter1IncludeApprox, filter1Limit, doFilter2, medianFactor, weightRealIntersection,
					weightApproxIntersection, GEOMETRIC_MEDIAN);

			System.out.println(v + 1 + ")" + " Final estimated position: " + estimate);
			// System.out.println(estimate.x);
			// System.out.println(estimate.y);
		}
	}
}