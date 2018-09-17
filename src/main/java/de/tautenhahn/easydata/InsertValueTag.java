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
   * Pattern to define this tag type, matches complete content.
   */
  public static final Pattern PATTERN = Pattern.compile("= *([\\w\\${}\\.]+) *");

  private final String name;

  InsertValueTag(Matcher start)
  {
    name = start.group(1);
  }

  @Override
  public void resolve(Token start, AccessableData data, Writer output) throws IOException
  {
    output.write(data.getString(name));
  }

}
