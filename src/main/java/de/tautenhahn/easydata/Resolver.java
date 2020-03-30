package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;


/**
 * Just the ability to resolve some part of the template using the given data.
 *
 * @author TT
 */
public interface Resolver
{

  /**
   * Writes result of transformation into given Writer.
   *
   * @param start Tag just passed in to allow singleton instances when there is not much to do.
   * @param data all data available within the current context, namely original data plus some attributes
   *          defined in surrounding tags.
   * @param output to write resolved content to
   * @throws IOException in case of IO problems
   */
  void resolve(Token start, AccessibleData data, Writer output) throws IOException;

}
