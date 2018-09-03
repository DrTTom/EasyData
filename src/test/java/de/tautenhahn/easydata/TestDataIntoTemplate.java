package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;




/**
 * Creating some kind of document by inserting data into an appropriate template is a recurring task. There
 * are many implementations, for instance XSLT for XML, luatex for LaTeX, lots of frameworks for HTML. <br>
 * Just how much functionality do we need to create a useful document? What do we have to assume about the
 * target language? How complicated will it get?<br>
 * This is a simple example which targets creation of arbitrary documents using data from a JSON file.
 * 
 * @author TT
 */
public class TestDataIntoTemplate
{

  /**
   * Because there is no assumption made about the target document, we need to mark our special syntax with at
   * least one character which must be freely chosen in order not to interfere with the target syntax.
   */
  private static final char MARKER = '@';

  private static Map<String, Object> exampleData = new HashMap<>();

  @SuppressWarnings("unchecked")
  @BeforeClass
  public static void getData() throws IOException
  {
    try (InputStream json = TestDataIntoTemplate.class.getResourceAsStream("/data.json");
      Reader reader = new InputStreamReader(json, StandardCharsets.UTF_8))
    {
      exampleData = new Gson().fromJson(reader, Map.class);
    }
  }

  /**
   * Template and document should be ideally handled streaming. First task is to expand some placeholder
   * addressing a single data value. Assume that
   * <ul>
   * <li>Data is represented as list or map or with String keys, nested objects, all primitive values are
   * handled as String.</li>
   * <li>For sake of simplicity, we treat a lists as maps with keys "0", "1" and so on.</li>
   * <ul>
   * Introduce the [@=...] tag here.
   * 
   * @throws IOException
   */
  @Test
  public void insertSingleValues() throws IOException
  {
    String result = doExpand("[@=Name] wohnt in [@=Adress.City]");
    assertThat("created document", result, equalTo("Horst wohnt in Wolkenkuckuksheim."));
  }

  /**
   * To freely access data, we need to use one expression to qualify another one. Use ${...} to de-reference
   * inside an expression. Do not worry about mixing up syntax, we are i a marked tag here.
   * 
   * @throws IOException
   */
  @Test
  public void useReference() throws IOException
  {
    String result = doExpand("Sein liebstes Hobby ist [@=Hobbies.${index}]");
    assertThat("created document", result, equalTo("Sein liebstes Hobby ist Feuerschlucken."));
  }

  /**
   * Need an iteration, in best case over keys and values of a map (or array treated as map).
   */

  /**
   * Need conditional parts.
   */

  private String doExpand(String template) throws IOException
  {
    try (InputStream ins = new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8));
      ByteArrayOutputStream out = new ByteArrayOutputStream())
    {
      DataIntoTemplate systemUnderTest = new DataIntoTemplate(exampleData, StandardCharsets.UTF_8, MARKER);
      systemUnderTest.fillData(ins, out);
      return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
