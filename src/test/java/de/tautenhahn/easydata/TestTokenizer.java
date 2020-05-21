package de.tautenhahn.easydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import java.util.Scanner;

/**
 * Unit test for splitting input into handy tokens. Those tokens must have the following properties:
 * <ul>
 * <li>concatenating all the tokens yields the original input</li>
 * <li>each special tag '[#some content]' is returned in a separate token</li>
 * <li>characters marking the special tag can be chosen freely</li>
 * <ul>
 * Handling of huge inputs should be done streaming. We assume the input is handled line by line.
 *
 * @author TT
 */
public class TestTokenizer
{

    /**
     * Checks that targeted tokens will be found and that input can be restored.
     */
    @Test
    public void splitInput()
    {
        String tag = "[#=Name]";
        boolean found = false;
        String source = "example [][][[" + tag + "[]]\n\n]\n";
        try (Scanner sRes = new Scanner(source); Scanner inputRes = sRes.useDelimiter("\n"))
        {
            StringBuilder copy = new StringBuilder();
            for (Tokenizer systemUnderTest = new Tokenizer(inputRes, '[', '#', ']'); systemUnderTest.hasNext(); )
            {
                Token token = systemUnderTest.next();
                if (tag.equals(token.getContent()))
                {
                    assertThat(token.getRow()).isEqualTo(1);
                    assertThat(token.getCol()).isEqualTo(14);
                    found = true;
                    assertThat(token.toString()).contains("  1: 14");
                }
                copy.append(token.getContent());
            }
            assertThat(copy.toString()).isEqualTo(source);
            assertThat(found).isTrue();
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
        try (Scanner sRes = new Scanner(source); Scanner inputRes = sRes.useDelimiter("\n"))
        {
            Tokenizer systemUnderTest = new Tokenizer(inputRes, '{', '$', '}');
            systemUnderTest.next();
            String content = systemUnderTest.next().getContent();
            assertThat(content).isEqualTo(tag);
        }
    }

    /**
     * Asserts that scanners delimiter is checked.
     */
    @Test
    public void wrongScanner()
    {
        try (Scanner s = new Scanner("whatever"))
        {
            assertThatThrownBy(() -> new Tokenizer(s, '[', '#', ']')).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
