package de.tautenhahn.easydata.adapter;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;


import de.tautenhahn.easydata.DataTestHelper;
import de.tautenhahn.easydata.engine.AccessibleData;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Unit tests for handling DOCX files. Note that if the template is created with LibreOffice, it has to be
 * re-opened and saved again for correct format!
 *
 * @author TT
 */
class TestDocxAdapter  {

    /**
     * Make sure that converting runs without error. Look into the created DOCX file to assert that it has
     * correct content.
     *
     * @throws IOException           to appear in test protocol
     */
    @Test
    void createDocument() throws IOException {
        Path outputPath = Paths.get("build", "example.docx");
        try (InputStream source = getRes("/example.docx");
             OutputStream destination = Files.newOutputStream(outputPath)) {
            DocxAdapter systemUnderTest = new DocxAdapter(DataTestHelper.getData("/data.json"));
            systemUnderTest.convert(source, destination);
        }
        assertThat(Files.exists(outputPath)).isTrue();
    }

    /**
     * Now try embedding images into the DOCX. Sorry, in this case the template was not simply written with Word
     * or LibreOffice but the XML was edited using a text editor. <br>
     * TODO: Unfortunately, the image size is stored in DOCX in absolute width and height. To preserve ratio, we
     * need to input at least one of the values according to the respective image. So, the client has to find
     * out the ratio before adding the image.
     *
     * @throws Exception to appear in the test protocol
     */
    @Test
    void embedImages() throws Exception {
        MediaProvider media = new MediaProvider();
        String data = "{images: [{name:\"Erwin\", id:\"id1\", cx:\"cx1\"}, {name:\"Knusperinchen\", id:\"id2\", cx:\"cx2\"}]}";
        data = data.replace("id1", media.addImage("Mustermann.png", () -> getRes("/Mustermann.png")));
        data = data.replace("cx1", Integer.toString(getCX("/Mustermann.png", 949_960)));
        data = data.replace("id2", media.addImage(new File("src/test/resources/hamster.jpg")));
        data = data.replace("cx2", Integer.toString(getCX("/hamster.jpg", 949_960)));
        AccessibleData ad = DataTestHelper.getDataFromJsonContent(data);

        Path outputPath = Paths.get("build", "images.docx");
        try (InputStream source = getRes("/images.docx");
             OutputStream destination = Files.newOutputStream(outputPath)) {
            DocxAdapter systemUnderTest = new DocxAdapter(ad, media);
            systemUnderTest.convert(source, destination);
        }
        assertThat(Files.exists(outputPath)).isTrue();
    }

    private int getCX(String path, int cy) throws IOException {
        try (InputStream res = getRes(path)) {
            BufferedImage img = ImageIO.read(res);
            return cy * img.getWidth() / img.getHeight();
        }
    }

    private InputStream getRes(String path) {
        return TestDocxAdapter.class.getResourceAsStream(path);
    }
}
