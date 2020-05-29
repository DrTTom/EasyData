package de.tautenhahn.easydata;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Map where the values are kept in stacks. Several values can be added for the same key shadowing each other,
 * upon removal the previous value appears again. This supports recursively called macros. * @author tt
 */
public class StackMap extends HashMap<String, Object>
{

  private static final long serialVersionUID = 1L;

  @Override
  public Object get(Object key)
  {
    return Optional.ofNullable(super.get(key)).map(e -> ((Deque)e).peek()).orElse(null);
  }

  @Override
  public Object put(String key, Object value)
  {
    ((Deque<Object>)super.computeIfAbsent(key, k -> new ArrayDeque<>())).push(value);
    return null;
  }

  @Override
  public void putAll(Map<? extends String, ?> m)
  {
    m.forEach(this::put);
  }

  @Override
  public Object remove(Object key)
  {
    Deque<Object> stack = (Deque<Object>)super.get(key);
    if (stack == null)
    {
      return null;
    }
    Object result = stack.pop();
    if (stack.isEmpty())
    {
      super.remove(key);
    }
    return result;
  }

  @Override
  public boolean containsValue(Object value)
  {
    return values().contains(value);
  }

  @Override
  public Collection<Object> values()
  {
    return super.values().stream().map(e -> ((Deque)e).peek()).collect(Collectors.toList());
  }

  @Override
  public Set<Entry<String, Object>> entrySet()
  {
    throw new UnsupportedOperationException();
  }
}
