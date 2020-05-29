package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;


/**
 * Base class for tags containing some content. Two kinds of content (3 Tags) supported here.
 *
 * @author TT
 */
public abstract class ComplexTag implements Resolver
{

  /**
   * Main content.
   */
  protected final Map<Token, Resolver> content = new LinkedHashMap<>();

  /**
   * Alternative content.
   */
  protected final Map<Token, Resolver> otherContent = new LinkedHashMap<>();

  /**
   * Creates new instance
   *
   * @param startTag only used for error message if end is missing
   * @param remaining further text to read until end tag is found
   * @param factory provides the resolvers for nested tags
   * @param delimName content excluding brace and markers of tag starting the alternative content.
   * @param endName names of end tags. "END" is always supported.
   */
  protected ComplexTag(Matcher startTag,
                       Iterator<Token> remaining,
                       ResolverFactory factory,
                       String delimName,
                       String... endName)
  {
    String delim = factory.nameToTag(delimName);
    List<String> end = new ArrayList<>();
    end.add(factory.nameToTag("END"));
    Arrays.stream(endName).map(factory::nameToTag).forEach(end::add);

    Map<Token, Resolver> tokens = content;

    while (remaining.hasNext())
    {
      Token token = remaining.next();
      if (delim.equals(token.getContent()))
      {
        tokens = otherContent;
        continue;
      }
      if (end.contains(token.getContent()))
      {
        return;
      }
      tokens.put(token, factory.getResolver(token, remaining));
    }
    throw new IllegalArgumentException("unexpected end of input, pending " + startTag.group(0) + ", missing "
                                       + end);
  }

  /**
   * Resolves the content.
   *
   * @param subTags content with respective resolvers
   * @param data original data enhanced by attributes defined in start tag
   * @param output where the result is written to
   * @throws IOException in case of streaming problems
   */
  protected void resolveContent(Map<Token, Resolver> subTags, AccessibleData data, Writer output)
    throws IOException
  {
    for ( Entry<Token, Resolver> entry : subTags.entrySet() )
    {
      entry.getValue().resolve(entry.getKey(), data, output);
    }
  }
}
