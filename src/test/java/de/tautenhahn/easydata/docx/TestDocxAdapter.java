package de.tautenhahn.easydata.docx;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import org.junit.Test;

import de.tautenhahn.easydata.DataIntoTemplate;
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
    try (InputStream source = TestDocxAdapter.class.getResourceAsStream("/example.docx");
      OutputStream destination = new FileOutputStream(Paths.get("build", "example.docx").toFile()))
    {
      DataIntoTemplate expander = new DataIntoTemplate(getData("/data.json"), '(', '@', ')');
      DocxAdapter systemUnderTest = new DocxAdapter(expander);
      systemUnderTest.convert(source, destination);
    }
  }
}
