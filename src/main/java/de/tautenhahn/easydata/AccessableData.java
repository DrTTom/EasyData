package de.tautenhahn.easydata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Wraps complete data access. First idea is to wrap all data elements into separate access objects of one
 * class but that looked ugly because the needed types are different after all. <br>
 * <br>
 * Error reporting uses only the given path values, information about where those values came from may be
 * added by the caller.
 *
 * @author TT
 */
public class AccessableData
{

  private static final Pattern DEREF = Pattern.compile("\\$\\{([^}]+)}");

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
    VALUES;
  }

  /**
   * Defines how to sort iterated elements.
   */
  public enum SortMode
  {
    /** use elements of lists or arrays, keys for maps or other complex objects */
    NONE,
    /** use map keys, attribute names or numeric strings for arrays and lists */
    ASCENDING,
    /** use values always */
    DECENDING;
  }

  /**
   * Constructs an instance. This constructor will be replaced as son as the object works.
   */
  public AccessableData(Map<String, Object> data)
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
    if (attrName.matches("\"[^\"]*\""))
    {
      return attrName.substring(1, attrName.length() - 1);
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
    return result.toString();
  }

  /**
   * Returns an iterator over sub-elements.
   *
   * @param attrName must target some complex attribute
   * @param mode chooses keys or values to iterate
   * @param sort may request sorting the elements
   * @param sortAttribute attribute to sort by (relative to iterated element), optional
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Iterator<Object> getIterator(String attrName, ListMode mode, SortMode sort, String sortAttribute)
  {
    Object target = get(attrName);
    Collection<Object> results = null;
    if (target instanceof Map)
    {
      results = mode == ListMode.VALUES ? ((Map)target).values() : ((Map)target).keySet();
    }
    else if (target instanceof List)
    {
      results = mode == ListMode.KEYS ? indexList(((List<?>)target).size()) : (List)target;
    }
    else
    {
      throw new IllegalArgumentException("expected complex object but " + attrName + " is a "
                                         + target.getClass().getName());
    }
    if (sort != SortMode.NONE)
    {
      results = new ArrayList<>(results);
      Collections.sort((List<?>)results, (a, b) -> compare(a, b, sort));
    }
    return results.iterator();
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
    Object attr = element instanceof Map ? ((Map<?, ?>)element).get(first)
      : ((List<?>)element).get(Integer.parseInt(first));
    return remaining == null ? attr : get(remaining, attr);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private int compare(Object a, Object b, SortMode sort)
  {
    int direction = sort == SortMode.DECENDING ? -1 : 1;
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
    Matcher resolveFirst = DEREF.matcher(result);
    while (resolveFirst.find())
    {
      result = attrName.replace(resolveFirst.group(0), getString(resolveFirst.group(1)));
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
}