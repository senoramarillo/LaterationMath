package latmath.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import latmath.util.Point2d;

public class MatlabEMinMaxW4Test {

	// use W-2 weight as it is optimum of paper
	public static void main(String[] args) throws FileNotFoundException, IOException {

		/*
		 * Point2d[] anchors = new Point2d[] { new Point2d(24.5626016260160,
		 * 2.29970731707320), new Point2d(23.1605118679050, 8.57622125902990), new
		 * Point2d(24.7228648090820, 12.1175545923630), new Point2d(26.0127689243310,
		 * 9.01743205528970), new Point2d(30.8588226401950, 9.19865358007020), new
		 * Point2d(30.6951496849370, 1.20326677881980), new Point2d(26.5792387026600,
		 * 1.27245974100970) };
		 */

		/* Simluation 1 anchors */
		Point2d[] anchors = new Point2d[] { new Point2d(81.4723686393179, 15.7613081677548),
				new Point2d(90.5791937075619, 97.0592781760616), new Point2d(12.6986816293506, 95.7166948242946),
				new Point2d(91.3375856139019, 48.5375648722841), new Point2d(63.2359246225410, 80.0280468888800),
				new Point2d(9.75404049994095, 14.1886338627215), new Point2d(27.8498218867048, 42.1761282626275),
				new Point2d(54.6881519204984, 91.5735525189067), new Point2d(95.7506835434298, 79.2207329559554),
				new Point2d(96.4888535199277, 95.9492426392903) };

		MatFileReader matfilereader = new MatFileReader("distances_generated100.mat");
		double[][] mlArrayDouble = ((MLDouble) matfilereader.getMLArray("distance_to_anchor")).getArray();
		Point2d estimate;
		double[] temp;
		RealMatrix rm = new Array2DRowRealMatrix(mlArrayDouble);
		for (int v = 0; v < mlArrayDouble.length; v++) {
			temp = rm.getRow(v);
			estimate = latmath.algorithm.EMinMaxW4.multilaterate(anchors, temp);

			System.out.println(v + 1 + ")" + " Final estimated position: " + estimate);
			// System.out.println(estimate.x);
			// System.out.println(estimate.y);
		}
	}
}