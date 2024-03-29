package de.tautenhahn.easydata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;


/**
 * Unit test for main class.
 *
 * @author TT
 */
class TestMain
{

  private final Path sourceDir = Paths.get("src", "test", "resources");

  /**
   * just a smoke test
   *
   * @throws IOException to appear in test protocol
   */
  @Test
  void createDocx() throws IOException
  {
    try (PrintStream out = new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8))
    {
      Main.setOut(out);
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

}
