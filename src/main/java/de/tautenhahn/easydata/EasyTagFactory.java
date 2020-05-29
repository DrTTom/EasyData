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

  private final String tagStart;

  private final char tagEnd;

  /**
   * Creates a factory with specified tag syntax.
   *
   * @param opening marks the beginning of a tag
   * @param marker first char in the tag to be sure that this is special tag
   * @param closing marks the end of a tag
   */
  public EasyTagFactory(char opening, char marker, char closing)
  {
    specialTag = Pattern.compile(RegexHelper.mask(opening) + RegexHelper.mask(marker) + " *(.*) *"
                                 + RegexHelper.mask(closing));
    tagStart = String.valueOf(new char[]{opening, marker});
    tagEnd = closing;

    resolvers.put(InsertValueTag.PATTERN, (s, r) -> new InsertValueTag(s));
    resolvers.put(SkipTag.PATTERN, (s, r) -> new SkipTag(r));
    resolvers.put(ForTag.PATTERN, (s, r) -> new ForTag(s, r, this));
    resolvers.put(IfTag.PATTERN, (s, r) -> new IfTag(s, r, this));
    resolvers.put(DefineTag.PATTERN, (s, r) -> new DefineTag(s, r, this));
    resolvers.put(IndentTag.PATTERN, (s, r) -> new IndentTag(s, r, this));
    Resolver useTag = new UseTag(opening, marker, closing, this);
    resolvers.put(UseTag.PATTERN, (s, r) -> useTag);
    resolvers.put(SetTag.PATTERN, (s, r) -> new SetTag(s));
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

  @Override
  public void register(Pattern pattern, Resolver tag)
  {
    resolvers.put(pattern, (r, s) -> tag);
  }

  @Override
  public String nameToTag(String name)
  {
    return tagStart + name + tagEnd;
  }
}
