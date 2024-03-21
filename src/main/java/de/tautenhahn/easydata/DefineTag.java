package de.tautenhahn.easydata;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Defines a macro for further use, specifying name, parameter list and content. This tag itself is resolved
 * to an empty String.
 * 
 * @author TT
 */
public class DefineTag extends ComplexTag
{

  static final Pattern PATTERN = Pattern.compile("DEFINE +([^\\(\\{ ]+)( *[\\(\\{]([^,]+(,[^,]+)*)[\\)\\}])?");

  DefineTag(Matcher startMatcher, Iterator<Token> remaining, ResolverFactory factory)
  {
    super(startMatcher, remaining, factory, "COMMENT", "/DEFINE");

    // Registering the new tag is done immediately so the new tag is available before some surrounding tag will be resolved.
    registerNewTag(startMatcher, factory);
  }

  private void registerNewTag(Matcher startMatcher, ResolverFactory factory) {
    String name = startMatcher.group(1);
    List<String> paramNames = Optional.ofNullable(startMatcher.group(3))
            .map(a -> Arrays.stream(a.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList()))
            .orElseGet(Collections::emptyList);
    MacroTag macro = new MacroTag(name, paramNames, content);
    factory.register(macro.getPattern(), macro);
  }

  @Override
  public void resolve(Token start, AccessibleData data, Writer output)
  {
    // empty on purpose
  }

}
