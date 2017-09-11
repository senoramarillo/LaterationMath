package latmath.test;

import latmath.util.Combinations;

public class CombinationsTest {

    public static void main(String[] args) {
        Combinations combinations = new Combinations(5,3);
        StringBuffer b = new StringBuffer(80);
        for (int[] combination: combinations) {
            b.setLength(0);
            for (int j = 0; j < combination.length; ++j) {
                b.append(combination[j] + 1);
                if (j + 1 < combination.length)
                    b.append(", ");
            }
            System.out.println(b.toString());
        }
    }
}
