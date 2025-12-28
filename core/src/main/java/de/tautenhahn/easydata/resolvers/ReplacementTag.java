package de.tautenhahn.easydata.resolvers;

import de.tautenhahn.easydata.engine.AccessibleData;
import de.tautenhahn.easydata.tokenizer.Token;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a replacement to be applied to all data String values whenever EasyData resolves another tag.
 * This tag itself is resolved to an empty String.
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
