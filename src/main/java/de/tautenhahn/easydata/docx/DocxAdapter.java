package de.tautenhahn.easydata.docx;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.tautenhahn.easydata.DataIntoTemplate;


/**
 * Can expand macros in Microsoft Word documents. Microsoft Words format DOCX is a ZIP file containing XML
 * contents. It turned out that manipulating the XML file which contains the text content goes a far way to
 * creating the wanted DOCX result. Be aware that complicated manipulations still may break the consistency of
 * the word document. <br>
 * When creating DOCX templates with LibreOffice, load and safe the created document a second time. For some
 * reason unknown to me the data content, especially the coding of the special tags, is completely different
 * in the version saved first.
 *
 * @author TT
 */
public class DocxAdapter
{

  private final byte[] buf = new byte[1024 * 4];

  private static final String SPECIAL_ENTRY_NAME = "word/document.xml";

  private static final int LIMIT = 1024 * 1024 * 1024;

  private final DataIntoTemplate expander;

  /**
   * Creates new instance.
   *
   * @param expander must use special characters allowed within XML content (do not use &lt; and &gt;)
   */
  public DocxAdapter(DataIntoTemplate expander)
  {
    this.expander = expander;
  }

  /**
   * Creates the document expanding any special tags in the template.
   *
   * @param source
   * @param destination
   * @throws IOException
   */
  public void convert(InputStream source, OutputStream destination) throws IOException
  {
    try (ZipInputStream ins = new ZipInputStream(source);
      ZipOutputStream out = new ZipOutputStream(destination))
    {
      while (true)
      {
        ZipEntry entry = ins.getNextEntry();
        if (entry == null)
        {
          return;
        }
        out.putNextEntry(entry);
        if (!entry.isDirectory())
        {
          copyContent(ins, out, entry.getName());
        }
      }
    }
  }

  private void copyContent(InputStream ins, OutputStream out, String name) throws IOException
  {
    if (SPECIAL_ENTRY_NAME.equals(name))
    {
      try (InputStream safe = new SafeInputStream(ins);
        Reader reader = new InputStreamReader(safe, StandardCharsets.UTF_8);
        OutputStream nonclosing = new NonclosingOutputStream(out);
        Writer writer = new OutputStreamWriter(nonclosing, StandardCharsets.UTF_8))
      {
        expander.fillData(reader, writer);
      }
    }
    else
    {
      copy(ins, out);
    }
  }

  private void copy(InputStream ins, OutputStream out) throws IOException
  {
    try (InputStream safe = new SafeInputStream(ins))
    {
      while (true)
      {
        int read = safe.read(buf);
        if (read == -1)
        {
          return;
        }
        out.write(buf, 0, read);
      }
    }
  }

  /**
   * Just to avoid closing the output ZIP before ready,
   */
  private static class NonclosingOutputStream extends FilterOutputStream
  {

    NonclosingOutputStream(OutputStream out)
    {
      super(out);
    }

    @Override
    public void close()
    {
      // not closing on purpose
    }
  }

  /**
   * Avoids ZIP bombs.
   */
  private static class SafeInputStream extends FilterInputStream
  {

    private int numRead;

    SafeInputStream(InputStream in)
    {
      super(in);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
      if (numRead >= LIMIT)
      {
        throw new IOException("ZIP bomb suspected");
      }
      int result = super.read(b, off, Math.min(len, LIMIT - numRead));
      numRead += result;
      return result;
    }

    @Override
    public void close()
    {
      // not closing on purpose
    }
  }
}
