package latmath.test;

import com.jmatio.io.*;
import com.jmatio.types.*;

public class MatlabFilesTest {

	public static void main(String[] args) {
		MatFileReader matfilereader = new MatFileReader();
		double[][] mlArrayDouble = ((MLDouble) matfilereader.getMLArray("data")).getArray();
	}

}
