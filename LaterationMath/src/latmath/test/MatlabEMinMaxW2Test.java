package latmath.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import latmath.util.Point2d;

public class MatlabEMinMaxW2Test {

	// use W-2 weight as it is optimum of paper
	public static void main(String[] args) throws FileNotFoundException, IOException {

		/* Simluation 1 anchors */
		/*
		 * Point2d[] anchors = new Point2d[] { new Point2d(81.4723686393179,
		 * 15.7613081677548), new Point2d(90.5791937075619, 97.0592781760616), new
		 * Point2d(12.6986816293506, 95.7166948242946), new Point2d(91.3375856139019,
		 * 48.5375648722841), new Point2d(63.2359246225410, 80.0280468888800), new
		 * Point2d(9.75404049994095, 14.1886338627215), new Point2d(27.8498218867048,
		 * 42.1761282626275), new Point2d(54.6881519204984, 91.5735525189067), new
		 * Point2d(95.7506835434298, 79.2207329559554), new Point2d(96.4888535199277,
		 * 95.9492426392903)};
		 */

		/* Simluation 2 anchors */

		Point2d[] anchors = new Point2d[] { new Point2d(60.3073702165264, 35.2175659844817),
				new Point2d(81.5841716501210, 8.33270573668292), new Point2d(3.13157665105628, 85.5091116734689),
				new Point2d(86.7817347885163, 46.4432866305852), new Point2d(59.1961801162065, 25.4925550820527),
				new Point2d(68.7500493088393, 39.6738849688838), new Point2d(73.7707606697940, 12.2229464759885),
				new Point2d(21.5991846061765, 70.9440107159869), new Point2d(71.5132870399859, 38.3735161565301),
				new Point2d(3.61823517774231, 97.3814426907805) };

		MatFileReader matfilereader = new MatFileReader("scenario2_distances.mat");
		double[][] mlArrayDouble = ((MLDouble) matfilereader.getMLArray("distance_to_anchor")).getArray();
		Point2d estimate;
		double[] temp;
		RealMatrix rm = new Array2DRowRealMatrix(mlArrayDouble);
		for (int v = 0; v < mlArrayDouble.length; v++) {
			temp = rm.getRow(v);
			estimate = latmath.algorithm.EMinMaxW2.multilaterate(anchors, temp);

			System.out.println(v + 1 + ")" + " Final estimated position: " + estimate);
			// System.out.println(estimate.x);
			// System.out.println(estimate.y);
		}
	}
}