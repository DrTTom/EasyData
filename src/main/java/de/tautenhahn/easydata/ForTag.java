package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Repetitions are made by repeatedly resolving the content.
 *
 * @author TT
 */
public class ForTag extends ComplexTag
{

  /**
   * Creates new instance.
   *
   * @param token
   * @param remaining
   * @param factory
   */
  public ForTag(Token token, Iterator<Token> remaining, ResolverFactory factory)
  {
    super(remaining, "[@DELIM]", factory);
  }

  @Override
  public void resolve(Token start, Object data, Writer output) throws IOException
  {
    // Quick implementation without parsing the start tag (wrong) to check base class and get a feel for what
    // is needed.
    Map<String, Object> subData = (Map<String, Object>)EqualsTag.getAttribute("friends", data);
    Map<String, Object> allData = new HashMap<>((Map)data);
    for ( Iterator iter = subData.keySet().iterator() ; iter.hasNext() ; )
    {
      allData.put("name", iter.next());
      resolveContent(content.iterator(), allData, output);
      if (iter.hasNext())
      {
        resolveContent(otherContent.iterator(), allData, output);
      }
    }
  }

}
