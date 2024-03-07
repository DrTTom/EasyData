package de.tautenhahn.easydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.tautenhahn.easydata.AccessibleData.ListMode;


/**
 * Some unit tests for accessing data elements. Note that this class misses several tests which could be
 * written as exercise.
 *
 * @author TT
 */
class TestAccessibleData
{

  private static AccessibleData systemUnderTest;

  /**
   * Provides some data to create documents with.
   *
   * @throws IOException in case of streaming problems
   */
  @BeforeAll
  static void provideData() throws IOException
  {
    try (InputStream jsonRes = TestDataIntoTemplate.class.getResourceAsStream("/data.json");
      Reader reader = new InputStreamReader(Objects.requireNonNull(jsonRes), StandardCharsets.UTF_8))
    {
      systemUnderTest = AccessibleData.byJsonReader(reader);
    }
  }

  /**
   * Assert that values can be addressed and sorted properly.
   */
  @Test
  void sort()
  {
    Collection<Object> result = systemUnderTest.getCollection("Hobbys", ListMode.DEFAULT);
    result = systemUnderTest.sort(result, null, true);

    assertThat(result).containsExactly("Feuerschlucken", "Schlafen", "Tanzen");
    result = systemUnderTest.getCollection("Hobbys", ListMode.KEYS);
    result = systemUnderTest.sort(result, null, false);
    assertThat(result).containsExactly("2", "1", "0");
  }

  /**
   * Assert that bean attributes can be accessed.
   */
  @Test
  void bean()
  {
    Map<String, Calendar> bean = Map.of("bean", Calendar.getInstance());
    AccessibleData byBean = AccessibleData.byBean(bean);
    assertThat(byBean.getCollection("bean", ListMode.KEYS)).contains("timeZone");
    assertThat(byBean.getString("bean.timeZone")).isNotEmpty();
  }

  /**
   * Asserts that the size of collections can be obtained. Because all data is treated as String, the size
   * comes as String as well.
   */
  @Test
  void size()
  {
    assertThat(systemUnderTest.getString("SIZE(Hobbys)")).isEqualTo("3");
    assertThat(systemUnderTest.getString("SIZE{Hobbys}")).isEqualTo("3");
  }

  /**
   * Assert that path selecting wrong target type produces a comprehensive error message.
   */
  @Test
  void wrongTarget()
  {
    assertThatThrownBy(() -> systemUnderTest.getString("Hobbys")).isInstanceOf(ResolverException.class)
                                                                 .hasMessageContaining("expected String but Hobbys is of type java.util.ArrayList");
  }

  /**
   * Assert that path selecting wrong target type produces a comprehensive error message.
   */
  @Test
  void wrongTarget2()
  {
    assertThatThrownBy(() -> systemUnderTest.getCollection("Hobbys.0",
                                                           ListMode.DEFAULT)).isInstanceOf(ResolverException.class)
                                                                             .hasMessageContaining("expected complex object but Hobbys.0 is a java.lang.String");
  }

  /**
   * Assert that path selecting wrong target type produces a comprehensive error message.
   */
  @Test
  void attributeOfPrimitive()
  {
    assertThatThrownBy(() -> systemUnderTest.getString("Name.shortName")).isInstanceOf(ResolverException.class)
                                                                         .hasMessageContaining("No property 'shortName' supported for element of type java.lang.String");
  }

  /**
   * Assert that overwriting an existing attribute can be done and un-defining it restores the original value.
   * Overwriting attributes is necessary to enable recursions.
   */
  @Test
  void duplicateDefinition()
  {
    String oldValue = systemUnderTest.getString("Name");
    systemUnderTest.define("Name", "Ludmilla");
    assertThat(systemUnderTest.getString("Name")).isEqualTo("Ludmilla");
    systemUnderTest.undefine("Name");
    assertThat(systemUnderTest.getString("Name")).isEqualTo(oldValue);
  }

  /**
   * Assert that getting attributes works with read-only objects.
   */
  @Test
  void accessReadOnlyContent()
  {
    assertThat(AccessibleData.byBean(new ExampleData()).get("value")).isEqualTo("egal");
  }

  /**
   * Assert that getting attributes works with read-only objects.
   */
  @Test
  void formatContent()
  {
    AccessibleData testee = AccessibleData.byBean(Map.of("date", new Date(0)));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd", Locale.GERMAN);
    testee.addFormatter((content, path)-> content instanceof Date date ? sdf.format(date): content );
    assertThat(testee.getString("date")).isEqualTo("1970.01.01");
  }

  /**
   * Assert that String literals are just returned, not resolved.
   */
  @Test
  void literalsAreReturned()
  {
    AccessibleData testee = AccessibleData.byBean(Map.of("date", new Date(0)));
    assertThat(testee.getString("\"date\"")).isEqualTo("date");
    assertThat(testee.getString("'date'")).isEqualTo("date");
    assertThat(testee.getString("#date#")).isEqualTo("date");
    assertThat(testee.getString("#date#")).isEqualTo("date");
    assertThat(testee.get("true")).isEqualTo(Boolean.TRUE);
    assertThat(testee.get("5")).isEqualTo(5);
  }

  /**
   * Example for a class wich has a getter but no setter to match it.
   */
  public static class ExampleData {
    /**
     * @return some value
     */
    public String getValue() {
      return "egal";
    }
  }
}
