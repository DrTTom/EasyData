package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

/**
 * TRYING TO ENCAPSULATE ALL THE UNDEFINED TYPE STUFF IN ONE CLASS. UNDECIDED.
 * Wraps a data structure. For sake of simplicity, the whole structure consists of string keyed maps
 * containing such maps, lists and string values.<br>
 * There are many instances of this class created but they are lightweight and so we have all the unclean 
 * generic-or-not stuff hidden in one class. 
 * 
 * @author TT
 */
public class AccessableElement
{
  // Could use configurable symbols but is used only inside tags which are already safely recognized.
  private static final Pattern DEREF = Pattern.compile("\\$\\{([^}]+)}");
  
  private final Map<String, Object> content;
  
  /**
   * Creates instance parsing a JSON file.
   * @param json
   * @param encoding
   * @throws IOException 
   */
  @SuppressWarnings("unchecked")
  public AccessableElement(InputStream json, Charset encoding) throws IOException
  {
    try (Reader reader = new InputStreamReader(json, encoding))
    {
      content = new Gson().fromJson(reader, Map.class);
    }
  }
  
  /**
   * Copy constructor. Because any kind of write access to the content is restricted to first level, a shallow copy is sufficient. 
   * @param original
   */
  protected AccessableElement(AccessableElement original)
  {
    content=new HashMap<>(original.content);
  }
  
  /**
   * Returns a (nested) attribute value.
   *
   * @param attrName name of an attribute, optionally followed by dot and name or index if sub-attribute, may contain 
   * references to other expressions or may be just a constant which is then returned.
  * @param source used to create error messages
   */
  public String getPrimitiveElement(String name, Token source)
  {
    try
    {
      return (String) get(resolveInnerExpressions(name), content);
    }
    catch (RuntimeException e)
    {
      throw createDataRefException(e, source, name);
    }
  }

  /**
   * Returns a sub-object to iterate on.
   * @param name
   * @param source
   * @return
   */
  public Object getIterableElement(String name, Token source)
  {
    Object result = null;
    try
    {
      result= get(resolveInnerExpressions(name), content);
    }
    catch (RuntimeException e)
    {
      throw createDataRefException(e, source, name);
    }
   if (!(result instanceof Map) && !(result instanceof List))
   {
     throw createDataRefException(null, source, name);
   }
   return result;
  }
  
  /**
   * Defines another attribute.
   * @param name
   * @param value must be map, string or list TODO: make the compiler check that, consider nested elements 
   */
  public void defineAttribute(String name, Object value)
  {
    content.put(name, value);
  }
  /**
   * Creates an exception stating that there is a wrong reference in the input.
   *
   * @param e
   * @param source
   * @param attrName
   */
  public static IllegalArgumentException createDataRefException(Throwable e, Token source, String attrName)
  {
    return new IllegalArgumentException("Invalid data reference \"" + attrName + "\" at line "
                                        + source.getRow() + ", col. " + source.getCol(), e);
  }

  /**
   * Returns (nested) object attribute.
   *
   * @param attrName
   * @param data
   */
  @SuppressWarnings("unchecked")
  private Object get(String attrName, Object data)
  {
    int pos = attrName.indexOf('.');
    String first = attrName;
    String remaining = null;
    if (pos > 0)
    {
      first = attrName.substring(0, pos);
      remaining = attrName.substring(pos + 1);
    }
    Object attr = data instanceof Map ? ((Map<String, ?>)data).get(first)
      : ((List<?>)data).get(Integer.parseInt(first));
    return remaining == null ? attr : get(remaining, attr);
  }

  private String resolveInnerExpressions(String expression)
  {
    String result = expression;
    Matcher resolveFirst = DEREF.matcher(result);
    while (resolveFirst.find())
    {
      result = result.replace(resolveFirst.group(0), (String) get(resolveFirst.group(1), content));
      resolveFirst = DEREF.matcher(result);
    }
    return result;
  }
}
