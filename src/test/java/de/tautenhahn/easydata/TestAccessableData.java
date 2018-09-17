package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import de.tautenhahn.easydata.AccessableData.ListMode;
import de.tautenhahn.easydata.AccessableData.SortMode;


/**
 * Some unit tests for accessing data elements.
 *
 * @author TT
 */
public class TestAccessableData
{

  private static AccessableData systemunderTest;

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
      systemunderTest = new AccessableData(new Gson().fromJson(reader, Map.class));
    }
  }

  /**
   * Assert that values cen be sorted properly.
   */
  @Test
  public void sort()
  {
    List<String> result = new ArrayList<>();
    for ( Iterator<Object> iter = systemunderTest.getIterator("Hobbies", // NOPMD
                                                              ListMode.DEFAULT,
                                                              SortMode.ASCENDING,
                                                              null) ; iter.hasNext() ; )
    {
      result.add((String)iter.next());
    }
    assertThat("sorted", result, contains("Feuerschlucken", "Schlafen", "Tanzen"));
    result.clear();
    for ( Iterator<Object> iter = systemunderTest.getIterator("Hobbies", // NOPMD
                                                              ListMode.KEYS,
                                                              SortMode.DECENDING,
                                                              null) ; iter.hasNext() ; )
    {
      result.add((String)iter.next());
    }
    assertThat("sorted", result, contains("2", "1", "0"));
  }

}
