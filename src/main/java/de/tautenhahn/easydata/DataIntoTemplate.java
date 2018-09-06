package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Scanner;


/**
 * Entry class into template expansion.
 *
 * @author TT
 */
public class DataIntoTemplate
{

  private final Object data;

  private final ResolverFactory factory;

  /**
   * Creates an instance filled with some data.
   *
   * @param data Map containing Maps, Arrays and Strings (as parsed without type restrictions by GSON).
   * @param marker character to recognize the special tags by.
   */
  public DataIntoTemplate(Map<String, Object> data, char opening, char marker, char closing)
  {
    this.data = data;
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
      for ( Tokenizer tokens = new Tokenizer(s) ; tokens.hasNext() ; )
      {
        Token start = tokens.next();
        factory.getResolver(start, tokens).resolve(start, data, output);
      }
    }

  }

}
