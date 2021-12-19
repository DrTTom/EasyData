package de.tautenhahn.easydata;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;


/**
 * Writer which adds indenting at the beginning of each line.
 */
public class FormattingWriter extends FilterWriter
{

  private final String indent;

  private boolean newLine = true;

  /**
   * Create a new filtered writer.
   *
   * @param out a Writer object to provide the underlying stream.
   * @param indent usually blanks or tabs to insert before each line
   * @throws NullPointerException if <code>out</code> is <code>null</code>
   */
  protected FormattingWriter(Writer out, String indent)
  {
    super(out);
    this.indent = indent;
  }

  @Override
  public void write(String str) throws IOException
  {
    if (newLine)
    {
      out.write(indent);
    }
    out.write(str);
    newLine = str.endsWith("\n");
  }

  @Override
  public void close()
  {
    // nothing on purpose
  }
}
