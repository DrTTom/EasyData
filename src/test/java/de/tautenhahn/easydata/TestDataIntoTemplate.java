package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Creating some kind of document by inserting data into an appropriate template is a recurring task. There
 * are many implementations, for instance XSLT for XML, luatex for LaTeX, lots of frameworks for HTML. <br>
 * Just how much functionality do we need to create a useful document? What do we have to assume about the
 * target language? How complicated will it get?<br>
 * This is a simple example which targets creation of arbitrary documents using data from a JSON file.
 *
 * @author TT
 */
public class TestDataIntoTemplate extends DataIntoTemplateBase
{

  /**
   * Because there is no assumption made about the target document, we need to mark our special syntax with at
   * least one character which must be freely chosen in order not to interfere with the target syntax. Think
   * about further configurable characters.
   */
  private static final char MARKER = '@';

  private static AccessibleData exampleData;

  /**
   * Provides some data to create documents with.
   *
   * @throws IOException
   */
  @BeforeClass
  public static void provideData() throws IOException
  {
    exampleData = getData("/data.json");
  }

  /**
   * Template and document should be ideally handled streaming. First task is to expand some placeholder
   * addressing a single data value. For sake of simplicity, we treat a lists as maps with keys "0", "1" and
   * so on. <br>
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
   * inside an expression. Do not worry about mixing up syntax, we are inside a marked tag here.
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
   * Need conditional parts. What kind of conditions do we need? At least an "==" operator is needed, assume
   * there are value literals supported as well. <br>
   * Introducing tags [@IF ==] [@ELSE] [@END]
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


  private String doExpand(String template) throws IOException
  {
    return doExpand(template, exampleData, '[', MARKER, ']');
  }
}
