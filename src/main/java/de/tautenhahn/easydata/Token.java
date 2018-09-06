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
   * @param content
   * @param row
   * @param col
   */
  public Token(String content, int row, int col)
  {
    this.content = content;
    this.row = row;
    this.col = col;
  }

  /**
   * Returns represented template part.
   */
  public String getContent()
  {
    return content;
  }

  /**
   * Returns original row.
   */
  public int getRow()
  {
    return row;
  }

  /**
   * Returns original column.
   */
  public int getCol()
  {
    return col;
  }

  @Override
  public String toString()
  {
    return String.format("%3d:%3d %s", row, col, content);
  }
}
