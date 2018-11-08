package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Executes replacement of an [@=]-tag by respective value.
 *
 * @author TT
 */
public class InsertValueTag implements Resolver
{

  /**
   * Value expression. The regex matches invalid expressions too but is needed here only to recognize the tag
   * type.
   */
  public static final String REGEX_SIMPLE_EXPRESSION = "\\w[\\w\\${}\\.\\)\\(\\[\\]]*[\\w})]+";

  /**
   * Pattern to define this tag type, matches complete content.
   */
  public static final Pattern PATTERN = Pattern.compile("= *(" + REGEX_SIMPLE_EXPRESSION + ") *");

  // Pattern.compile("= *" + REGEX_EXPRESSION + " *");
  private final String name;

  InsertValueTag(Matcher start)
  {
    name = start.group(1);
  }

  @Override
  public void resolve(Token start, AccessibleData data, Writer output) throws IOException
  {
    output.write(data.getString(name));
  }

}
