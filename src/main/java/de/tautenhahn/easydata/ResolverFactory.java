package de.tautenhahn.easydata;

import java.util.Iterator;


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
   * @param startToken
   * @param data data object to fill in
   */
  Resolver getResolver(Token startToken, Iterator<Token> remaining);
}
