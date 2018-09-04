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

  /**
   * Creates an instance filled with some data.
   * 
   * @param data Map containing Maps, Arrays and Strings (as parsed without type restrictions by GSON).
   * @param marker character to recognize the special tags by.
   */
  public DataIntoTemplate(Map<String, Object> data, char marker)
  {
    this.data = data;
  }

  /**
   * Reads input and expands all the special tags using data.
   * 
   * @param template
   * @param out where the output is written to.
   * @throws IOException
   */
  public void fillData(Reader template, Writer out) throws IOException
  {
    ResolverFactory factory = new EasyTagFactory();
    try (Scanner s = new Scanner(template).useDelimiter("\n"))
    {
      for ( Tokenizer tokens = new Tokenizer(s) ; tokens.hasNext() ; )
      {
        Token start = tokens.next();
        factory.getResolver(start, tokens).resolve(start, data, out);
      }
    }

  }

}
