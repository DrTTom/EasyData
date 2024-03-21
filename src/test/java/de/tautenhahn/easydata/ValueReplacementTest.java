package de.tautenhahn.easydata;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UnitTest for replacing character sequences in data values. That feature is used when text strings from the data
 * shall support paragraph breaks and are to be inserted into a docx-File. In that case simple line breaks do not work.
 * <p>
 * Whereas it is a cleaner solution to keep all the formatting in the template, there are practical cases where this
 * is not always possible.
 */
class ValueReplacementTest extends DataIntoTemplateBase {

    /**
     * Defines a macro and uses it.
     *
     * @throws IOException to appear in test protocol
     */
    @Test
    void replaceWithinDataString() throws IOException {
        AccessibleData data = AccessibleData.byBean(Map.of("value", "Long text from obscure source, _NEWTHOUGHT_ which needs a very special break."));
        String template = "{@REPLACEMENT _NEWTHOUGHT_}\n -- new thought -- \n{@/REPLACEMENT}{@=value}";
        String result = doExpand(template, data, '{', '@', '}');
        assertThat(result).isEqualTo("""
                Long text from obscure source,\s
                 -- new thought --\s
                 which needs a very special break.
                """);
    }

}
