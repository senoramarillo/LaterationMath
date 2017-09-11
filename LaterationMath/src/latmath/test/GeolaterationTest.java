package latmath.test;

import latmath.util.Point2d;

/**
 * Debugging Geolateration with given coordinates.
 */
public class GeolaterationTest {

    public static void main(String[] args) {
        Point2d actual = new Point2d(300, 700);
        Point2d[] anchors = new Point2d[] {new Point2d(0,0), new Point2d(999,999), new Point2d(990,0)};
        double[] ranges = new double[] {actual.distance(anchors[0])+30,
        actual.distance(anchors[1])+50, actual.distance(anchors[2])+70};
        Point2d estimate = latmath.algorithm.Geolateration.trilaterate(anchors, ranges);
        System.out.println(estimate);
    }

}