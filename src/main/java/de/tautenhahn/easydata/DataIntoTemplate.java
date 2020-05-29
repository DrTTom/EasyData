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

  private ResolverFactory factory;

  private final char opening;

  private final char closing;

  private final char marker;


  /**
   * Creates an instance filled with some data.
   *
   * @param data data to insert
   * @param opening first character of the tag
   * @param marker character to recognize the special tags by.
   * @param closing last character of the tag
   */
  public DataIntoTemplate(AccessibleData data, char opening, char marker, char closing)
  {
    this.data = data;
    this.opening = opening;
    this.closing = closing;
    this.marker = marker;
    resetFactory();
  }

  /**
   * Re-create resolver factory so that only default tags are supported. More precisely, all tags defined
   * by @DEFINE are forgotten.
   */
  public final void resetFactory()
  {
    factory = new EasyTagFactory(opening, marker, closing);
  }

  /**
   * Reads input and expands all the special tags using data.
   *
   * @param template input test containing special tags
   * @param output where the output is written to.
   * @throws IOException in case of streaming problems
   */
  public void fillData(Reader template, Writer output) throws IOException
  {
    try (Scanner scanner = new Scanner(template); Scanner sRes = scanner.useDelimiter("\n"))
    {
      for ( Tokenizer tokens = new Tokenizer(sRes, opening, marker, closing) ; tokens.hasNext() ; )
      {
        Token start = tokens.next();
        factory.getResolver(start, tokens).resolve(start, data, output);
      }
    }

  }

}
