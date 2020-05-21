package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolver to apply a pre-defined macro.
 *
 * @author TT
 */
public class MacroTag implements Resolver
{

    private final Map<Token, Resolver> content;

    private final Pattern pattern;

    private final List<String> paramNames;

    MacroTag(String name, List<String> paramNames, Map<Token, Resolver> content)
    {
        this.paramNames = paramNames;
        this.content = content;
        StringBuffer regex = new StringBuffer(name);
        paramNames.forEach(n -> regex.append(" +([^ ]+)"));
        this.pattern = Pattern.compile(regex.toString());
    }

    @Override
    public void resolve(Token start, AccessibleData data, Writer output) throws IOException
    {
        try
        {
            String str = start.getContent();
            Matcher m = pattern.matcher(str.substring(2, str.length() - 1).trim());
            m.matches();
            for (int i = 0; i < paramNames.size(); i++)
            {
                data.define(paramNames.get(i), data.get(m.group(i + 1)));
            }
            for (Entry<Token, Resolver> entry : content.entrySet())
            {
                entry.getValue().resolve(entry.getKey(), data, output);
            }
            paramNames.forEach(data::undefine);
        } catch (ResolverException e)
        {
            e.addLocation(start);
            throw e;
        }
    }

    /**
     * @return the pattern to recognize this tag by, contains name and correct number of groups for parameters.
     */
    public Pattern getPattern()
    {
        return pattern;
    }
}
