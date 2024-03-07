package de.tautenhahn.easydata;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for the {@link MarkupOnlyTag} which allows complex markup to be written in several lines without
 * cluttering the output.
 *
 * @author TT
 */
class TestMarkupOnlyTag extends DataIntoTemplateBase
{

  /**
   * Makes sure regex pattern fits intended syntax.
   */
  @Test
  void resolve() throws IOException {
    String template = """
            {@MARKUP_ONLY}
               {@IF value=='a'}a is a nice value{@/IF}
               {@IF value=='b'}b is also good{@/IF}
            {@/MARKUP_ONLY}.""";
    AccessibleData data = AccessibleData.byBean(Map.of("value", "a"));
    String result = doExpand(template, data, '{', '@', '}');
    assertThat(result).isEqualTo("a is a nice value.\n");
  }

}
