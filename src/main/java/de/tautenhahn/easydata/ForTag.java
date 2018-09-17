package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.regex.Matcher;

import de.tautenhahn.easydata.AccessableData.ListMode;
import de.tautenhahn.easydata.AccessableData.SortMode;


/**
 * Repetitions are made by repeatedly resolving the content.
 *
 * @author TT
 */
public class ForTag extends ComplexTag
{

  private final Matcher start;

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
  public void resolve(Token startTag, AccessableData data, Writer output) throws IOException
  {
    String addressedCollection = start.group(2);
    ListMode mode = ListMode.DEFAULT;
    if (addressedCollection.endsWith(".keys"))
    {
      addressedCollection = addressedCollection.substring(0, addressedCollection.length() - ".keys".length());
      mode = ListMode.KEYS;
    }
    else if (addressedCollection.endsWith(".values"))
    {
      addressedCollection = addressedCollection.substring(0,
                                                          addressedCollection.length() - ".values".length());
      mode = ListMode.VALUES;
    }
    String definedName = start.group(1);

    for ( Iterator<Object> iter = data.getIterator(addressedCollection,
                                                   mode,
                                                   SortMode.NONE,
                                                   null) ; iter.hasNext() ; )
    {
      data.define(definedName, iter.next());
      resolveContent(content, data, output);
      if (iter.hasNext())
      {
        resolveContent(otherContent, data, output);
      }
      data.undefine(definedName);
    }
  }

}
