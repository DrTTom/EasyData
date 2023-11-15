package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Same as {@link MacroTag} but the tag name can be passed in as expression which is resolved.
 * 
 * @author TT
 */
public class UseTag implements Resolver
{

  private final ResolverFactory factory;

  static final Pattern PATTERN = Pattern.compile("USE +([^ ]+)(( +[^ ]+)*)");

  private final String op;

  private final String close;

  UseTag(char opening, char marker, char closing, ResolverFactory factory)
  {
    this.factory = factory;
    this.op = String.valueOf(new char[]{opening, marker});
    this.close = String.valueOf(closing);
  }

  @Override
  public void resolve(Token start, AccessibleData data, Writer output) throws IOException
  {

    String str = start.getContent();
    Matcher m = PATTERN.matcher(str.substring(2, str.length() - 1).trim());
    if (!m.matches()) {
      throw new IllegalArgumentException("Unsupported token '"+str+"'");
    }
    String translated = op + data.getString(m.group(1)) + m.group(2) + close;
    Token eventualStartToken = new Token(translated, start.getRow(), start.getCol());
    factory.getResolver(eventualStartToken, null).resolve(eventualStartToken, data, output);
  }

}
