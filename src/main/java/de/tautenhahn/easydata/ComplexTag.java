package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


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
  protected Map<Token, Resolver> content = new LinkedHashMap<>();

  /**
   * Alternative content.
   */
  protected Map<Token, Resolver> otherContent = new LinkedHashMap<>();

  /**
   * Creates new instance
   *
   * @param remaining
   * @param delim content of tag starting the alternative content.
   * @param end
   * @param factory
   */
  protected ComplexTag(Iterator<Token> remaining, String delim, String end, ResolverFactory factory)
  {
    Map<Token, Resolver> tokens = content;

    while (remaining.hasNext())
    {
      Token token = remaining.next();
      if (delim.equals(token.getContent()))
      {
        tokens = otherContent;
        continue;
      }
      if (end.equals(token.getContent()))
      {
        return;
      }
      tokens.put(token, factory.getResolver(token, remaining));
    }
  }

  /**
   * Resolves the content.
   *
   * @param subTags
   * @param data original data enhanced by attributes defined in start tag
   * @param output
   * @throws IOException
   */
  protected void resolveContent(Map<Token, Resolver> subTags, Object data, Writer output) throws IOException
  {
    for ( Entry<Token, Resolver> entry : subTags.entrySet() )
    {
      entry.getValue().resolve(entry.getKey(), data, output);
    }
  }
}
