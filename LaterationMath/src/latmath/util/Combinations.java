package latmath.util;

import java.util.Iterator;

/**
 * Simulate a collection of combinations.
 *
 * @version 1.0, 2012-02-24
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class Combinations implements Iterable<int[]> {

    public final int N;
    public final int K;

    public Combinations(int n, int k) {
        if (n <= 0 || k > n)
            throw new IllegalArgumentException();
        this.N = n;
        this.K = k;
    }

    @Override
    public Iterator<int[]> iterator() {
        return new CombinatorialIterator(N, K);
    }
}
