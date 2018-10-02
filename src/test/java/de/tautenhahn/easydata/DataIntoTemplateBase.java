package de.tautenhahn.easydata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.Gson;


/**
 * Base class for tests which expand a given template with data read from a JSON file.
 *
 * @author TT
 */
public class DataIntoTemplateBase
{

  /**
   * Reads some data from a JSON file from test resources.
   *
   * @throws IOException
   */
  protected static AccessibleData getData(String path) throws IOException
  {
    try (InputStream json = DataIntoTemplateBase.class.getResourceAsStream(path);
      Reader reader = new InputStreamReader(json, StandardCharsets.UTF_8))
    {
      return new AccessibleData(new Gson().fromJson(reader, Map.class));
    }
  }

  /**
   * Returns expanded template.
   *
   * @param template
   * @param data
   * @param beginning
   * @param marker
   * @param ending
   * @throws IOException
   */
  protected String doExpand(String template, AccessibleData data, char beginning, char marker, char ending)
    throws IOException
  {
    try (InputStream ins = new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8));
      Reader reader = new InputStreamReader(ins, StandardCharsets.UTF_8);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8))
    {
      DataIntoTemplate systemUnderTest = new DataIntoTemplate(data, beginning, marker, ending);
      systemUnderTest.fillData(reader, writer);
      writer.flush();
      return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
