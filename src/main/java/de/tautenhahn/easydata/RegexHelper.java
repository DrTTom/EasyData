package de.tautenhahn.easydata;

/**
 * Just building regular expressions.
 *
 * @author TT
 */
public final class RegexHelper
{

  private static final String SPECIAL_CHARS = "\\.[]{}()<>*+-=?^$|";

  private RegexHelper()
  {
    // no instances
  }

  /**
   * Returns a regex matching the char literally.
   *
   * @param value
   */
  public static String mask(char value)
  {

    return (SPECIAL_CHARS.indexOf(value) >= 0 ? "\\" : "") + value;
  }

  /**
   * Returns true if given regex matches only to a string equal to the regex String.
   *
   * @param regex
   */
  public static boolean isUniqueMatch(String regex)
  {
    for ( char c : regex.toCharArray() )
    {
      if (SPECIAL_CHARS.indexOf(c) != -1)
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Return true if regex matches a unique line break
   *
   * @param regex
   */
  public static boolean isLineBreak(String regex)
  {
    return "\n".equals(regex) || "\r\n".equals(regex);
  }
}
