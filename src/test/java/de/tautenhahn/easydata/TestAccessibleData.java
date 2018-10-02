package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gson.Gson;

import de.tautenhahn.easydata.AccessibleData.ListMode;


/**
 * Some unit tests for accessing data elements. Note that this class misses several tests which could be
 * written as exercise.
 *
 * @author TT
 */
public class TestAccessibleData
{

  /**
   * for checking error cases.
   */
  @Rule
  public ExpectedException expected = ExpectedException.none();

  private static AccessibleData systemUnderTest;

  /**
   * Provides some data to create documents with.
   *
   * @throws IOException
   */
  @BeforeClass
  public static void getData() throws IOException
  {
    try (InputStream json = TestDataIntoTemplate.class.getResourceAsStream("/data.json");
      Reader reader = new InputStreamReader(json, StandardCharsets.UTF_8))
    {
      systemUnderTest = new AccessibleData(new Gson().fromJson(reader, Map.class));
    }
  }

  /**
   * Assert that values can be addressed and sorted properly.
   */
  @Test
  public void sort()
  {
    Collection<Object> result = systemUnderTest.getCollection("Hobbys", ListMode.DEFAULT);
    result = systemUnderTest.sort(result, null, true);
    assertThat("sorted", result, contains("Feuerschlucken", "Schlafen", "Tanzen"));
    result = systemUnderTest.getCollection("Hobbys", ListMode.KEYS);
    result = systemUnderTest.sort(result, null, false);
    assertThat("sorted", result, contains("2", "1", "0"));
  }

  /**
   * Asserts that the size of collections can be obtained. Because all data is treated as String, the size
   * comes as String as well.
   */
  @Test
  public void size()
  {
    assertThat("size", systemUnderTest.getString("SIZE(Hobbys)"), equalTo("3"));
  }

  /**
   * Assert that path selecting wrong target type produces a comprehensive error message.
   */
  @Test
  public void wrongTarget()
  {
    expected.expectMessage("expected String but Hobbys is of type java.util.ArrayList");
    systemUnderTest.getString("Hobbys");
  }

  /**
   * Assert that path selecting wrong target type produces a comprehensive error message.
   */
  @Test
  public void wrongTarget2()
  {
    expected.expectMessage("expected complex object but Hobbys.0 is a java.lang.String");
    systemUnderTest.getCollection("Hobbys.0", ListMode.DEFAULT);
  }

  /**
   * Assert that path selecting wrong target type produces a comprehensive error message.
   */
  @Test
  public void attributeOfPrimitive()
  {
    expected.expectMessage("No property 'shortName' supported for element of type java.lang.String");
    systemUnderTest.getString("Name.shortName");
  }

  /**
   * Assert that overwriting an existing attribute produces a comprehensive error message.
   */
  @Test
  public void duplicateDefinition()
  {
    expected.expectMessage("cannot re-define existing key Name");
    systemUnderTest.define("Name", "Ludmilla");
  }


}
