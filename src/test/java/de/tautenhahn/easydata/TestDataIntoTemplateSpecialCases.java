package de.tautenhahn.easydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * This is the next level of inserting data into a template. See {@link TestDataIntoTemplate} for basic functions. Now
 * focus on error handling and some more advanced forms of data access.
 *
 * @author TT
 */
public class TestDataIntoTemplateSpecialCases extends DataIntoTemplateBase
{

    private static AccessibleData exampleData;

    /**
     * Provides some data to create documents with.
     *
     * @throws IOException
     */
    @BeforeAll
    public static void provideData() throws IOException
    {
        exampleData = getData("/data.json");
    }

    /**
     * Asserts that wrong attribute references are reported.
     *
     * @throws IOException
     */
    @Test
    public void wrongAttribute() throws IOException
    {
        assertThatThrownBy(() -> doExpand("Wrong reference [@=Hobies.wrongAttribute]"))
            .isInstanceOf(ResolverException.class)
            .hasMessageContaining("no object to resolve wrongAttribute with");
    }

    /**
     * Asserts that misspelled special tag produces a comprehensive error message.
     *
     * @throws IOException
     */
    @Test
    public void unsupportedTag() throws IOException
    {
        assertThatThrownBy(() -> doExpand("no such Tag [@WHILE true] haha [@END]"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("unrecognized token   1: 12 [@WHILE true]");
    }

    /**
     * Asserts that missing end tag produces a comprehensive error message.
     *
     * @throws IOException
     */
    @Test
    public void missingEnd() throws IOException
    {
        assertThatThrownBy(() -> doExpand("no such Tag [@IF Name==\"Friedbert\"] Was für ein altmodischer Name!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unexpected end of input, pending IF Name==\"Friedbert\", missing [@END]");
    }

    /**
     * Documents might want to display data in a certain order, so introduce sorting. Furthermore, duplicate elements
     * might be unwanted.
     *
     * @throws IOException
     */
    @Test
    public void sortElementsByAttribute() throws IOException
    {
        String result =
            doExpand("Städte alphabetisch: [@FOR friend:friends.values UNIQUE DESCENDING city][@= friend.city] [@END]");
        assertThat(result).isEqualTo("Städte alphabetisch: Rom Gera Berlin \n");
        result = doExpand(
            "Städte nach Abstand: [@FOR friend:friends.values UNIQUE ASCENDING distance][@= friend.city] [@END]");
        assertThat(result).isEqualTo("Städte nach Abstand: Gera Berlin Rom \n");
    }

    /**
     * One step further: data elements should appear in groups defined by some attribute. Instead of iterating over the
     * data elements, the document needs to iterate the values of that attribute. <br> Furthermore, developers might be
     * inclined to write "friends[name]" instead of the implemented "friends.${name}", so allow that form as well.
     *
     * @throws IOException
     */
    @Test
    public void groupElementsByAttribute() throws IOException
    {
        String result = doExpand("Freunde nach Stadt: [@FOR city:friends.values SELECT city UNIQUE] [@= city]:"
            + "[@FOR name:friends][@IF city==friends[name].city] [@=name][@END][@END][@DELIM], [@END]");
        assertThat(result).isEqualTo("Freunde nach Stadt:  Gera: Emil,  Rom: Oskar,  Berlin: Heinz Franz\n");
    }

    /**
     * The IF tag becomes much more useful with some additional operators. Introduce ">" and function "SIZE".
     *
     * @throws IOException
     */
    @Test
    public void size() throws IOException
    {
        String result = doExpand("Kinder: [@IF SIZE(children)>0]JA![@ELSE]nein.[@END]");
        assertThat(result).isEqualTo("Kinder: nein.\n");
        result = doExpand("Hobbys: [@IF SIZE(Hobbys)<10][@=SIZE(Hobbys)][@ELSE]viele[@END]");
        assertThat(result).isEqualTo("Hobbys: 3\n");
    }

    private String doExpand(String template) throws IOException
    {
        return doExpand(template, exampleData, '[', '@', ']');
    }
}
