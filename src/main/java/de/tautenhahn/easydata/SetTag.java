package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Defines a pre-computed value for further use.
 *
 * @author TT
 */
public class SetTag implements Resolver
{

  /**
   * Pattern to define this tag type, matches complete content.
   */
  public static final Pattern PATTERN = Pattern.compile("SET +(\\w+) *= *("
                                                        + InsertValueTag.REGEX_SIMPLE_EXPRESSION + ") *");

  private final Matcher start;

  SetTag(Matcher start)
  {
    this.start = start;
  }

  @Override
  public void resolve(Token startTag, AccessibleData data, Writer output) throws IOException
  {
    data.define(start.group(1), data.get(start.group(2)));
  }

}
