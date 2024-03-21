package de.tautenhahn.easydata;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Supports conditional output with very simple conditions.
 *
 * @author TT
 */
public class IfTag extends ComplexTag {

    private static final String VALUE_KEY = "VALUE";

    private final Matcher start;

    /**
     * How to recognize this tag.
     */
    public static final Pattern PATTERN = Pattern.compile("IF +([^=><!]+) *((==|!=|>|<|<=|>=) *(.+))?");

    /**
     * Creates new instance.
     *
     * @param start     matched first token
     * @param remaining remaining tokens, should be read until end tag is found
     * @param factory   provides the objects to resolve nested tags with
     */
    public IfTag(Matcher start, Iterator<Token> remaining, ResolverFactory factory) {
        super(start, remaining, factory, "ELSE", "/IF");
        this.start = start;
    }

    @Override
    public void resolve(Token startTag, AccessibleData data, Writer output) throws IOException {
        try {
            if (conditionSatisfied(data)) {
                resolveContent(content, data, output);
            } else {
                resolveContent(otherContent, data, output);
            }
        } catch (ResolverException e) {
            e.addLocation(startTag);
            throw e;
        }
        data.undefine(VALUE_KEY);
    }


    private boolean conditionSatisfied(AccessibleData data) {
        String leftSide = start.group(1).trim();
        Object left = data.get(leftSide);

        if (start.group(2) == null) {
            if (isTruthy(left)) {
                data.define(VALUE_KEY, left);
                return true;
            }
            return false;
        }

        String operator = start.group(3);
        String rightSide = start.group(4).trim();

        Object right = data.get(rightSide);

        return switch (operator) {
            case "==" -> Objects.equals(left, right);
            case "!=" -> !Objects.equals(left, right);
            case "<" -> AccessibleData.compare(left, right, true) < 0;
            case ">" -> AccessibleData.compare(left, right, true) > 0;
            default -> throw new IllegalArgumentException("Unsupported operator " + operator);
        };
    }

    private static boolean isTruthy(Object left) {
        if (left instanceof String ls) {
            return !ls.isEmpty();
        }
        if (left instanceof Number n) {
            return n.floatValue() != 0;
        }
        return left != null && !Boolean.FALSE.equals(left);
    }

}
