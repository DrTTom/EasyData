package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;


/**
 * A factory for all the tag resolvers defined in this project.
 *
 * @author TT
 */
public final class EasyTagFactory implements ResolverFactory
{

  private static final Resolver IDENTITY = new Identity();

  /**
   * Just leaves text as it is, to simplify token handling.
   */
  static class Identity implements Resolver
  {

    @Override
    public void resolve(Token start, Object data, Writer output) throws IOException
    {
      output.write(start.getContent());
    }
  }

  @Override
  public Resolver getResolver(Token token, Iterator<Token> remaining)
  {
    if (token.getContent().matches("\\[@=.*\\]"))
    {
      return new EqualsTag(token);
    }
    if (token.getContent().matches("\\[@FOR.*\\]"))
    {
      return new ForTag(token, remaining, this);
    }
    return IDENTITY;
  }
}