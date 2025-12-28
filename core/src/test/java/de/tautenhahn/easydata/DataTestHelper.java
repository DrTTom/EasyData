package de.tautenhahn.easydata;

import com.google.gson.Gson;
import de.tautenhahn.easydata.engine.AccessibleData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Utility methods to get data for tests and feed it into the converter.
 */
public final class DataTestHelper {
    private DataTestHelper() { // no instances
    }

    /**
     * Reads some data from a JSON file from test resources.
     *
     * @param path points to JSON file within class path
     * @return parsed data
     * @throws IOException in case of streaming problems
     */
    public static AccessibleData getData(String path) throws IOException {
        try (InputStream jsonRes = DataTestHelper.class.getResourceAsStream(path);
             Reader reader = new InputStreamReader(Objects.requireNonNull(jsonRes), StandardCharsets.UTF_8)) {
            return AccessibleData.byBean(new Gson().fromJson(reader, Map.class));
        }
    }

    /**
     * Reads some data from a JSON content.
     *
     * @param json contains the data
     * @return parsed data
     */
    public static AccessibleData getDataFromJsonContent(String json) {
        return AccessibleData.byBean(new Gson().fromJson(json, Map.class));
    }

    /**
     * Actually performs the expanding.
     *
     * @param template  contains the tags to expand
     * @param data      values to insert
     * @param beginning character opening the special tags
     * @param marker    second character opening the special tags
     * @param ending    closing the special tags
     * @return expanded template.
     * @throws IOException in case of streaming problems
     */
    public static String doExpand(String template, AccessibleData data, char beginning, char marker, char ending)
            throws IOException {
        try (Reader reader = new StringReader(template);
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            DataIntoTemplate systemUnderTest = new DataIntoTemplate(data, beginning, marker, ending);
            systemUnderTest.fillData(reader, writer);
            writer.flush();
            return out.toString(StandardCharsets.UTF_8);
        }
    }
}
