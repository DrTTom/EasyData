package de.tautenhahn.easydata.docx;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.junit.Test;

import de.tautenhahn.easydata.AccessibleData;
import de.tautenhahn.easydata.DataIntoTemplateBase;


/**
 * Unit tests for handling DOCX files. Note that if the template is created with LibreOffice, it has to be
 * re-opened and saved again for correct format!
 *
 * @author TT
 */
public class TestDocxAdapter extends DataIntoTemplateBase
{

  /**
   * Make sure that converting runs without error. Look into the created DOCX file to assert that it has
   * correct content.
   *
   * @throws IOException
   * @throws FileNotFoundException
   */
  @Test
  public void createDocument() throws FileNotFoundException, IOException
  {
    try (InputStream source = getRes("/example.docx");
      OutputStream destination = new FileOutputStream(Paths.get("build", "example.docx").toFile()))
    {
      DocxAdapter systemUnderTest = new DocxAdapter(getData("/data.json"));
      systemUnderTest.convert(source, destination);
    }
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
  public void embedImages() throws Exception
  {
    MediaProvider media = new MediaProvider();
    String data = "{images: [{name:\"Erwin\", id:\"id1\", cx:\"cx1\"}, {name:\"Knusperinchen\", id:\"id2\", cx:\"cx2\"}]}";
    data = data.replace("id1", media.addImage("Mustermann.png", () -> getRes("/Mustermann.png")));
    data = data.replace("cx1", Integer.toString(getCX("/Mustermann.png", 949_960)));
    data = data.replace("id2", media.addImage("hamster.jpg", () -> getRes("/hamster.jpg")));
    data = data.replace("cx2", Integer.toString(getCX("/hamster.jpg", 949_960)));
    AccessibleData ad = AccessibleData.byJsonContent(data);

    try (InputStream source = getRes("/images.docx");
      OutputStream destination = new FileOutputStream(Paths.get("build", "images.docx").toFile()))
    {
      DocxAdapter systemUnderTest = new DocxAdapter(ad, media);
      systemUnderTest.convert(source, destination);
    }
  }

  private int getCX(String path, int cy) throws IOException
  {
    BufferedImage img = ImageIO.read(getRes(path));
    return cy * img.getWidth() / img.getHeight();
  }

  private InputStream getRes(String path)
  {
    return TestDocxAdapter.class.getResourceAsStream(path);
  }
}
