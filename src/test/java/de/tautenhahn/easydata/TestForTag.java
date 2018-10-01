package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;

import org.junit.Test;


/**
 * Unit tests for the ForTag which shall be able to sort and map collections so data pre-processing will be
 * obsolete in several applications.
 *
 * @author TT
 */
public class TestForTag
{

  /**
   * Makes sure pattern fits intended syntax.
   */
  @Test
  public void pattern()
  {
    Matcher m = ForTag.PATTERN.matcher("FOR x: customers SELECT address.city DESCENDING distance");
    assertTrue("matches", m.matches());
    checkValues(m, "x", "customers", "address.city", "DESCENDING", "distance");
    assertThat("unique", m.group(ForTag.GROUP_UNIQUE), nullValue());

    m = ForTag.PATTERN.matcher("FOR x: customers.${targetField}  UNIQUE DESCENDING");
    assertTrue("matches", m.matches());
    checkValues(m, "x", "customers.${targetField}", null, "DESCENDING", null);
    assertThat("unique", m.group(ForTag.GROUP_UNIQUE), not(nullValue()));
  }

  private void checkValues(Matcher m,
                           String name,
                           String collection,
                           String select,
                           String order,
                           String attribute)
  {
    assertThat("name", m.group(ForTag.GROUP_NAME), equalTo(name));
    assertThat("collection", m.group(ForTag.GROUP_COLLECTION), equalTo(collection));
    assertThat("select unique attribute values", m.group(ForTag.GROUP_SELECT), equalTo(select));
    assertThat("ordering", m.group(ForTag.GROUP_ORDER), equalTo(order));
    assertThat("attribute to order by", m.group(ForTag.GROUP_ORDERATTR), equalTo(attribute));
  }

}
