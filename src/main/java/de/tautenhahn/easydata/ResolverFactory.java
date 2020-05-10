package de.tautenhahn.easydata;

import java.util.Iterator;
import java.util.regex.Pattern;


/**
 * Just to avoid a cyclic dependency.
 *
 * @author TT
 */
public interface ResolverFactory
{

  /**
   * Returns a resolver suitable for given token.
   *
   * @param startToken opening token to get resolver for
   * @param remaining contains next tokens which may relate to the current tag.
   * @return matching resolver
   */
  Resolver getResolver(Token startToken, Iterator<Token> remaining);

  /**
   * Registers a further resolver.
   * 
   * @param pattern to recognize the tag by
   * @param value the resolver object
   */
  void register(Pattern pattern, Resolver value);
}
