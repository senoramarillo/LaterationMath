package latmath.util;

/**
 * Hoare's selection algorithm also known as quickselect algorithm.
 *
 * @version 1.0, 2012-07-04
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public class QuickSelect {

    private static final int CUTOFF = 10;
    
    public static Comparable select(Comparable[] a, int k) {
        select(a, 0, a.length - 1, k);
        return a[k-1];
    }

    @SuppressWarnings("empty-statement")
    private static void select(Comparable[] a, int low, int high, int k) {
        if (low + CUTOFF > high) {
            insertionSort(a, low, high);
        } else {
            // Sort low, middle, high
            int middle = (low + high) / 2;
            if (a[middle].compareTo(a[low]) < 0) {
                swapReferences(a, low, middle);
            }
            if (a[high].compareTo(a[low]) < 0) {
                swapReferences(a, low, high);
            }
            if (a[high].compareTo(a[middle]) < 0) {
                swapReferences(a, middle, high);
            }

            // Place pivot at position high - 1
            swapReferences(a, middle, high - 1);
            Comparable pivot = a[high - 1];

            // Begin partitioning
            int i, j;
            for (i = low, j = high - 1;;) {
                while (a[++i].compareTo(pivot) < 0);
                while (pivot.compareTo(a[--j]) < 0);
                if (i >= j) {
                    break;
                }
                swapReferences(a, i, j);
            }

            // Restore pivot
            swapReferences(a, i, high - 1);

            // Recurse on the relevant sub-array
            int pos = k - 1;
            if (pos < i) {
                select(a, low, i - 1, k);
            } else if (pos > i) {
                select(a, i + 1, high, k);
            }
        }
    }

    private static void swapReferences(Object[] a, int index1, int index2) {
        Object tmp = a[index1];
        a[index1] = a[index2];
        a[index2] = tmp;
    }

    private static void insertionSort(Comparable[] a, int low, int high) {
        for (int p = low + 1; p <= high; p++) {
            Comparable tmp = a[p];
            int j;
            for (j = p; j > low && tmp.compareTo(a[j - 1]) < 0; j--) {
                a[j] = a[j - 1];
            }
            a[j] = tmp;
        }
    }
  
}