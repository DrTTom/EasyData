package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Objects;
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
  public void resolve(Token startTag, AccessableData data, Writer output) throws IOException
  {
    String leftSide = start.group(1); // TODO: support some recursion here too!
    // String operator = start.group(2);
    String rightSide = start.group(3);

    if (Objects.equals(data.get(leftSide), data.get(rightSide)))
    {
      resolveContent(content, data, output);
    }
    else
    {
      resolveContent(otherContent, data, output);
    }
  }
}
