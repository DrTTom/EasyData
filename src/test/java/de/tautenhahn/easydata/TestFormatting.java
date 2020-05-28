package de.tautenhahn.easydata;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Tests for indenting and hiding line breaks
 * @author tt
 */
public class TestFormatting extends DataIntoTemplateBase
{
    @Test
    void useWriter() throws IOException
    {
        try (StringWriter data = new StringWriter();
             FormattingWriter systemUnderTest = new FormattingWriter(data, "   ");
             FormattingWriter other = new FormattingWriter(systemUnderTest, "___"))
        {
            for (String token : new String[] {"bla ", "bla\n", "blub\n", "hi\n"})
            {
                systemUnderTest.write(token);
            }
            other.write("hu");
            other.write("hu\n");
            systemUnderTest.write("Oh!");
            assertThat(data.toString()).isEqualTo("   bla bla\n" + "   blub\n" + "   hi\n" + "   ___huhu\n" + "   Oh!");
        }
    }

    @Test
    void indentTag() throws IOException
    {
        String template = "<@DEFINE GROUP(group)><@= group.name>: {\n"
            + "<@INDENT><@FOR element: group.children><@USE \"GROUP\" element><@END><@VALUE>   <@END>}\n<@END><@SKIP>\n"
            + "<@GROUP root>";
        AccessibleData data = AccessibleData.byBean(Map.of("root", Map.of("name", "ROOT", "children", List.of(
            Map.of("name", "CHILD", "children",
                List.of(Map.of("name", "GRANDCHILD", "children", Collections.EMPTY_LIST)))))));
        String result = doExpand(template, data, '<', '@', '>');
        assertThat(result).isEqualTo(
            "ROOT: {\n" + "   CHILD: {\n" + "      GRANDCHILD: {\n" + "      }\n" + "   }\n" + "}\n\n");
    }
}
