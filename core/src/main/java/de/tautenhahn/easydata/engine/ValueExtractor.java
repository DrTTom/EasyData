package de.tautenhahn.easydata.engine;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * Can extract a value specified by path form an object.
 */
public class ValueExtractor {
    private final Consumer<String> readMissObserver;

    /**
     * Creates immutable instance.
     * @param readMissObserver where to record the message about not resolvable path
     */
    ValueExtractor(Consumer<String> readMissObserver) {
        this.readMissObserver = readMissObserver;
    }

    Object get(String[] path, int alreadyResolved, Object element) {
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
        if (element instanceof Map<?,?> map) {
            return map.get(attrName);
        }
        if (element instanceof List<?> list) {
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

    private void recordValueReadMiss(String[] path, int alreadyResolved, String msg) {
        String resolvedPath = String.join(".", Arrays.copyOfRange(path, 0, alreadyResolved));
        String remainingPath = String.join(".", Arrays.copyOfRange(path, alreadyResolved, path.length));
        String message = "cannot resolve '" + remainingPath + "' because value of '" + resolvedPath + "' is " + msg;
        readMissObserver.accept(message);
    }

}
