package latmath.util;

import java.awt.Frame;

/**
 * Standard configuration API for all algorithms and filters.
 *
 * @version 1.1, 2013-06-22
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public interface Configurable {

    /**
     * Tests, if this object is configurable.
     *
     * @return {@code true} if this object is configurable;
     *         {@code false} otherwise.
     */
    boolean isConfigurable();

    /**
     * If this object is configurable, a dialog to configure the
     * object is shown.
     *
     * @param parent The parent frame of the new dialog.
     * 
     * @return {@code true} if settings have changed;
     *         {@code false} otherwise.
     */
    boolean configure(Frame parent);

}
