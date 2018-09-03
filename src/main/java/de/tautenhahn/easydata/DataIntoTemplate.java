package de.tautenhahn.easydata;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
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
   * @param data Map as parsed without type restrictions by GSON.
   * @param encoding
   * @param marker character to recognize the special tags by.
   */
  public DataIntoTemplate(Map<String, Object> data, Charset encoding, char marker)
  {
    // TODO Auto-generated constructor stub
  }

  /**
   * Reads input and expands all the special tags using data.
   * 
   * @param template
   * @param out where the output is written to.
   */
  public void fillData(InputStream template, ByteArrayOutputStream out)
  {
    // TODO Auto-generated method stub

  }

}
