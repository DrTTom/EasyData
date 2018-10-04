package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


/**
 * This is the next level of inserting data into a template. See {@link TestDataIntoTemplate} for basic
 * functions. Now focus on error handling and some more advanced forms of data access.
 *
 * @author TT
 */
public class TestDataIntoTemplateSpecialCases extends DataIntoTemplateBase
{

  /**
   * for checking error cases.
   */
  @Rule
  public ExpectedException expected = ExpectedException.none();

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
   * Documents might want to display data in a certain order, so introduce sorting. Furthermore, duplicate
   * elements might be unwanted.
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
   * One step further: data elements should appear in groups defined by some attribute. Instead of iterating
   * over the data elements, the document needs to iterate the values of that attribute.
   *
   * @throws IOException
   */
  @Test
  public void groupElementsByAttribute() throws IOException
  {
    String result = doExpand("Freunde nach Stadt: [@FOR city:friends.values SELECT city UNIQUE] [@= city]:"
                             + "[@FOR name:friends][@IF city==friends.${name}.city] [@=name][@END][@END][@DELIM], [@END]");
    assertThat("Freunde gruppiert nach Stadt: ",
               result,
               equalTo("Freunde nach Stadt:  Gera: Emil,  Rom: Oskar,  Berlin: Heinz Franz\n"));
  }

  /**
   * The IF tag becomes much more useful with some additional operators. Introduce ">" and function "SIZE".
   *
   * @throws IOException
   */
  @Test
  public void size() throws IOException
  {
    String result = doExpand("Kinder: [@IF SIZE(children)>0]JA![@ELSE]nein.[@END]");
    assertThat("result", result, equalTo("Kinder: nein.\n"));
    result = doExpand("Hobbys: [@IF SIZE(Hobbys)<5][@=SIZE(Hobbys)][@ELSE]viele[@END]");
    assertThat("result", result, equalTo("Hobbys: 3\n"));
  }

  private String doExpand(String template) throws IOException
  {
    return doExpand(template, exampleData, '[', '@', ']');
  }
}
