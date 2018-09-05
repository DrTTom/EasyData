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

  /**
   * Creates instance
   *
   * @param start
   */
  public EqualsTag(Token start)
  {
    name = start.getContent().substring(3, start.getContent().length() - 1).trim();
  }

  @Override
  public void resolve(Token start, Object data, Writer output) throws IOException
  {
    output.write((String)getAttribute(name, data));
  }

  /**
   * Into own class?
   *
   * @param attrName
   * @param data
   */
  public static Object getAttribute(String attrName, Object data)
  {
    return get(resolveInnerExpressions(attrName, data), data);
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
