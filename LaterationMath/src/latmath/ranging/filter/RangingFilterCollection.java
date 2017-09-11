package latmath.ranging.filter;

import java.util.ArrayList;
import java.util.List;
import latmath.util.ReflectionUtils;

/**
 * A collection of all available ranging filters in this package.
 *
 * @version 1.0, 2012-02-27
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class RangingFilterCollection {

    private static final List<RangingFilter> filters;

    static {
        filters = new ArrayList<>();
        Class[] classes = ReflectionUtils.getClasses("latmath",
                RangingFilter.class);
        for (Class clazz : classes) {
            filters.add((RangingFilter)
                    ReflectionUtils.createObjectFromClass(clazz));
        }
    }

    private RangingFilterCollection() {}

    /**
     * Register an additional ranging filter.
     *
     * @param filter The filter to be added to the list.
     */
    public static void registerFilter(RangingFilter filter) {
        filters.add(filter);
    }

    /**
     * Returns a list of all available ranging filters in this package.
     *
     * @return A list of all available ranging filters.
     */
    public static List<RangingFilter> getFilters() {
        return filters;
    }

}
