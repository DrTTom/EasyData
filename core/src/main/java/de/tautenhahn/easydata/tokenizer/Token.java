package de.tautenhahn.easydata.tokenizer;

import java.io.Serial;
import java.io.Serializable;

/**
 * Wraps some part of the template file together with its position in the original template.
 *
 * @author TT
 */
public record Token(String content, int row, int col) implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Override
  public String toString() {
    return String.format("%3d:%3d %s", row, col, content);
  }
}