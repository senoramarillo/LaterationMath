package latmath.location.filter;

import java.util.ArrayList;
import java.util.List;
import latmath.util.ReflectionUtils;

/**
 * A collection of all available location filters in this package.
 *
 * @version 1.1, 2012-02-25
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class LocationFilterCollection {

    private static final List<LocationFilter> filters;

    static {
        filters = new ArrayList<>();
        Class[] classes = ReflectionUtils.getClasses("latmath",
                LocationFilter.class);
        for (Class clazz : classes) {
            filters.add((LocationFilter)
                    ReflectionUtils.createObjectFromClass(clazz));
        }
    }

    private LocationFilterCollection() {}

    /**
     * Register an additional location filters.
     *
     * @param filter The filter to be added to the list.
     */
    public static void registerFilter(LocationFilter filter) {
        filters.add(filter);
    }

    /**
     * Returns a list of all available location filters in this package.
     *
     * @return A list of all available location filters.
     */
    public static List<LocationFilter> getFilters() {
        return filters;
    }

}
