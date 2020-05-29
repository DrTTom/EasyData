package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Just resolves to the content but inserts a defined indent at the beginning of each new line.
 *
 * @author TT
 */
public class IndentTag extends ComplexTag
{

  static final Pattern PATTERN = Pattern.compile("INDENT");

  IndentTag(Matcher startMatcher, Iterator<Token> remaining, ResolverFactory factory)
  {
    super(startMatcher, remaining, factory, "VALUE", "/INDENT");
  }

  @Override
  public void resolve(Token start, AccessibleData data, Writer output) throws IOException
  {
    if (otherContent.isEmpty())
    {
      resolveContent(data, output);
    }
    else
    {
      try (
        Writer formatter = new FormattingWriter(output, otherContent.keySet().iterator().next().getContent()))
      {
        resolveContent(data, formatter);
      }
    }
  }

  private void resolveContent(AccessibleData data, Writer output) throws IOException
  {
    for ( Map.Entry<Token, Resolver> entry : content.entrySet() )
    {
      entry.getValue().resolve(entry.getKey(), data, output);
    }
  }
}
