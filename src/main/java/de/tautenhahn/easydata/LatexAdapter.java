package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;


/**
 * Handling LaTeX input is easy. The only task her is to sanitize the input data. This adapter could be
 * extended to call some LaTeX by itself but that would not simplify usage more than it complicates
 * configuration.
 *
 * @author TT
 */
public class LatexAdapter
{

  private final DataIntoTemplate expander;

  /**
   * Creates new instance.
   *
   * @param data data to fill into the template
   */
  public LatexAdapter(AccessibleData data)
  {
    this.expander = new DataIntoTemplate(new SanitizingData(data), '<', '@', '>');
  }

  /**
   * Creates the document expanding any special tags in the template.
   *
   * @param source contains the template
   * @param destination to write the result into
   * @throws IOException in case of streaming problems
   */
  public void convert(InputStream source, OutputStream destination) throws IOException
  {
    try (Reader reader = new InputStreamReader(source, StandardCharsets.UTF_8);
      Writer writer = new OutputStreamWriter(destination, StandardCharsets.UTF_8))
    {
      expander.fillData(reader, writer);
    }
  }

  /**
   * Masks a few characters to avoid destroying LaTeX syntax.
   */
  private static class SanitizingData extends AccessibleData
  {

    SanitizingData(AccessibleData original)
    {
      super(original);
    }

    @Override
    public String getString(String attrName)
    {
      return super.getString(attrName).replace("&", "\\&").replace("\\n", "\\newline");
    }
  }
}
