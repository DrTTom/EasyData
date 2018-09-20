package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;


/**
 * Entry class into template expansion.
 *
 * @author TT
 */
public class DataIntoTemplate
{

  private final AccessibleData data;

  private final ResolverFactory factory;

  private final char opening;

  private final char closing;

  private final char marker;


  /**
   * Creates an instance filled with some data.
   *
   * @param data data to insert
   * @param marker character to recognize the special tags by.
   */
  public DataIntoTemplate(AccessibleData data, char opening, char marker, char closing)
  {
    this.data = data;
    this.opening = opening;
    this.closing = closing;
    this.marker = marker;
    factory = new EasyTagFactory(opening, marker, closing);
  }

  /**
   * Reads input and expands all the special tags using data.
   *
   * @param template
   * @param output where the output is written to.
   * @throws IOException
   */
  public void fillData(Reader template, Writer output) throws IOException
  {
    try (Scanner scanner = new Scanner(template); Scanner s = scanner.useDelimiter("\n"))
    {
      for ( Tokenizer tokens = new Tokenizer(s, opening, marker, closing) ; tokens.hasNext() ; )
      {
        Token start = tokens.next();
        factory.getResolver(start, tokens).resolve(start, data, output);
      }
    }

  }

}
