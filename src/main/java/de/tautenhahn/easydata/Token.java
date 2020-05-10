package de.tautenhahn.easydata;

/**
 * Wraps some part of the template file together with its position in the original template.
 *
 * @author TT
 */
public class Token
{

  private final String content;

  private final int row;

  private final int col;

  /**
   * Creates immutable instance
   *
   * @param content wrapped text part
   * @param row location of that part
   * @param col location of that part
   */
  public Token(String content, int row, int col)
  {
    this.content = content;
    this.row = row;
    this.col = col;
  }

  /**
   * @return represented template part.
   */
  public String getContent()
  {
    return content;
  }

  /**
   * @return original row number
   */
  public int getRow()
  {
    return row;
  }

  /**
   * @return original column number of token start
   */
  public int getCol()
  {
    return col;
  }

  @Override
  public String toString()
  {
    return String.format("%3d:%3d %s", Integer.valueOf(row), Integer.valueOf(col), content);
  }
}
