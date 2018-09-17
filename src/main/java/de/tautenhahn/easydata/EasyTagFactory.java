package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A factory for all the tag resolvers defined in this project.
 *
 * @author TT
 */
public final class EasyTagFactory implements ResolverFactory
{

  private static final Resolver IDENTITY = new Identity();

  private final Map<Pattern, BiFunction<Matcher, Iterator<Token>, Resolver>> resolvers = new HashMap<>();

  /**
   * Creates a factory with specified tag syntax.
   *
   * @param opening
   * @param marker
   * @param closing
   */
  public EasyTagFactory(char opening, char marker, char closing)
  {
    String open = RegexHelper.mask(opening) + RegexHelper.mask(marker);
    String close = RegexHelper.mask(closing);
    String op = String.valueOf(new char[]{opening, marker});
    resolvers.put(Pattern.compile(open + "=(.*)" + close), (s, r) -> new InsertValueTag(s));
    resolvers.put(Pattern.compile(open + "FOR +(\\w+):([^" + close + "]+)" + close),
                  (s, r) -> new ForTag(s, r, op + "DELIM" + closing, op + "END" + closing, this));
    resolvers.put(Pattern.compile(open + "IF +(.+)(==)([^" + close + "]+)" + close),
                  (s, r) -> new IfTag(s, r, op + "ELSE" + closing, op + "END" + closing, this));
  }

  /**
   * Just leaves text as it is, to simplify token handling.
   */
  static class Identity implements Resolver
  {

    @Override
    public void resolve(Token start, AccessableData data, Writer output) throws IOException
    {
      output.write(start.getContent());
    }
  }

  @Override
  public Resolver getResolver(Token token, Iterator<Token> remaining)
  {
    for ( Entry<Pattern, BiFunction<Matcher, Iterator<Token>, Resolver>> entry : resolvers.entrySet() )
    {
      Matcher m = entry.getKey().matcher(token.getContent());
      if (m.matches())
      {
        return entry.getValue().apply(m, remaining);
      }
    }
    return IDENTITY;
  }
}
