package de.tautenhahn.easydata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;

import org.junit.jupiter.api.Test;


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
    assertThat(m.matches()).isTrue();
    checkValues(m, "x", "customers", "address.city", "DESCENDING", "distance");
    assertThat(m.group(ForTag.GROUP_UNIQUE)).isNull();

    m = ForTag.PATTERN.matcher("FOR x: customers.${targetField}  UNIQUE DESCENDING");
    assertThat(m.matches()).isTrue();
    checkValues(m, "x", "customers.${targetField}", null, "DESCENDING", null);
    assertThat(m.group(ForTag.GROUP_UNIQUE)).isNotEmpty();
  }

  private void checkValues(Matcher m,
                           String name,
                           String collection,
                           String select,
                           String order,
                           String attribute)
  {
    assertThat(m.group(ForTag.GROUP_NAME)).isEqualTo(name);
    assertThat(m.group(ForTag.GROUP_COLLECTION)).isEqualTo(collection);
    assertThat(m.group(ForTag.GROUP_SELECT)).isEqualTo(select);
    assertThat(m.group(ForTag.GROUP_ORDER)).isEqualTo(order);
    assertThat(m.group(ForTag.GROUP_ORDERATTR)).isEqualTo(attribute);
  }
}
