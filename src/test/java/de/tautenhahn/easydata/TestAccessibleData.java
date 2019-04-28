package de.tautenhahn.easydata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tautenhahn.easydata.AccessibleData.ListMode;


/**
 * Some unit tests for accessing data elements. Note that this class misses several tests which could be
 * written as exercise.
 *
 * @author TT
 */
public class TestAccessibleData
{

  private static AccessibleData systemUnderTest;

  /**
   * Provides some data to create documents with.
   *
   * @throws IOException
   */
  @BeforeClass
  public static void provideData() throws IOException
  {
    try (InputStream jsonRes = TestDataIntoTemplate.class.getResourceAsStream("/data.json");
      Reader reader = new InputStreamReader(jsonRes, StandardCharsets.UTF_8))
    {
      systemUnderTest = AccessibleData.byJsonReader(reader);
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
   * Assert that bean attributes can be accessed.
   */
  @Test
  public void bean()
  {
    AccessibleData byBean = AccessibleData.byBean(Map.of("bean", Calendar.getInstance()));
    assertThat("bean keys", byBean.getCollection("bean", ListMode.KEYS), hasItem("timeZone"));
    assertThat("bean value", byBean.getString("bean.timeZone"), notNullValue());
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
    assertThrows("expected String but Hobbys is of type java.util.ArrayList",
                 IllegalArgumentException.class,
                 () -> systemUnderTest.getString("Hobbys"));
  }

  /**
   * Assert that path selecting wrong target type produces a comprehensive error message.
   */
  @Test
  public void wrongTarget2()
  {
    assertThrows("expected complex object but Hobbys.0 is a java.lang.String",
                 IllegalArgumentException.class,
                 () -> systemUnderTest.getCollection("Hobbys.0", ListMode.DEFAULT));
  }

  /**
   * Assert that path selecting wrong target type produces a comprehensive error message.
   */
  @Test
  public void attributeOfPrimitive()
  {
    assertThrows("No property 'shortName' supported for element of type java.lang.String",
                 IllegalArgumentException.class,
                 () -> systemUnderTest.getString("Name.shortName"));
  }

  /**
   * Assert that overwriting an existing attribute produces a comprehensive error message.
   */
  @Test
  public void duplicateDefinition()
  {
    assertThrows("cannot re-define existing key Name",
                 IllegalArgumentException.class,
                 () -> systemUnderTest.define("Name", "Ludmilla"));
  }


}
