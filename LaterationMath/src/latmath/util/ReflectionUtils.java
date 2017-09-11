package latmath.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility methods for class loading and object instantiation.
 *
 * @version 1.0, 2012-02-25
 * @author  Thomas Hillebrandt <t.hillebrandt@t-online.de>
 * @since   LatMath 1.0
 */
public final class ReflectionUtils {

    private ReflectionUtils() {}

    /**
     * Creates a new instance of the given class.
     *
     * @param clazz The class name of the class to be created.
     *
     * @return The new instance of the given class or
     *         <code>null</code> if the creation failed.
     */
    public static Object createObjectFromClass(String clazz) {
        try {
            return createObjectFromClass(Class.forName(clazz));
        } catch (ClassNotFoundException e) {}
        return null;
    }

    /**
     * Creates a new instance of the given class.
     *
     * @param clazz The class name of the class to be created.
     *
     * @return The new instance of the given class or
     *         <code>null</code> if the creation failed.
     */
    public static Object createObjectFromClass(Class clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {}
        return null;
    }

    /**
     * Scans all classes accessible from the context class loader which
     * belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @param baseClass The base class of all classes or {@code null} if the
     *                  classes can be of any type.
     *
     * @return The classes found (may be none).
     */
    public static Class[] getClasses(String packageName, Class baseClass) {
        try {
            return getClasses0(packageName, baseClass);
        } catch (ClassNotFoundException | IOException e) {
            return new Class[0];
        }
    }

    /**
     * Test if given class is valid by means of base class.
     *
     * @param clazz The class to be tested.
     * @param baseClass The base class.
     *
     * @return {@code true} if class is valid by means of base class;
     *         {@code false} otherwise.
     */
    private static boolean isValidByBaseClass(Class clazz, Class baseClass) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())
                && baseClass.isAssignableFrom(clazz)
                && Releasable.class.isAssignableFrom(clazz);
    }

    /**
     * Scans all classes accessible from the context class loader which
     * belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @param baseClass The base class of all classes or {@code null} if the
     *                  classes can be of any type.
     *
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException If I/O errors occur
     */
    private static Class[] getClasses0(String packageName, Class baseClass)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        ArrayList<Class> classes = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("jar")) {
                String jarFileName = URLDecoder.decode(resource.getFile(), "UTF-8");
                jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
                JarFile jarFile = new JarFile(jarFileName);
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {
                    JarEntry entry = e.nextElement();
                    if (!entry.isDirectory()) {
                        String s = entry.getName();
                        if (s.startsWith(packageName) && s.endsWith(".class")) {
                            s = s.replaceAll("/", ".");
                            Class clazz = Class.forName(s.substring(0, s.lastIndexOf(".class")));
                            if (baseClass != null && !isValidByBaseClass(clazz, baseClass)) {
                                continue;
                            }
                            classes.add(clazz);
                        }
                    }
                }
            } else {
                classes.addAll(findClasses(new File(resource.getFile()),
                        packageName, baseClass));
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @param baseClass   The base class of all classes or {@code null} if the
     *                    classes can be of any type.
     *
     * @return The classes
     * @throws ClassNotFoundException If the class cannot be located
     */
    private static List<Class> findClasses(File directory, String packageName, Class baseClass) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName(), baseClass));
            } else if (file.getName().endsWith(".class")) {
                Class clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (baseClass != null && !isValidByBaseClass(clazz, baseClass)) {
                    continue;
                }
                classes.add(clazz);
            }
        }
        return classes;
    }

}
