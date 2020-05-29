package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.regex.Pattern;


/**
 * Resolves to nothing but hides the following token.
 *
 * @author TT
 */
public class SkipTag implements Resolver
{

  /**
   * Pattern to define this tag type, matches complete content.
   */
  public static final Pattern PATTERN = Pattern.compile("SKIP");

  SkipTag(Iterator<Token> remaining)
  {
    remaining.next();
  }

  @Override
  public void resolve(Token start, AccessibleData data, Writer output) throws IOException
  {
    // nothing on purpose
  }
}
