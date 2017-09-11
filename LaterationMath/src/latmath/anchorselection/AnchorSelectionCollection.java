package latmath.anchorselection;

import java.util.ArrayList;
import java.util.List;
import latmath.util.ReflectionUtils;

/**
 * A collection of all available anchor selection algorithms in this package.
 *
 * @version 1.1, 2012-02-25
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class AnchorSelectionCollection {

    private static final List<AnchorSelection> selAlgs;

    static {
        selAlgs = new ArrayList<>();
        Class[] classes = ReflectionUtils.getClasses("latmath",
                AnchorSelection.class);
        for (Class clazz : classes) {
            selAlgs.add((AnchorSelection)
                    ReflectionUtils.createObjectFromClass(clazz));
        }
    }

    private AnchorSelectionCollection() {}

    /**
     * Register an additional anchor selection algorithm.
     *
     * @param anchorSelection The anchor selection algorithm to be added to the list.
     */
    public static void registerAnchorSelectionAlgorithm(AnchorSelection anchorSelection) {
        selAlgs.add(anchorSelection);
    }

    /**
     * Returns a list of all available anchor selection algorithms in this package.
     *
     * @return A list of all available anchor selection algorithms.
     */
    public static List<AnchorSelection> getAnchorSelectionAlgorithms() {
        return selAlgs;
    }

}
