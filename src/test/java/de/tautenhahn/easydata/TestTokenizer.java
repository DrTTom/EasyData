package de.tautenhahn.easydata;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Scanner;

import org.junit.Test;


/**
 * Unit test for splitting input into handy tokens.
 *
 * @author TT
 */
public class TestTokenizer
{

  /**
   * Checks that targeted tokens will be found.
   */
  @SuppressWarnings("boxing")
  @Test
  public void splitInput()
  {
    String tag = "[#=Name]";
    boolean found = false;
    String source = "example [][][[" + tag + "[]]\n\n]\n";
    try (Scanner s = new Scanner(source); Scanner input = s.useDelimiter("\n"))
    {
      StringBuilder copy = new StringBuilder();
      for ( Tokenizer systemUnderTest = new Tokenizer(input, '[', '#', ']') ; systemUnderTest.hasNext() ; )
      {
        Token token = systemUnderTest.next();
        if (tag.equals(token.getContent()))
        {
          assertThat("row", token.getRow(), equalTo(1));
          assertThat("col", token.getCol(), equalTo(14));
          found = true;
          assertThat("toString", token.toString(), containsString("  1: 14"));
        }
        copy.append(token.getContent());
      }
      assertThat("copy", copy.toString(), equalTo(source));
      assertTrue("token found", found);
    }
  }

  /**
   * Asserts that tokens can be found even if markers are used which appear inside the tag.
   */
  @Test
  public void collisionWithInnerStuff()
  {
    String tag = "{$= element.${i} }";
    String source = "i-th element is " + tag + ".\n";
    try (Scanner s = new Scanner(source); Scanner input = s.useDelimiter("\n"))
    {
      Tokenizer systemUnderTest = new Tokenizer(s, '{', '$', '}');
      systemUnderTest.next();
      String content = systemUnderTest.next().getContent();
      assertThat("found token", content, equalTo(tag));
    }
  }

  /**
   * Asserts that scanners delimiter is checked.
   */
  @SuppressWarnings("unused")
  @Test(expected = IllegalArgumentException.class)
  public void wrongScanner()
  {
    try (Scanner s = new Scanner("whatever"))
    {
      new Tokenizer(s, '[', '#', ']');
    }
  }

}
