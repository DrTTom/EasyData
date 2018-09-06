package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Executes replacement of an [@=]-tag by respective value.
 *
 * @author TT
 */
public class EqualsTag implements Resolver
{

  private static final Pattern DEREF = Pattern.compile("\\$\\{([^}]+)}");

  private final String name;

  EqualsTag(Matcher start)
  {
    name = start.group(1).trim();
  }

  @Override
  public void resolve(Token start, Object data, Writer output) throws IOException
  {
    output.write((String)getAttribute(start, name, data));
  }

  /**
   * Into own class?
   *
   * @param attrName
   * @param data
   */
  public static Object getAttribute(Token source, String attrName, Object data)
  {
    try
    {
      return get(resolveInnerExpressions(attrName, data), data);
    }
    catch (RuntimeException e)
    {
      throw createDataRefException(e, source, attrName);
    }
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
  private static Object get(String attrName, Object data)
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

  private static String resolveInnerExpressions(String attrName, Object data)
  {
    String result = attrName;
    Matcher resolveFirst = DEREF.matcher(result);
    while (resolveFirst.find())
    {
      result = attrName.replace(resolveFirst.group(0), (String)get(resolveFirst.group(1), data));
      resolveFirst = DEREF.matcher(result);
    }
    return result;
  }
}
