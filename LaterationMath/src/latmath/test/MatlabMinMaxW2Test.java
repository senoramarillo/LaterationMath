package latmath.test;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import latmath.util.Point2d;

public class MatlabMinMaxW2Test {

	// use W-2 weight as it is optimum of paper
	public static void main(String[] args) throws FileNotFoundException, IOException {

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
			estimate = latmath.algorithm.EMinMaxW2.multilaterate(anchors, temp);

			System.out.println(v + 1 + ")" + " Final estimated position: " + estimate);
			// System.out.println(estimate.x);
			// System.out.println(estimate.y);
		}
	}
}