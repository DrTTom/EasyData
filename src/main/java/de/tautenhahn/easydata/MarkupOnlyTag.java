package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Deletes all plain content and retains only such content which is inserted by some special tag. That allows
 * writing several consecutive (complex) special tags into separate lines and even add comments without having
 * these extra line breaks and comments in the output. The second part "COMMENT" is not really needed, everything wich
 * is not in a special tag will be treated as comment anyway.
 *
 * @author TT
 */
public class MarkupOnlyTag extends ComplexTag {

    static final Pattern PATTERN = Pattern.compile("MARKUP_ONLY");

    MarkupOnlyTag(Matcher startMatcher, Iterator<Token> remaining, ResolverFactory factory) {
        super(startMatcher, remaining, factory, "COMMENT", "/MARKUP_ONLY");
    }

    @Override
    public void resolve(Token start, AccessibleData data, Writer output) throws IOException {
        for (Map.Entry<Token, Resolver> entry : content.entrySet()) {
            Resolver resolver = entry.getValue();
            if (resolver.resolvesSpecialMarkup()) {
                resolver.resolve(entry.getKey(), data, output);
            }
        }
    }
}
