package de.tautenhahn.easydata;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;


/**
 * Unit tests for using the macro tags. Being able to define and use macros opens a whole new dimension of
 * possibilities.
 *
 * @author TT
 */
class TestMacros extends DataIntoTemplateBase
{

  /**
   * Defines a macro and uses it.
   *
   * @throws IOException to appear in test protocol
   */
  @Test
  void useMacro() throws IOException
  {
    AccessibleData data = getData("/data.json");
    String template = "{@DEFINE Oskar}my best friend{@END}{@DEFINE Emil(_data)}the guy from {@=_data.city}{@END}"
                      + "{@Oskar}, {@Emil friends.Emil}";

    String result = doExpand(template, data, '{', '@', '}');
    assertThat(result).isEqualTo("my best friend, the guy from Gera\n");
  }

  /**
   * Take the name of a macro to use from data content. That function is necessary to expand keywords in the
   * data into pre-defined text paragraphs.
   *
   * @throws IOException to appear in test protocol
   */
  @Test
  void useMacroFreeName() throws IOException
  {
    AccessibleData data = getData("/data.json");
    String template = "(@DEFINE Gera {_attribute, _distance})A (@=_attribute) place (@=_distance) km away(@/DEFINE)"
            + "(@USE friends.Emil.city \"nice\" friends.Emil.distance)";

    String result = doExpand(template, data, '(', '@', ')');
    assertThat(result).isEqualTo("A nice place 90 km away\n");
  }

  /**
   * Define a Macro with parameters when special tags use braces as it is done by the DOCX adapter.
   *
   * @throws IOException to appear in test protocol
   */
  @Test
  void useMacroBraces() throws IOException
  {
    AccessibleData data = getData("/data.json");
    String template = "{@DEFINE Gera (_attribute, _distance)}A {@=_attribute} place {@=_distance} km away{@END}"
            + "{@USE friends.Emil.city \"nice\" friends.Emil.distance}";

    String result = doExpand(template, data, '{', '@', '}');
    assertThat(result).isEqualTo("A nice place 90 km away\n");
  }

  /**
   * Define and reference a pre-computed value.
   *
   * @throws IOException to appear in test protocol
   */
  @Test
  void defineValue() throws IOException
  {
    AccessibleData data = getData("/data.json");
    String template = "{@SET myValue=friends.Emil.city}{@=myValue}";

    String result = doExpand(template, data, '{', '@', '}');
    assertThat(result).isEqualTo("Gera\n");
  }
}
