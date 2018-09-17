package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;


/**
 * Executes replacement of an [@=]-tag by respective value.
 *
 * @author TT
 */
public class InsertValueTag implements Resolver
{

  private final String name;

  InsertValueTag(Matcher start)
  {
    name = start.group(1).trim();
  }

  @Override
  public void resolve(Token start, AccessableData data, Writer output) throws IOException
  {
    output.write(data.getString(name));
  }

}
