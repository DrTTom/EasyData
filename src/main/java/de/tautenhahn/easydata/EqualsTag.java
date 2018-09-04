package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;


/**
 * Executes replacement of an [@=]-tag by respective value.
 * 
 * @author TT
 */
public class EqualsTag implements Resolver
{

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
    output.write(get(name, data));
  }

  /**
   * Returns (nested) object attribute.
   * 
   * @param name2
   * @param data
   */
  @SuppressWarnings("unchecked")
  public String get(String name2, Object data)
  {
    int pos = name2.indexOf('.');
    if (pos == -1)
    {
      return ((Map<String, String>)data).get(name2);
    }
    return get(name2.substring(pos + 1), ((Map<String, Object>)data).get(name2.substring(0, pos)));
  }
}
