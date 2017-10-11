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
		/*
		 * Point2d[] anchors = new Point2d[] { new Point2d(24.5626016260160,
		 * 2.29970731707320), new Point2d(23.1605118679050, 8.57622125902990), new
		 * Point2d(24.7228648090820, 12.1175545923630), new Point2d(26.0127689243310,
		 * 9.01743205528970), new Point2d(30.8588226401950, 9.19865358007020), new
		 * Point2d(30.6951496849370, 1.20326677881980), new Point2d(26.5792387026600,
		 * 1.27245974100970) };
		 */

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

		Point2d[] anchors = new Point2d[] { new Point2d(0.864768787809034, 63.3333580320122),
				new Point2d(72.7079601148527, 62.4000554312344), new Point2d(35.4116466056586, 32.7941596973249),
				new Point2d(78.0445946096025, 80.2965315958237), new Point2d(43.6656641635291, 99.9477858635892),
				new Point2d(43.6554782372268, 98.0978160932146), new Point2d(4.92131803564864, 12.7036942194857),
				new Point2d(4.96319015193902, 23.2240145961793), new Point2d(9.11001754130539, 2.36324666582251),
				new Point2d(59.4037031444121, 60.7432610401855), new Point2d(24.1084055169022, 11.0809321287150),
				new Point2d(84.1369101972144, 40.7459507878399), new Point2d(85.7212764090606, 88.4076806661962),
				new Point2d(96.3612200951355, 54.8132777476588), new Point2d(48.8899786160604, 36.9003076711617),
				new Point2d(22.0310100568633, 20.8345982813347), new Point2d(22.6208640841286, 44.0943276524324),
				new Point2d(53.6787804512826, 95.6196152175878), new Point2d(76.2109709211147, 12.4025916648043),
				new Point2d(34.7567150448759, 47.0763245866957), new Point2d(46.1231759391499, 85.6896327782193),
				new Point2d(63.9323762199356, 4.33904715669532), new Point2d(91.7336040866845, 69.1625145201306),
				new Point2d(16.1572573890331, 97.8985466675039), new Point2d(71.5635404167473, 28.3267898492137),
				new Point2d(57.7738876192409, 13.3780499994423), new Point2d(43.3298977209119, 68.5279684412687),
				new Point2d(88.4242782631094, 90.9454555749395), new Point2d(39.3051755376913, 61.0868982383243),
				new Point2d(17.8975152627732, 89.9982796432861) };

		MatFileReader matfilereader = new MatFileReader("scenario/distances_200_30_5.mat");
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