package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


/**
 * Repetitions are made by repeatedly resolving the content.
 *
 * @author TT
 */
public class ForTag extends ComplexTag
{

  private final Matcher start;

  /**
   * just because we need a three-valued type.
   */
  private enum ListPreferrence
  {
    BY_TYPE, KEYS, VALUES;
  }

  /**
   * Creates new instance.
   *
   * @param start
   * @param remaining
   * @param factory
   */
  public ForTag(Matcher start, Iterator<Token> remaining, String delim, String end, ResolverFactory factory)
  {
    super(remaining, delim, end, factory);
    this.start = start;
  }

  @Override
  public void resolve(Token startTag, Object data, Writer output) throws IOException
  {
    String addressedCollection = start.group(2);
    ListPreferrence mode = ListPreferrence.BY_TYPE;
    if (addressedCollection.endsWith(".keys"))
    {
      addressedCollection = addressedCollection.substring(0, addressedCollection.length() - ".keys".length());
      mode = ListPreferrence.KEYS;
    }
    else if (addressedCollection.endsWith(".values"))
    {
      addressedCollection = addressedCollection.substring(0,
                                                          addressedCollection.length() - ".values".length());
      mode = ListPreferrence.VALUES;
    }

    Object subData = InsertValueTag.getAttribute(startTag, addressedCollection, data);
    if (subData == null)
    {
      throw InsertValueTag.createDataRefException(null, startTag, addressedCollection);
    }
    Map<String, Object> allData = (Map)data; // overall object is always a map
    String definedName = start.group(1);
    for ( Iterator<Object> iter = iterate(startTag, subData, mode) ; iter.hasNext() ; )
    {
      allData.put(definedName, iter.next());
      resolveContent(content, allData, output);
      if (iter.hasNext())
      {
        resolveContent(otherContent, allData, output);
      }
    }
  }

  private Iterator<Object> iterate(Token tag, Object subData, ListPreferrence mode)
  {
    if (subData instanceof Map)
    {
      return mode == ListPreferrence.VALUES ? ((Map)subData).values().iterator()
        : ((Map)subData).keySet().iterator();
    }
    if (subData instanceof List)
    {
      return mode == ListPreferrence.KEYS ? null : ((List)subData).iterator(); // TODO
    }
    throw new IllegalArgumentException("No object or array found at line " + tag.getRow() + ", col."
                                       + tag.getCol());
  }


}
