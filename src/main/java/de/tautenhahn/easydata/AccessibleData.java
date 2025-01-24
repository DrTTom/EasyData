package de.tautenhahn.easydata;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;


/**
 * Wraps complete data access and resolves any requests for data elements.<br>
 * First idea was to wrap all data elements into separate access objects of one base type but that looked ugly
 * because the needed types are different after all. <br>
 * Error reporting uses only the given path values, information about where those values came from may be
 * added by the caller.
 *
 * @author TT
 */
public class AccessibleData {

    private static final Pattern DEREF = Pattern.compile("\\$\\{([^}]+)}");

    private static final Pattern SIZE = Pattern.compile("SIZE\\(([^)]+)\\)");

    private static final Pattern SIZE2 = Pattern.compile("SIZE\\{([^}]+)\\}");

    private static final Pattern LITERAL = Pattern.compile("\"[^\"]*\"|'[^']*'|#[^#]*#");

    public static final String VALUE_READ_MISSES = "VALUE_READ_MISSES";

    private final Object data;

    private final Map<String, Object> additionalValues = new StackMap<>();

    private final Map<String, String> replacements = new HashMap<>();

    protected final List<BiFunction<Object, String, Object>> formatters = new ArrayList<>();

    private boolean throwOnValueReadMiss;

    /**
     * Defines how to iterate over a complex type.
     */
    public enum ListMode {
        /**
         * use elements of lists or arrays, keys for maps or other complex objects
         */
        DEFAULT,
        /**
         * use map keys, attribute names or numeric strings for arrays and lists
         */
        KEYS,
        /**
         * use values always
         */
        VALUES
    }

    /**
     * Returns new instance wrapping a Map or Java Bean.
     *
     * @param data given data
     * @return new instance
     */
    public static AccessibleData byBean(Object data) {
        return new AccessibleData(data);
    }

    /**
     * Returns new instance wrapping data specified by JSON string.
     *
     * @param data given data
     * @return new instance
     */
    public static AccessibleData byJsonContent(String data) {
        return new AccessibleData(new Gson().fromJson(data, Map.class));
    }

    /**
     * Returns new instance wrapping data specified by JSON file.
     *
     * @param file path to a JSON file
     * @return new instance
     * @throws IOException in case of streaming problems
     */
    public static AccessibleData byJsonPath(String file) throws IOException {
        try (InputStream json = new FileInputStream(file);
             Reader reader = new InputStreamReader(json, StandardCharsets.UTF_8)) {
            return byJsonReader(reader);
        }
    }

    /**
     * Returns new instance wrapping data specified by JSON reader.
     *
     * @param data given data
     * @return new instance
     */
    public static AccessibleData byJsonReader(Reader data) {
        return new AccessibleData(new Gson().fromJson(data, Map.class));
    }

    /**
     * Creates new instance wrapping a Map or Java Bean.
     *
     * @param data given data
     */
    private AccessibleData(Object data) {
        this.data = data;
    }

    /**
     * Copy constructor.
     *
     * @param original shares formatters and bean with new object: both are supposed not to change
     */
    protected AccessibleData(AccessibleData original) {
        data = original.data;
        throwOnValueReadMiss = original.throwOnValueReadMiss;
        formatters.addAll(original.formatters);
    }

    /**
     * Adds a formatter to be used by the {@link #getString(String)} method.
     *
     * @param formatter Parameters are the Object to format and the path where that object was obtained from.
     *                  Return value should either be a formatted String representation of the given object or the
     *                  given object itself, if the formatter is not applicable.
     */
    public void addFormatter(BiFunction<Object, String, Object> formatter) {
        this.formatters.add(formatter);
    }

    /**
     * @param attrName dot notation and literals supported
     * @return the attribute of specified name, of whatever type. May return null if the attribute does not
     * exist but parent object does. Accepts constants if surrounded by quotes.
     * @throws IllegalArgumentException in case an intermediate object is null or primitive.
     */
    public Object get(String attrName) {
        if ("NULL".equals(attrName)) {
            return null;
        }
        if ("true".equalsIgnoreCase(attrName)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(attrName)) {
            return Boolean.FALSE;
        }
        if (LITERAL.matcher(attrName).matches()) {
            return attrName.substring(1, attrName.length() - 1);
        }
        if (attrName.matches("\\d+")) {
            return Integer.valueOf(attrName);
        }
        for (Pattern p : new Pattern[]{SIZE, SIZE2}) {
            Matcher m = p.matcher(attrName);
            if (m.matches()) {
                return getCollection(m.group(1), ListMode.DEFAULT).size();
            }
        }

        String attr = resolveInnerExpressions(attrName);
        String[] path = attr.split("\\.");
        return get(path, 0, additionalValues.containsKey(path[0]) ? additionalValues : data);
    }

    /**
     * Same as {@link #get(String)} but returns String and throws Exception if target is complex.
     *
     * @param attrName see {@link #get(String)}
     * @return string value
     */
    public String getString(String attrName) {
        if (VALUE_READ_MISSES.equals(attrName)) {
            return Optional.ofNullable(additionalValues.get(attrName)).map(Object::toString).orElse("");
        }
        Object result = get(attrName);
        if (result != null && (result instanceof Map || result instanceof List || result.getClass().isArray())) {
            throw new ResolverException("expected String but " + attrName + " is of type "
                    + result.getClass().getName());
        }
        for (BiFunction<Object, String, Object> formatter : formatters) {
            result = formatter.apply(result, attrName);
        }
        return Optional.ofNullable(result).map(Object::toString).map(this::sanitize).map(this::applyReplacements).orElse("null");
    }

    /**
     * Sanitizes the string. Default implementation does nothing.
     *
     * @param s input string
     * @return same as s but without elements which may cause problems in the target document.
     */
    protected String sanitize(String s) {
        return s;
    }

    private String applyReplacements(String value) {
        String result = value;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Replaces each object by its addressed attribute.
     *
     * @param original input data
     * @param attrName attribute to select
     * @return new collection
     */
    public Collection<Object> map(Collection<Object> original, String attrName) {
        return original.stream().map(o -> get(attrName.split("\\."), 0, o)).collect(Collectors.toList());
    }

    /**
     * Returns a list of sorted elements. This method calls {@link #get(String[], int, Object)} too frequently,
     * optimize in case of performance problems.
     *
     * @param original  content to sort
     * @param attrName  specifies attribute to sort by
     * @param ascending specifies order
     * @return sorted list
     */
    public List<Object> sort(Collection<Object> original, String attrName, boolean ascending) {
        List<Object> result = new ArrayList<>(original);
        if (attrName == null) {
            result.sort((a, b) -> compare(a, b, ascending));
        } else {
            String[] path = attrName.split("\\.");
            result.sort((a, b) -> compare(get(path, 0, a), get(path, 0, b), ascending));
        }
        return result;
    }

    /**
     * Define additional value. This will shadow any already defined value by that key.
     *
     * @param name  must be simple name
     * @param value must be of some supported type.
     */
    public void define(String name, Object value) {
        additionalValues.put(name, value);
    }

    /**
     * Undoes {@link #define(String, Object)}. The value associated by the key before last define call (if any)
     * is restored.
     *
     * @param name specifies the attribute to remove
     */
    public void undefine(String name) {
        additionalValues.remove(name);
    }

    /**
     * Defines a string replacement to be applied to all results of {@link #getString(String)}. That allows
     * to use content defined in the template document within the data strings. Reason for that feature was an
     * application which builds large String items and inserts them into a Word document. Those String item should
     * contain paragraphs.
     *
     * @param key   String to be replaced
     * @param value replacement value
     */
    public void defineReplacement(String key, String value) {
        replacements.put(key, value);
    }

    /**
     * Undefines all replacements from {@link #defineReplacement(String, String)}
     */
    public void clearReplacements() {
        replacements.clear();
    }

    /**
     * @return original wrapped data
     */
    public Object getData() {
        return data;
    }

    private Object get(String[] path, int alreadyResolved, Object element) {
        if (alreadyResolved == path.length) {
            return element;
        }
        if (element == null) {
            recordValueReadMiss(path, alreadyResolved, "null");
            return null;
        }
        Object attr = getAttribute(element, path, alreadyResolved);
        return get(path, alreadyResolved + 1, attr);
    }

    private Object getAttribute(Object element, String[] path, int alreadyResolved) {
        String attrName = path[alreadyResolved];
        if (element instanceof Map map) {
            return map.get(attrName);
        }
        if (element instanceof List list) {
            return selectByIndex(path, alreadyResolved, list.size(), list::get);
        }
        if (element.getClass().isArray()) {
            return selectByIndex(path, alreadyResolved, Array.getLength(element), i -> Array.get(element, i));
        }
        return callGetMethod(path, alreadyResolved, element);
    }

    private Object selectByIndex(String[] path, int alreadyResolved, int size, IntFunction<Object> getter) {
        try {
            int result = Integer.parseInt(path[alreadyResolved]);
            if (result >= 0 || result < size) {
                return getter.apply(result);
            }
        } catch (NumberFormatException e) // NOPMD same handling needed as in case without Exception
        {
            // empty on purpose
        }
        recordValueReadMiss(path, alreadyResolved, "a Collection with " + size + " elements");
        return null;
    }

    private Object callGetMethod(String[] path, int alreadyResolved, Object element) {
        String attrName = path[alreadyResolved];
        try {
            String upperName = Character.toUpperCase(attrName.charAt(0)) + attrName.substring(1);
            Method method;
            try {
                method = element.getClass().getMethod("get" + upperName);
            } catch (NoSuchMethodException e) {
                method = element.getClass().getMethod("is" + upperName);
            }
            return method.invoke(element);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                 NoSuchMethodException e) {
            recordValueReadMiss(path, alreadyResolved, element.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Compares two values, considering the case that both are numeric.
     *
     * @param a         one value
     * @param b         other value
     * @param ascending true for natural order, false to reverse
     * @return see {@link Comparator}
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static int compare(Object a, Object b, boolean ascending) {
        int direction = ascending ? 1 : -1;
        if (isNumericString(a) && isNumericString(b)) {
            return Double.valueOf((String) a).compareTo(Double.valueOf((String) b)) * direction;
        }
        if (a instanceof Comparable) {
            return ((Comparable) a).compareTo(b) * direction;
        }
        return a.toString().compareTo(b.toString()) * direction;
    }

    private static boolean isNumericString(Object value) {
        return value instanceof String && ((String) value).matches("-?\\d+(\\.\\d+)?");
    }

    private String resolveInnerExpressions(String attrName) {
        String result = attrName;
        result = result.replaceAll("\\[([^]]+)]", ".\\${$1}");
        Matcher resolveFirst = DEREF.matcher(result);
        while (resolveFirst.find()) {
            result = result.replace(resolveFirst.group(0), getString(resolveFirst.group(1)));
            resolveFirst = DEREF.matcher(result);
        }
        return result;
    }

    private List<Object> indexList(int size) {
        List<Object> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(Integer.toString(i));
        }
        return result;
    }

    /**
     * @param attrName see {@link #get(String)}
     * @param mode     specifies in case of maps whether keys or values are used.
     * @return a collection of sub-elements.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Object> getCollection(String attrName, ListMode mode) {
        Object target = get(attrName);
        if (target == null || target instanceof String || target.getClass().isPrimitive()) {
            String className = Optional.ofNullable(target).map(Object::getClass).map(Class::getName).orElse("null value");
            recordValueReadMiss(new String[]{attrName, "to collection"}, 1, className);
            return Collections.emptyList();
        }
        if (target instanceof Map) {
            return mode == ListMode.VALUES ? ((Map) target).values() : ((Map) target).keySet();
        }
        if (target instanceof List) {
            return mode == ListMode.KEYS ? indexList(((List<?>) target).size()) : (List) target;
        }
        if (target.getClass().isArray()) {
            return mode == ListMode.KEYS ? indexList(Array.getLength(target)) : Arrays.asList((Object[]) target);
        }
        return beanToList(attrName, mode, target);
    }

    /**
     * Make the object throw an exception when trying to resolve non-existing data. That allows other classes to add
     * more information to that exception, for instance which tag is currently being resolved. Activate that mode
     * for debugging.
     *
     * @param throwOnValueReadMiss default is false
     */
    public void setThrowOnValueReadMiss(boolean throwOnValueReadMiss) {
        this.throwOnValueReadMiss = throwOnValueReadMiss;
    }

    private Collection<Object> beanToList(String attrName, ListMode mode, Object target) {
        if (target == null) {
            return Collections.emptyList();
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            List<Object> result = new ArrayList<>();
            for (PropertyDescriptor ds : pds) {
                Method readMethod = ds.getReadMethod();
                if (readMethod != null && !"class".equals(ds.getName())) {
                    result.add(mode == ListMode.KEYS ? ds.getName() : readMethod.invoke(target));
                }
            }
            return result;
        } catch (IntrospectionException | ReflectiveOperationException | IllegalArgumentException e) {
            recordValueReadMiss(new String[]{attrName, "to collection"}, 1, target.getClass().getName());
            return Collections.emptyList();
        }
    }

    private void recordValueReadMiss(String[] path, int alreadyResolved, String msg) {
        String resolvedPath = String.join(".", Arrays.copyOfRange(path, 0, alreadyResolved));
        String remainingPath = String.join(".", Arrays.copyOfRange(path, alreadyResolved, path.length));
        String message = "cannot resolve '" + remainingPath + "' because value of '" + resolvedPath + "' is " + msg;
        if (throwOnValueReadMiss) {
            throw new ResolverException(message);
        }
        List<String> misses = (List<String>) additionalValues.getOrDefault(VALUE_READ_MISSES, new ArrayList<>());
        misses.add(message);
        additionalValues.put(VALUE_READ_MISSES, misses);
    }

}
