package latmath.test;

import latmath.util.CombinatorialIterator;

public class CombinatorialIteratorTest {

    public static void main(String[] args) {
        CombinatorialIterator i = new CombinatorialIterator(5,3);
        StringBuffer b = new StringBuffer(80);
        while (i.hasNext()) {
            int[] next = i.next();
            b.setLength(0);
            for (int j = 0; j < next.length; ++j) {
                b.append(next[j] + 1);
                if (j + 1 < next.length)
                    b.append(", ");
            }
            System.out.println(b.toString());
        }
    }
}
