package latmath.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import latmath.util.ReflectionUtils;

/**
 * A collection of all available lateration algorithms in this package.
 *
 * @version 1.4, 2012-02-25
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class LaterationAlgorithmCollection {

    private static final List<BasicLaterationAlgorithm> algorithms;

    static {
        algorithms = new ArrayList<>();
        Class[] classes = ReflectionUtils.getClasses("latmath",
                BasicLaterationAlgorithm.class);
        for (Class clazz : classes) {
            algorithms.add((BasicLaterationAlgorithm)
                    ReflectionUtils.createObjectFromClass(clazz));
        }
        Collections.sort(algorithms, new BasicLaterationAlgorithmComparator());
    }
    
    private static class BasicLaterationAlgorithmComparator implements Comparator<BasicLaterationAlgorithm> {
        @Override
        public int compare(BasicLaterationAlgorithm o1, BasicLaterationAlgorithm o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private LaterationAlgorithmCollection() {}

    /**
     * Register an additional lateration algorithm.
     *
     * @param algorithm The lateration algorithm to be added to the list.
     */
    public static void registerLaterationAlgorithm(BasicLaterationAlgorithm algorithm) {
        algorithms.add(algorithm);
    }

    /**
     * Returns a list of all available lateration algorithms in this package.
     *
     * @return A list of all available lateration algorithms.
     */
    public static List<BasicLaterationAlgorithm> getAlgorithms() {
        return getAlgorithms(true);
    }
    
    /**
     * Returns a list of all available lateration algorithms in this package.
     * 
     * @param includeLBE {@code true} if Lower Bound Estimators should be included;
     *                   {@code false} otherwise.
     * 
     * @return A list of all available lateration algorithms.
     */
    public static List<BasicLaterationAlgorithm> getAlgorithms(boolean includeLBE) {
        if (includeLBE) {
            return algorithms;
        } else {
            List<BasicLaterationAlgorithm> tmp = new ArrayList<>();
            for (BasicLaterationAlgorithm alg : algorithms) {
                if (!(alg instanceof LowerBoundEstimator)) {
                    tmp.add(alg);
                }
            }
            return tmp;
        }
    }

}
