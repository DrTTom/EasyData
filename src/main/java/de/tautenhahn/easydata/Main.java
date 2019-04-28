package de.tautenhahn.easydata;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import de.tautenhahn.easydata.docx.DocxAdapter;


/**
 * Command line interface for document creation.
 *
 * @author TT
 */
public final class Main
{

  static PrintStream out = System.out;

  private Main()
  {
    // no instances
  }

  /**
   * Command line call.
   *
   * @param args data file (JSON), template file, output file, markers (3 characters, defaults to "(@)" for
   *          DOCX and "<@>" otherwise.
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static void main(String... args) throws FileNotFoundException, IOException
  {
    if (args.length < 3)
    {
      out.println("Usage: Main <data> <template> <output> [markers] \nwhere\n   data is path of a JSON file"
                  + "\n   template is a document template file containing special tags to be replaced"
                  + "\n   output is the destination file name"
                  + "\n   markers consists of 3 characters marking the special tags, for instance \"<@>\"");
      return;
    }
    AccessibleData data = AccessibleData.byJsonPath(args[0]);
    String marker = getMarker(args);
    DataIntoTemplate expander = new DataIntoTemplate(data, marker.charAt(0), marker.charAt(1),
                                                     marker.charAt(2));
    try (InputStream src = new FileInputStream(args[1]); OutputStream destRes = new FileOutputStream(args[2]))
    {
      if (args[1].endsWith(".docx"))
      {
        new DocxAdapter(expander).convert(src, destRes);
      }
      else
      {
        try (Reader template = new InputStreamReader(src, StandardCharsets.UTF_8);
          Writer output = new OutputStreamWriter(destRes, StandardCharsets.UTF_8))
        {
          expander.fillData(template, output);
        }
      }
    }
  }

  private static String getMarker(String... args)
  {
    if (args.length < 4)
    {
      return args[1].endsWith(".docx") ? "(@)" : "<@>";
    }
    if (args[3].length() == 3)
    {
      return args[3];
    }
    throw new IllegalArgumentException("markers must have 3 characters: opening, marking, closing");
  }

}
