package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.tautenhahn.easydata.AccessibleData.ListMode;


/**
 * Repetitions are made by repeatedly resolving the content. Furthermore, this tag supports some data
 * operations to avoid extra pre-processing of data:
 * <ul>
 * <li>SELECT - replaces each iterated data element by a specified attribute of it
 * <li>
 * <li>ASCENDING/DESCENDING - sorts elements, optionally by some attribute</li>
 * <li>UNIQUE - skips all repeated elements</li>
 * </ul>
 * Additionally, we support inserting some content between the iterated resolved text.
 *
 * @author TT
 */
public class ForTag extends ComplexTag
{

  private static final String PATH_REGEX = "(" + InsertValueTag.REGEX_SIMPLE_EXPRESSION + ")";

  /**
   * How to recognize this tag.
   */
  public static final Pattern PATTERN = Pattern.compile("FOR +(\\w+) *: *" + PATH_REGEX + "( +SELECT +"
                                                        + PATH_REGEX
                                                        + ")?( +UNIQUE)?( +(ASCENDING|DESCENDING)( +"
                                                        + PATH_REGEX + ")?)? *");

  static final int GROUP_NAME = 1;

  static final int GROUP_COLLECTION = 2;

  static final int GROUP_SELECT = 4;

  static final int GROUP_UNIQUE = 5;

  static final int GROUP_ORDER = 7;

  static final int GROUP_ORDER_ATTR = 9;

  private final Matcher start;

  /**
   * Creates new instance.
   *
   * @param start matched first token
   * @param remaining remaining tokens, should be read until end tag is found
   * @param factory provides the objects to resolve nested tags with
   */
  public ForTag(Matcher start, Iterator<Token> remaining, ResolverFactory factory)
  {
    super(start, remaining, factory, "DELIM", "/FOR");
    this.start = start;
  }

  @Override
  public void resolve(Token startTag, AccessibleData data, Writer output) throws IOException
  {
    try
    {
      String addressedCollection = start.group(GROUP_COLLECTION);
      ListMode mode = ListMode.DEFAULT;
      if (addressedCollection.endsWith(".keys"))
      {
        addressedCollection = addressedCollection.substring(0,
                                                            addressedCollection.length() - ".keys".length());
        mode = ListMode.KEYS;
      }
      else if (addressedCollection.endsWith(".values"))
      {
        addressedCollection = addressedCollection.substring(0,
                                                            addressedCollection.length()
                                                               - ".values".length());
        mode = ListMode.VALUES;
      }
      String definedName = start.group(GROUP_NAME);

      for ( Iterator<Object> iter = getIterator(data, addressedCollection, mode) ; iter.hasNext() ; )
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
    catch (ResolverException e)
    {
      e.addLocation(startTag);
      throw e;
    }
  }

  private Iterator<Object> getIterator(AccessibleData data, String addressedCollection, ListMode mode)
  {
    Collection<Object> col = data.getCollection(addressedCollection, mode);
    String selectAttr = start.group(GROUP_SELECT);
    if (selectAttr != null)
    {
      col = data.map(col, selectAttr);
    }
    String orderOperator = start.group(GROUP_ORDER);
    if (orderOperator != null)
    {
      col = data.sort(col, start.group(GROUP_ORDER_ATTR), "ASCENDING".equals(orderOperator));
    }
    if (start.group(GROUP_UNIQUE) != null)
    {
      col = col.stream().distinct().collect(Collectors.toList());
    }
    return col.iterator();
  }
}
