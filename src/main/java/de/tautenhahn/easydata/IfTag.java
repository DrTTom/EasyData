package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Supports conditional output with very simple conditions.
 *
 * @author TT
 */
public class IfTag extends ComplexTag
{

  private final Matcher start;

  /**
   * How to recognize this tag.
   */
  public static final Pattern PATTERN = Pattern.compile("IF +(.+) *(==|!=|>|<) *(.+)");

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
  public void resolve(Token startTag, AccessibleData data, Writer output) throws IOException
  {
    String leftSide = start.group(1).trim();
    String operator = start.group(2);
    String rightSide = start.group(3).trim();
    Object left = data.get(leftSide);
    Object right = data.get(rightSide);

    if ("==".equals(operator) && Objects.equals(left, right)
        || "!=".equals(operator) && !Objects.equals(left, right)
        || "<".equals(operator) && data.compare(left.toString(), right.toString(), true) < 0
        || ">".equals(operator) && data.compare(left.toString(), right.toString(), true) > 0)
    {
      resolveContent(content, data, output);
    }
    else
    {
      resolveContent(otherContent, data, output);
    }
  }
}
