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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public final class AccessibleData
{

  private static final Pattern DEREF = Pattern.compile("\\$\\{([^}]+)}");

  private static final Pattern SIZE = Pattern.compile("SIZE\\(([^\\)]+)\\)");

  private static final Pattern LITERAL = Pattern.compile("\"[^\"]*\"");

  private final Object data;

  private final Map<String, Object> additionals = new HashMap<>();

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
   * Returns new instance wrapping a Map or Java Bean.
   * 
   * @param data given data
   * @return new instance
   */
  public static AccessibleData byBean(Object data)
  {
    return new AccessibleData(data);
  }

  /**
   * Returns new instance wrapping data specified by JSON string.
   *
   * @param data given data
   * @return new instance
   */
  public static AccessibleData byJsonContent(String data)
  {
    return new AccessibleData(new Gson().fromJson(data, Map.class));
  }

  /**
   * Returns new instance wrapping data specified by JSON file.
   *
   * @param data given data
   * @return new instance
   * @throws IOException
   */
  public static AccessibleData byJsonPath(String data) throws IOException
  {
    try (InputStream json = new FileInputStream(data);
      Reader reader = new InputStreamReader(json, StandardCharsets.UTF_8))
    {
      return byJsonReader(reader);
    }
  }

  /**
   * Returns new instance wrapping data specified by JSON reader.
   *
   * @param data given data
   * @return new instance
   * @throws IOException
   */
  public static AccessibleData byJsonReader(Reader data) throws IOException
  {
    return new AccessibleData(new Gson().fromJson(data, Map.class));
  }


  private AccessibleData(Object data)
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
    String attr = resolveInnerExpressions(attrName);
    return get(attr, additionals.containsKey(getFirstPart(attr)) ? additionals : data);
  }

  private String getFirstPart(String path)
  {
    int pos = path.indexOf('.');
    return pos > 0 ? path.substring(0, pos) : path;
  }

  /**
   * Same as {@link #get(String)} but returns String and throws Exception if target is complex.
   *
   * @param attrName
   */
  public String getString(String attrName)
  {
    Object result = get(attrName);
    if (result != null && (result instanceof Map || result instanceof List || result.getClass().isArray()))
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
    if (get(name) != null)
    {
      throw new IllegalArgumentException("cannot re-define existing key " + name);
    }
    additionals.put(name, value);
  }

  /**
   * Removes named attribute.
   *
   * @param name
   */
  public void undefine(String name)
  {
    additionals.remove(name);
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
    String first = getFirstPart(attrName);
    Object attr = getAttribute(element, first);
    return first.equals(attrName) ? attr : get(attrName.substring(first.length() + 1), attr);
  }

  private Object getAttribute(Object element, String first)
  {
    if (element instanceof Map)
    {
      return ((Map<?, ?>)element).get(first);
    }
    if (element instanceof List)
    {
      return ((List<?>)element).get(getIndex(first, ((List<?>)element).size()));
    }
    if (element.getClass().isArray())
    {
      return Array.get(element, getIndex(first, Array.getLength(element)));
    }
    try
    {
      return new PropertyDescriptor(first, element.getClass()).getReadMethod().invoke(element);
    }
    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
      | IntrospectionException e)
    {
      throw new IllegalArgumentException("No property '" + first + "' supported for element of type "
                                         + element.getClass().getName(), e);
    }
  }

  private int getIndex(String index, int length)
  {
    try
    {
      int result = Integer.parseInt(index);
      if (result >= 0 || result < length)
      {
        return result;
      }
    }
    catch (NumberFormatException e) // NOPMD same handling needed as in case without Exception
    {
      // error handling below
    }
    throw new IllegalArgumentException("Invalid index '" + index + "', adressed object has elements 0-"
                                       + length);
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
    if (isNumericString(a) && isNumericString(b))
    {
      return Double.valueOf((String)a).compareTo(Double.valueOf((String)b)) * direction;
    }
    if (a instanceof Comparable)
    {
      return ((Comparable)a).compareTo(b) * direction;
    }
    throw new IllegalArgumentException("expected comparable but got " + a.getClass().getName());
  }

  private static boolean isNumericString(Object value)
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
    checkComplex(attrName, target);
    if (target instanceof Map)
    {
      return mode == ListMode.VALUES ? ((Map)target).values() : ((Map)target).keySet();
    }
    if (target instanceof List)
    {
      return mode == ListMode.KEYS ? indexList(((List<?>)target).size()) : (List)target;
    }
    if (target.getClass().isArray())
    {
      return mode == ListMode.KEYS ? indexList(Array.getLength(target)) : toList(target);
    }
    return beanToList(attrName, mode, target);
  }

  private Collection<Object> beanToList(String attrName, ListMode mode, Object target)
  {
    try
    {
      BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
      PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
      List<Object> result = new ArrayList<>();
      for ( PropertyDescriptor ds : pds )
      {
        Method readMethod = ds.getReadMethod();
        if (readMethod != null && !"class".equals(ds.getName()))
        {
          result.add(mode == ListMode.KEYS ? ds.getName() : readMethod.invoke(target));
        }
      }
      return result;
    }
    catch (IntrospectionException | ReflectiveOperationException | IllegalArgumentException e)
    {
      throw new IllegalArgumentException("expected complex object but " + attrName + " is a "
                                         + target.getClass().getName(), e);
    }
  }

  private void checkComplex(String attrName, Object target)
  {
    if (target == null || target instanceof String || target.getClass().isPrimitive())
    {
      throw new IllegalArgumentException("expected complex object but " + attrName + " is a "
                                         + Optional.ofNullable(target)
                                                   .map(Object::getClass)
                                                   .map(Class::getName)
                                                   .orElse("null value"));
    }

  }

  private List<Object> toList(Object target)
  {
    int length = Array.getLength(target);
    List<Object> result = new ArrayList<>(length);
    for ( int i = 0 ; i < length ; i++ )
    {
      result.add(Array.get(target, i));
    }
    return result;
  }
}
