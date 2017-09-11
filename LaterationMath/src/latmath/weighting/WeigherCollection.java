package latmath.weighting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import latmath.util.ReflectionUtils;

/**
 * A collection of all available weighting methods in this package.
 *
 * @version 1.00, 2013-05-12
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class WeigherCollection {
    
    private static final List<Weighable> weighers;
    
    static {
        weighers = new ArrayList<>();
        Class[] classes = ReflectionUtils.getClasses("latmath", Weighable.class);
        for (Class clazz : classes) {
            weighers.add((Weighable)ReflectionUtils.createObjectFromClass(clazz));
        }
        Collections.sort(weighers, new WeighableComparator());
    }
    
    private static class WeighableComparator implements Comparator<Weighable> {
        @Override
        public int compare(Weighable o1, Weighable o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
    
    private WeigherCollection() {}
    
    /**
     * Returns a list of all available weighting methods in this package.
     *
     * @return A list of all available weighting methods.
     */
    public static List<Weighable> getWeighers() {
        return weighers;
    }
    
    /**
     * Returns an array of all available weighting methods in this package.
     * <p>
     * The elements are copies from the original list.
     *
     * @return An array of all available weighting methods.
     */
    public static Weighable[] getWeighersAsCopy() {
        Weighable[] wa = new Weighable[weighers.size()];
        for (int i = 0; i < wa.length; i++) {
            wa[i] = (Weighable) ReflectionUtils.createObjectFromClass(
                    weighers.get(i).getClass());
        }
        return wa;
    }
    
    /**
     * Get the weigher for the given class.
     * 
     * @param wa Array of weighers.
     * @param clazz The weigher class.
     * 
     * @return Weigher for given class or {@code null} if no weigher
     *         for this class could be found.
     */
    public static Weighable getByClass(Weighable[] wa, Class clazz) {
        for (Weighable w : wa) {
            if (w.getClass().getName().equals(clazz.getName())) {
                return w;
            }
        }
        return null;
    }
    
}
