package latmath.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Enumerate combinatorial numbers.
 *
 * @version 1.0, 2012-02-24
 * @author  Marcel Kyas <marcel.kyas@fu-berlin.de>
 * @since   LatMath 1.0
 */
public class CombinatorialIterator implements Iterator<int[]> {

    public final int N;
    public final int K;
    private int[] state;
    private int[] current;

    public CombinatorialIterator(int n, int k) {
        if (n <= 0 || k > n)
            throw new IllegalArgumentException();
        this.N = n;
        this.K = k;
        // Extend the state vector by a sentinel initialised to N
        // in order to simplify some computations.
        this.state = new int[k+1];
        this.state[K] = N;
        this.current = new int[k];
        for (int i = 0; i < K; ++i)
            this.state[i] = i;
    }

    @Override
    public boolean hasNext() {
        for (int i = 0; i < K; ++i) {
            if (this.state[i] < N - K + i)
                return true;
        }
        return false;
    }

    @Override
    public int[] next() {
        if (!this.hasNext())
            throw new NoSuchElementException();
        // state holds the next element, we save it to current.
        System.arraycopy(state, 0, current, 0, K);
        // Compute the next element in the vector.
        for (int i = 0; i < K; ++i) {
            if (this.state[i] + 1 < this.state[i + 1]) {
                this.state[i]++;
                break;
            } else {
                this.state[i] = i;
            }
        }
        return this.current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
