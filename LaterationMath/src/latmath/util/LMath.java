package latmath.util;

/**
 * Utility math methods.
 * 
 * @version 1.0, 2011-08-03
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class LMath {

    /** All long-representable factorials */
    private static final long[] FACTORIALS = new long[] {
        1l, 1l, 2l,
        6l, 24l, 120l,
        720l, 5040l, 40320l,
        362880l, 3628800l, 39916800l,
        479001600l, 6227020800l, 87178291200l,
        1307674368000l, 20922789888000l, 355687428096000l,
        6402373705728000l, 121645100408832000l, 2432902008176640000l
    };

    /** Calculate binomial coefficient */
    public static int binom(int n, int k) {
        if (n < k) {
            return 0;
        }

        long ret = n;
        for (int i = n - 1; i > n - k; i--) {
            ret *= i;
        }

        return (int) (ret / factorial(k));
    }

    /**
     * Calcualte factorial.
     *
     * @param n argument
     *
     * @return {@code n!}
     * @throws IllegalArgumentException if {@code n < 0} or if {@code n > 20}:
     *                                  The factorial value is too large to fit
     *                                  in a {@code long}.
     */
    public static long factorial(int n) {
        if (n < 0 || n > 20) {
            throw new IllegalArgumentException("illegal value for parameter n");
        }
        return FACTORIALS[n];
    }

    /** Utility method for k-permutation building */
    public static boolean incCounter(int[] v, int i, int n, int k) {
        v[i]++;
        return (v[i] == n - ((k - 1) - i)); // return overflow information
    }
    
}
