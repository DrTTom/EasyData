package de.tautenhahn.easydata;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;


/**
 * Entry class into template expansion.
 * 
 * @author TT
 */
public class DataIntoTemplate
{

  /**
   * Creates an instance filled with some data.
   * 
   * @param data Map containing Maps, Arrays and Strings (as parsed without type restrictions by GSON).
   * @param marker character to recognize the special tags by.
   */
  public DataIntoTemplate(Map<String, Object> data, char marker)
  {
    // TODO Auto-generated constructor stub
  }

  /**
   * Reads input and expands all the special tags using data.
   * 
   * @param template
   * @param out where the output is written to.
   */
  public void fillData(Reader template, Writer out)
  {
    // TODO Auto-generated method stub

  }

}
