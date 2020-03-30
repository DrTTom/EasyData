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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.tautenhahn.easydata.AccessibleData;
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

  private static final String RELATIONS_ENTRY_NAME = "word/_rels/document.xml.rels";

  private static final int LIMIT = 1024 * 1024 * 1024;

  private final DataIntoTemplate expander;

  private final MediaProvider media;

  /**
   * Creates new instance.
   *
   * @param data data to fill into the template
   */
  public DocxAdapter(AccessibleData data)
  {
    this(data, null);
  }

  /**
   * Creates new instance.
   *
   * @param data data to fill into the template
   * @param media provides additional images to embed into the DOCX
   */
  public DocxAdapter(AccessibleData data, MediaProvider media)
  {
    this.expander = new DataIntoTemplate(new SanitizingData(data), '(', '@', ')');
    this.media = media;
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
    try (ZipInputStream ins = new ZipInputStream(source);
      ZipOutputStream out = new ZipOutputStream(destination))
    {
      while (true)
      {
        ZipEntry entry = ins.getNextEntry();
        if (entry == null)
        {
          break;
        }
        out.putNextEntry(targetEntry(entry));
        if (!entry.isDirectory())
        {
          copyContent(ins, out, entry.getName());
        }
      }
      if (media != null)
      {
        media.writeContentsTo(out);
      }
    }
  }

  private ZipEntry targetEntry(ZipEntry entry)
  {
    String name = entry.getName();
    if (SPECIAL_ENTRY_NAME.equals(name) || media != null && RELATIONS_ENTRY_NAME.equals(name))
    {
      return new ZipEntry(name);
    }
    return entry;
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
    else if (media != null && RELATIONS_ENTRY_NAME.equals(name))
    {
      addReferences(ins, out);
    }
    else
    {
      copy(ins, out);
    }
  }

  private void addReferences(InputStream ins, OutputStream out) throws IOException
  {
    Charset encoding = StandardCharsets.UTF_8;
    String content = new String(ins.readAllBytes(), encoding);
    out.write(content.substring(0, content.indexOf("</Relationships>")).getBytes(encoding));
    media.writeRefsTo(out);
    out.write("</Relationships>".getBytes(encoding));
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

  /**
   * Masks a few characters to avoid destroying internal XML syntax of the DOCX.
   */
  private static class SanitizingData extends AccessibleData
  {

    SanitizingData(AccessibleData original)
    {
      super(original.getData());
    }

    @Override
    public String getString(String attrName)
    {
      return super.getString(attrName).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
  }
}
