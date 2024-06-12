package de.tautenhahn.easydata;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Map where the values are kept in stacks. Several values can be added for the same key shadowing each other,
 * upon removal the previous value appears again. This supports recursively called macros.
 *
 * @param <K> key type
 * @param <V> value type
 * @author tt
 */
public class StackMap<K, V> implements Map<K, V> {

    private final Map<K, Deque<V>> content = new HashMap<>();


    @Override
    public V get(Object key) {
        return Optional.ofNullable(content.get(key)).map(Deque::peek).orElse(null);
    }

    @Override
    public V put(K key, V value) {
        Deque<V> stack = content.computeIfAbsent(key, k -> new LinkedList<>());
        V previous = stack.isEmpty() ? null : stack.peek();
        stack.push(value);
        return previous;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public V remove(Object key) {
        Deque<V> stack = content.get(key);
        if (stack == null) {
            return null;
        }
        V result = stack.pop();
        if (stack.isEmpty()) {
            content.remove(key);
        }
        return result;
    }

    @Override
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    @Override
    public Collection<V> values() {
        return content.values().stream().map(Deque::peek).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return content.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().peek()))
                .collect(Collectors.toSet());
    }

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return content.containsKey(key);
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public Set<K> keySet() {
        return content.keySet();
    }
}
