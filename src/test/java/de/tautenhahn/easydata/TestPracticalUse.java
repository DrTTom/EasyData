package de.tautenhahn.easydata;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * These are not tests for the classes but for the usability of the concept.
 *
 * @author TT
 */
public class TestPracticalUse
{

    /**
     * Prove that the tool is suitable for creating a LaTeX document. Test data is all fake stuff, obviously.
     *
     * @throws IOException
     */
    @Test
    public void cvAsLatex() throws IOException
    {
        AccessibleData cv = null;
        try (InputStream jsonRes = TestPracticalUse.class.getResourceAsStream("/cv.json");
             Reader reader = new InputStreamReader(jsonRes, StandardCharsets.UTF_8))
        {
            cv = AccessibleData.byJsonReader(reader);
        }
        try (InputStream tiRes = TestPracticalUse.class.getResourceAsStream("/cv_template.tex");
             Reader template = new InputStreamReader(tiRes, StandardCharsets.UTF_8);
             OutputStream outRes = new FileOutputStream("cv.tex");
             Writer document = new OutputStreamWriter(outRes, StandardCharsets.UTF_8))
        {
            DataIntoTemplate systemUnderTest = new DataIntoTemplate(cv, '<', '@', '>');
            systemUnderTest.fillData(template, document);
        }
    }

    /**
     * Check that in case of data mismatch helpful error messages appear which enable the user to correct a template.
     * This time, use data from a java object.<br> Note that iteration over the top level object is not supported
     * because syntax needs a name of the object.
     *
     * @throws IOException
     */
    @Test
    public void errors() throws IOException
    {
        Object data = List.of(Map.of("header", "first block", "lines", List.of("bla", "bla")),
            Map.of("header", "second block", "lines", List.of("one", "two")));
        String template = "Output: (@FOR label:items)(@=label.header) (@END)";

        try (Reader reader = new StringReader(template); Writer writer = new StringWriter())
        {
            DataIntoTemplate systemUnderTest = new DataIntoTemplate(AccessibleData.byBean(data), '(', '@', ')');

            assertThatThrownBy(() -> systemUnderTest.fillData(reader, writer))
                .isInstanceOf(ResolverException.class)
                .hasMessageContaining("Invalid index 'items', addressed object has elements 0-1")
                .hasMessageContaining("1:  8 (@FOR label:items)");
        }
    }
}
