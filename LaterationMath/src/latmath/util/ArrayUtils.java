package latmath.util;

/**
 * Array util methods.
 *
 * @version 1.0, 2011-12-10
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class ArrayUtils {

    public static void fill(double[] a, double val) {
        for (int i = 0; i < a.length; i++) {
            a[i] = val;
        }
    }

}
