package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a macro for further use, specifying name, parameter list and content. This tag itself is resolved
 * to an empty String.
 *
 * @author TT
 */
public class ReplacementTag extends ComplexTag {

    static final Pattern PATTERN = Pattern.compile("REPLACEMENT +(.+)");

    private final Matcher startMatcher;

    ReplacementTag(Matcher startMatcher, Iterator<Token> remaining, ResolverFactory factory) {
        super(startMatcher, remaining, factory, "COMMENT", "/REPLACEMENT");
        this.startMatcher = startMatcher;
    }

    @Override
    public void resolve(Token start, AccessibleData data, Writer output) {
        String name = startMatcher.group(1);
        try (Writer replacement = new StringWriter()) {
            resolveContent(content, data, replacement);
            data.defineReplacement(name, replacement.toString());
        } catch (IOException e) {
            throw new IllegalStateException("cannot happen when writing on a String", e);
        }
    }

}
