package de.tautenhahn.easydata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Wraps complete data access. First idea is to wrap all data elements into separate access objects of one
 * class but that looked ugly because the needed types are different after all. <br>
 * <br>
 * Error reporting uses only the given path values, information about where those values came from may be
 * added by the caller.
 *
 * @author TT
 */
public class AccessibleData
{

  private static final Pattern DEREF = Pattern.compile("\\$\\{([^}]+)}");

  private static final Pattern SIZE = Pattern.compile("SIZE\\(([^\\)]+)\\)");

  private static final Pattern LITERAL = Pattern.compile("\"[^\"]*\"");

  private final Map<String, Object> data;

  /**
   * Defines how to iterate over a complex type.
   */
  public enum ListMode
  {
    /** use elements of lists or arrays, keys for maps or other complex objects */
    DEFAULT,
    /** use map keys, attribute names or numeric strings for arrays and lists */
    KEYS,
    /** use values always */
    VALUES
  }

  /**
   * Constructs an instance. TODO: replace this constructor with one which takes a JSON content.
   */
  public AccessibleData(Map<String, Object> data)
  {
    this.data = data;
  }

  /**
   * Returns the attribute of specified name, of whatever type. May return null if the attribute does not
   * exist but parent object does. Accepts constants if surrounded by quotes.
   *
   * @param attrName
   * @throws IllegalArgumentException in case an intermediate object is null or primitive.
   */
  public Object get(String attrName)
  {
    if (LITERAL.matcher(attrName).matches())
    {
      return attrName.substring(1, attrName.length() - 1);
    }
    if (attrName.matches("\\d+"))
    {
      return attrName;
    }
    Matcher m = SIZE.matcher(attrName);
    if (m.matches())
    {
      return Integer.toString(getCollection(m.group(1), ListMode.DEFAULT).size());
    }

    return get(resolveInnerExpressions(attrName), data);
  }

  /**
   * Same as {@link #get(String)} but returns String and throws Exception if target is complex.
   *
   * @param attrName
   */
  public String getString(String attrName)
  {
    Object result = get(attrName);
    if (isComplex(result))
    {
      throw new IllegalArgumentException("expected String but " + attrName + " is of type "
                                         + result.getClass().getName());
    }
    return Optional.ofNullable(result).map(Object::toString).orElse("null");
  }

  /**
   * Replaces each object by its addresses attribute.
   *
   * @param original
   * @param attrName
   */
  public Collection<Object> map(Collection<Object> original, String attrName)
  {
    return original.stream().map(o -> get(attrName, o)).collect(Collectors.toList());
  }

  /**
   * Returns a list of sorted elements. This method calls {@link #get(String, Object)} too frequently,
   * optimize in case of performance problems.
   *
   * @param original
   * @param attrName
   * @param ascending
   */
  public List<Object> sort(Collection<Object> original, String attrName, boolean ascending)
  {
    List<Object> result = new ArrayList<>(original);
    result.sort((a, b) -> compare(get(attrName, a), get(attrName, b), ascending));
    return result;
  }

  /**
   * Define additional value.
   *
   * @param name must be simple name
   * @param value must be of some supported type.
   */
  public void define(String name, Object value)
  {
    if (data.containsKey(name))
    {
      throw new IllegalArgumentException("cannot re-define existing key " + name);
    }
    data.put(name, value);
  }

  /**
   * Removes named attribute.
   *
   * @param name
   */
  public void undefine(String name)
  {
    data.remove(name);
  }

  /**
   * Returns true if o is some complex type, .i.e. something which may have attribute values. Implementation
   * depends on supported types, at the moment only {@link List} and {@link Map}.
   *
   * @param o
   */
  private boolean isComplex(Object o)
  {
    return o instanceof Map || o instanceof List;
  }

  private Object get(String attrName, Object element)
  {
    if (attrName == null)
    {
      return element;
    }
    if (element == null)
    {
      throw new IllegalArgumentException("no object to resolve " + attrName + " with");
    }
    int pos = attrName.indexOf('.');
    String first = attrName;
    String remaining = null;
    if (pos > 0)
    {
      first = attrName.substring(0, pos);
      remaining = attrName.substring(pos + 1);
    }
    Object attr = getAttribute(element, first);
    return remaining == null ? attr : get(remaining, attr);
  }

  /** By the way, this is the method to be changed if you want to support arbitrary Java beans. */
  private Object getAttribute(Object element, String first)
  {
    if (element instanceof Map)
    {
      return ((Map<?, ?>)element).get(first);
    }
    if (element instanceof List)
    {
      return ((List<?>)element).get(Integer.parseInt(first));
    }
    throw new IllegalArgumentException("No property '" + first + "' supported for element of type "
                                       + element.getClass().getName());
  }

  /**
   * Compares two values, considering the case that both are numeric.
   *
   * @param a
   * @param b
   * @param ascending
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public int compare(Object a, Object b, boolean ascending)
  {
    int direction = ascending ? 1 : -1;
    if (isNumeric(a) && isNumeric(b))
    {
      return Double.valueOf((String)a).compareTo(Double.valueOf((String)b)) * direction;
    }
    if (a instanceof Comparable)
    {
      return ((Comparable)a).compareTo(b) * direction;
    }
    throw new IllegalArgumentException("expected comparable but got " + a.getClass().getName());
  }

  private static boolean isNumeric(Object value)
  {
    return value instanceof String && ((String)value).matches("-?\\d+(\\.\\d+)?");
  }

  private String resolveInnerExpressions(String attrName)
  {
    String result = attrName;
    result = result.replaceAll("\\[([^\\]]+)\\]", ".\\${$1}");
    Matcher resolveFirst = DEREF.matcher(result);
    while (resolveFirst.find())
    {
      result = result.replace(resolveFirst.group(0), getString(resolveFirst.group(1)));
      resolveFirst = DEREF.matcher(result);
    }
    return result;
  }

  private List<Object> indexList(int size)
  {
    List<Object> result = new ArrayList<>(size);
    for ( int i = 0 ; i < size ; i++ )
    {
      result.add(Integer.toString(i));
    }
    return result;
  }

  /**
   * Returns a collection of sub-elements.
   *
   * @param attrName
   * @param mode
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Collection<Object> getCollection(String attrName, ListMode mode)
  {
    Object target = get(attrName);

    if (target instanceof Map)
    {
      return mode == ListMode.VALUES ? ((Map)target).values() : ((Map)target).keySet();
    }
    if (target instanceof List)
    {
      return mode == ListMode.KEYS ? indexList(((List<?>)target).size()) : (List)target;
    }

    throw new IllegalArgumentException("expected complex object but " + attrName + " is a "
                                       + target.getClass().getName());
  }
}
