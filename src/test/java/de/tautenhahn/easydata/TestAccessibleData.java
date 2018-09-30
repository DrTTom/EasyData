package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import de.tautenhahn.easydata.AccessibleData.ListMode;


/**
 * Some unit tests for accessing data elements.
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
  @SuppressWarnings("unchecked")
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
    Collection<Object> result = systemUnderTest.getCollection("Hobbys",
                                                              ListMode.DEFAULT); 
    systemUnderTest.sort(result, null, true);
    assertThat("sorted", result, contains("Feuerschlucken", "Schlafen", "Tanzen"));
    result= systemUnderTest.getCollection("Hobbys", ListMode.KEYS);
    systemUnderTest.sort(result, null, false);    
    assertThat("sorted", result, contains("2", "1", "0"));
  }

}
