package de.tautenhahn.easydata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


/**
 * Base class for tests which expand a given template with data read from a JSON file.
 *
 * @author TT
 */
public class DataIntoTemplateBase {

    /**
     * Reads some data from a JSON file from test resources.
     *
     * @param path points to JSON file within class path
     * @return parsed data
     * @throws IOException in case of streaming problems
     */
    protected static AccessibleData getData(String path) throws IOException {
        try (InputStream jsonRes = DataIntoTemplateBase.class.getResourceAsStream(path);
             Reader reader = new InputStreamReader(Objects.requireNonNull(jsonRes), StandardCharsets.UTF_8)) {
            return AccessibleData.byJsonReader(reader);
        }
    }

    /**
     * Actually performs the expanding.
     * @param template  contains the tags to expand
     * @param data      values to insert
     * @param beginning character opening the special tags
     * @param marker    second character opening the special tags
     * @param ending    closing the special tags
     * @return expanded template.
     * @throws IOException in case of streaming problems
     */
    protected String doExpand(String template, AccessibleData data, char beginning, char marker, char ending)
            throws IOException {
        try (InputStream insRes = new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8));
             Reader reader = new InputStreamReader(insRes, StandardCharsets.UTF_8);
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            DataIntoTemplate systemUnderTest = new DataIntoTemplate(data, beginning, marker, ending);
            systemUnderTest.fillData(reader, writer);
            writer.flush();
            return out.toString(StandardCharsets.UTF_8);
        }
    }
}
