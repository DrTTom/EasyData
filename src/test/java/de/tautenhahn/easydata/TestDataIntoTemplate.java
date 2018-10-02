package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

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

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
   * for checking error cases.
   */
  @Rule
  public ExpectedException expected = ExpectedException.none();

  /**
   * Because there is no assumption made about the target document, we need to mark our special syntax with at
   * least one character which must be freely chosen in order not to interfere with the target syntax.
   */
  private static final char MARKER = '@';

  private static AccessibleData exampleData;

  /**
   * Provides some data to create documents with.
   *
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  @BeforeClass
  public static void getData() throws IOException
  {
    try (InputStream json = TestDataIntoTemplate.class.getResourceAsStream("/data.json");
      Reader reader = new InputStreamReader(json, StandardCharsets.UTF_8))
    {
      exampleData = new AccessibleData(new Gson().fromJson(reader, Map.class));
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
    String result = doExpand("[@=Name] wohnt in [@=Address.City].");
    assertThat("text with inserted attribute", result, equalTo("Horst wohnt in Wolkenkuckuksheim.\n"));
    result = doExpand("Erstes Hobby ist [@=Hobbys.0].");
    assertThat("text with inserted array element", result, equalTo("Erstes Hobby ist Tanzen.\n"));
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
    String result = doExpand("Sein liebstes Hobby ist [@=Hobbys.${index}].");
    assertThat("text with resolved reference", result, equalTo("Sein liebstes Hobby ist Feuerschlucken.\n"));
  }

  /**
   * Asserts that wrong attribute references are reported.
   *
   * @throws IOException
   */
  @Test
  public void wrongAttribute() throws IOException
  {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage(containsString("no object to resolve wrongAttribute with"));
    doExpand("Wrong reference [@=Hobies.wrongAttribute]");
  }

  /**
   * Asserts that misspelled special tag produces a comprehensive error message.
   *
   * @throws IOException
   */
  @Test
  public void unsupportedTag() throws IOException
  {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("unrecognized token   1: 12 [@WHILE true]");
    doExpand("no such Tag [@WHILE true] haha [@END]");
  }

  /**
   * Asserts that missing end tag produces a comprehensive error message.
   *
   * @throws IOException
   */
  @Test
  public void missingEnd() throws IOException
  {
    expected.expect(IllegalArgumentException.class);
    expected.expectMessage("unexpected end of input, missing [@END]");
    doExpand("no such Tag [@IF Name==\"Friedbert\"] Was für ein altmodischer Name!");
  }

  /**
   * Need an iteration, in best case over keys and values of a map (or array treated as map). Furthermore,
   * check whether nested tags work.<br>
   * Introducing tags [@FOR :] [@DELIM] [@END]
   */
  @Test
  public void repeatElements() throws IOException
  {
    String result = doExpand("Seine Freunde sind [@FOR name:friends.keys][@=name][@DELIM], [@END].");
    assertThat("text with repeated element",
               result,
               equalTo("Seine Freunde sind Emil, Oskar, Heinz, Franz.\n"));

    result = doExpand("Die wohnen in [@FOR friend:friends.values][@=friend.city][@DELIM], [@END].");
    assertThat("created document", result, equalTo("Die wohnen in Gera, Rom, Berlin, Berlin.\n"));

    result = doExpand("Sein liebstes Hobby ist [@FOR h:Hobbys.keys][@IF h==index][@=Hobbys.${h}][@END][@END].");
    assertThat("created document", result, equalTo("Sein liebstes Hobby ist Feuerschlucken.\n"));
  }

  /**
   * Need conditional parts. What kind of conditions do we need?
   */
  @Test
  public void conditional() throws IOException
  {
    String result = doExpand("[@IF index==\"2\"]Index ist 2.[@ELSE]Index ist anders.[@END]");
    assertThat("equals to constant", result, equalTo("Index ist 2.\n"));
    result = doExpand("Es ist [@IF Name==\"Franz\"][@ELSE]nicht [@END]Franz.");
    assertThat("equals to constant", result, equalTo("Es ist nicht Franz.\n"));

    result = doExpand("Der Exot ist [@FOR name:friends.keys][@IF friends.${name}.city==\"Rom\"][@=name][@END][@END].");
    assertThat("nested tags", result, equalTo("Der Exot ist Oskar.\n"));
  }

  /**
   * Advanced level of FOR-tags: sort elements by some attribute.
   *
   * @throws IOException
   */
  @Test
  public void sortElementsByAttribute() throws IOException
  {
    String result = doExpand("Städte alphabetisch: [@FOR friend:friends.values UNIQUE DESCENDING city][@= friend.city] [@END]");
    assertThat("result sorted by name", result, equalTo("Städte alphabetisch: Rom Gera Berlin \n"));
    result = doExpand("Städte nach Abstand: [@FOR friend:friends.values UNIQUE ASCENDING distance][@= friend.city] [@END]");
    assertThat("result sorted by distance", result, equalTo("Städte nach Abstand: Gera Berlin Rom \n"));
  }

  /**
   * Advanced level of FOR-tags: group elements.
   *
   * @throws IOException
   */
  @Test
  public void groupElementsByAttribute() throws IOException
  {
    String result = doExpand("Freunde nach Stadt: [@FOR city:friends.values SELECT city UNIQUE] [@= city]:"
                             + "[@FOR name:friends][@IF city==friends.${name}.city] [@=name][@END][@END][@DELIM], [@END]");
    assertThat("Freunde gruppirt nach Stadt: ",
               result,
               equalTo("Freunde nach Stadt:  Gera: Emil,  Rom: Oskar,  Berlin: Heinz Franz\n"));
  }

  private String doExpand(String template) throws IOException
  {
    try (InputStream ins = new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8));
      Reader reader = new InputStreamReader(ins, StandardCharsets.UTF_8);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8))
    {
      DataIntoTemplate systemUnderTest = new DataIntoTemplate(exampleData, '[', MARKER, ']');
      systemUnderTest.fillData(reader, writer);
      writer.flush();
      return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
