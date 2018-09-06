package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.regex.Matcher;


/**
 * Supports conditional output with very simple conditions.
 *
 * @author TT
 */
public class IfTag extends ComplexTag
{

  private final Matcher start;

  /**
   * Creates new instance.
   *
   * @param start
   * @param remaining
   * @param factory
   */
  public IfTag(Matcher start, Iterator<Token> remaining, String delim, String end, ResolverFactory factory)
  {
    super(remaining, delim, end, factory);
    this.start = start;
  }

  @Override
  public void resolve(Token startTag, Object data, Writer output) throws IOException
  {
    String leftSide = start.group(1); // TODO: support some recursion here too!
    // String operator = start.group(2);
    String rightSide = start.group(3);

    if (get(startTag, data, leftSide).equals(get(startTag, data, rightSide)))
    {
      resolveContent(content, data, output);
    }
    else
    {
      resolveContent(otherContent, data, output);
    }
  }

  private Object get(Token startTag, Object data, String name)
  {
    if (name.matches("\"[^\"]*\""))
    {
      return name.substring(1, name.length() - 1);
    }
    return InsertValueTag.getAttribute(startTag, name, data);
  }


}
