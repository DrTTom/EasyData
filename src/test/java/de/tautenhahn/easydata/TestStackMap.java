package de.tautenhahn.easydata;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Unit test for stacks map.
 *
 * @author tt
 */
public class TestStackMap
{
    /**
     * calls the needed methods.
     */
    @Test
    public void check()
    {
        Map<String, Object> systemUnderTest = new StackMap();
        final String key = "key";
        systemUnderTest.put(key, "value1");
        systemUnderTest.put(key, "value2");
        assertThat(systemUnderTest.size()).isEqualTo(1);
        assertThat(systemUnderTest.get(key)).isEqualTo("value2");

        assertThat(systemUnderTest.remove(key)).isEqualTo("value2");
        assertThat(systemUnderTest.size()).isEqualTo(1);
        assertThat(systemUnderTest.get(key)).isEqualTo("value1");

        assertThat(systemUnderTest.remove(key)).isEqualTo("value1");
        assertThat(systemUnderTest.isEmpty()).isEqualTo(true);
    }
}
