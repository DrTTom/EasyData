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

  private final Pattern specialTag;

  /**
   * Creates a factory with specified tag syntax.
   *
   * @param opening
   * @param marker
   * @param closing
   */
  public EasyTagFactory(char opening, char marker, char closing)
  {

    String open = RegexHelper.mask(opening);
    String close = RegexHelper.mask(closing);
    String mmarker = RegexHelper.mask(marker);
    // easy because input is already a token:
    specialTag = Pattern.compile(open + mmarker + " *(.*) *" + close);


    String op = String.valueOf(new char[]{opening, marker});
    resolvers.put(InsertValueTag.PATTERN, (s, r) -> new InsertValueTag(s));
    resolvers.put(ForTag.PATTERN,
                  (s, r) -> new ForTag(s, r, op + "DELIM" + closing, op + "END" + closing, this));
    resolvers.put(IfTag.PATTERN,
                  (s, r) -> new IfTag(s, r, op + "ELSE" + closing, op + "END" + closing, this));

  }

  /**
   * Just leaves text as it is, to simplify token handling.
   */
  static class Identity implements Resolver
  {

    @Override
    public void resolve(Token start, AccessibleData data, Writer output) throws IOException
    {
      output.write(start.getContent());
    }
  }

  @Override
  public Resolver getResolver(Token token, Iterator<Token> remaining)
  {
    Matcher tagMatcher = specialTag.matcher(token.getContent());
    if (tagMatcher.matches())
    {
      for ( Entry<Pattern, BiFunction<Matcher, Iterator<Token>, Resolver>> entry : resolvers.entrySet() )
      {
        Matcher contentMatcher = entry.getKey().matcher(tagMatcher.group(1));
        if (contentMatcher.matches())
        {
          return entry.getValue().apply(contentMatcher, remaining);
        }
      }
      throw new IllegalArgumentException("unrecognized token " + token);
    }
    return IDENTITY;
  }
}
