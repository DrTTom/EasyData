package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
  protected List<Token> content = new ArrayList<>();

  /**
   * Alternative content.
   */
  protected List<Token> otherContent = new ArrayList<>();


  private final ResolverFactory factory;

  /**
   * Creates new instance
   *
   * @param remaining
   * @param delim content of tag starting the alternative content.
   * @param factory
   */
  protected ComplexTag(Iterator<Token> remaining, String delim, ResolverFactory factory)
  {
    this.factory = factory;
    List<Token> tokens = content;

    while (remaining.hasNext())
    {
      Token token = remaining.next();
      if (delim.equals(token.getContent()))
      {
        tokens = otherContent;
        continue;
      }
      if ("[@END]".equals(token.getContent()))
      {
        return;
      }
      tokens.add(token);
    }
  }

  /**
   * Resolves the content.
   *
   * @param subTemplate
   * @param data original data enhanced by attributes defined in start tag
   * @param output
   * @throws IOException
   */
  protected void resolveContent(Iterator<Token> subTemplate, Object data, Writer output) throws IOException
  {
    while (subTemplate.hasNext())
    {
      Token start = subTemplate.next();
      factory.getResolver(start, subTemplate).resolve(start, data, output);
    }
  }
}
