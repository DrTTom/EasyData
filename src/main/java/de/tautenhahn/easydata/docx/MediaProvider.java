package de.tautenhahn.easydata.docx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Holds media to embed into a DOCX like images. Instances are not thread-safe!
 * 
 * @author tt
 */
public class MediaProvider
{

  private int idSource = 1000;

  private static final String REL = "<Relationship Id=\"{id}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/{fileName}\"/>";

  private final Map<String, String> allRefs = new LinkedHashMap<>();

  private final Map<String, Supplier<InputStream>> contents = new LinkedHashMap<>();


  /**
   * Adds an image specified by file.
   * 
   * @param f file
   * @return generated ID
   */
  public String addImage(File f)
  {
    return addImage(f.getName(), () -> {
      try
      {
        return new FileInputStream(f);
      }
      catch (FileNotFoundException e)
      {
        return null;
      }
    });
  }

  /**
   * Adds an image specified by name and content supplier
   * 
   * @param name file name
   * @param sup supplies an input stream with content
   * @return generated ID
   */
  public String addImage(String name, Supplier<InputStream> sup)
  {
    String result = "rId" + idSource++;
    allRefs.put(result, name);
    contents.put(name, sup);
    return result;
  }

  void writeRefsTo(OutputStream out) throws IOException
  {
    for ( Entry<String, String> ref : allRefs.entrySet() )
    {
      out.write(REL.replace("{id}", ref.getKey())
                   .replace("{fileName}", ref.getValue())
                   .getBytes(StandardCharsets.UTF_8));
    }
  }

  void writeContentsTo(ZipOutputStream out) throws IOException
  {
    for ( Entry<String, Supplier<InputStream>> file : contents.entrySet() )
    {
      out.putNextEntry(new ZipEntry("word/media/" + file.getKey()));
      file.getValue().get().transferTo(out);
    }
  }
}
