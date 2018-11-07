package de.tautenhahn.easydata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;


/**
 * Unit test for main class.
 *
 * @author TT
 */
public class TestMain
{

  private final Path sourceDir = Paths.get("src", "test", "resources");

  /**
   * just a smoke test
   *
   * @throws IOException
   */
  @Test
  public void createDocx() throws Exception
  {
    try (PrintStream out = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8"))
    {
      setOut(out);
      Main.main(sourceDir.resolve("data.json").toString(),
                sourceDir.resolve("example.docx").toString(),
                Paths.get("build", "example.docx").toString(),
                "(@)");
      Main.main();
      Main.main(sourceDir.resolve("cv.json").toString(),
                sourceDir.resolve("cv_template.tex").toString(),
                Paths.get("build", "cv.tex").toString());
    }
  }

  private static void setOut(PrintStream out)
  {
    Main.out = out;
  }
}
