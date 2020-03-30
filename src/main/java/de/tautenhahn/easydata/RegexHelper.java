package de.tautenhahn.easydata;

/**
 * Just building regular expressions.
 *
 * @author TT
 */
final class RegexHelper
{

  private static final String SPECIAL_CHARS = "\\.[]{}()<>*+-=?^$|";

  private RegexHelper()
  {
    // no instances
  }

  /**
   * @return a regex matching the char literally.
   * @param value input
   */
  public static String mask(char value)
  {

    return (SPECIAL_CHARS.indexOf(value) >= 0 ? "\\" : "") + value;
  }

  /**
   * @return true if regex matches a unique line break
   * @param regex input
   */
  public static boolean isLineBreak(String regex)
  {
    return "\n".equals(regex) || "\r\n".equals(regex);
  }
}
