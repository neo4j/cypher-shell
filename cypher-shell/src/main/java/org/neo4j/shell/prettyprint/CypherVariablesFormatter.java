package org.neo4j.shell.prettyprint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public final class CypherVariablesFormatter {
    private static final String BACKTICK = "`";
    private static final Pattern ALPHA_NUMERIC = Pattern.compile("^[\\p{L}_][\\p{L}0-9_]*");

    private CypherVariablesFormatter()
    {
        throw new UnsupportedOperationException( "do not instantiate" );
    }

    @Nonnull
    public static String escape(@Nonnull String string) {
        Matcher alphaNumericMatcher = ALPHA_NUMERIC.matcher(string);
        if (!alphaNumericMatcher.matches()) {
            String reEscapeBackTicks = string.replaceAll(BACKTICK, BACKTICK + BACKTICK);
            return BACKTICK + reEscapeBackTicks + BACKTICK;
        }
        return string;
    }

    @Nonnull
    public static String unescapedCypherVariable(@Nonnull String string) {
        Matcher alphaNumericMatcher = ALPHA_NUMERIC.matcher(string);
        if (!alphaNumericMatcher.matches()) {
            String substring = string.substring(1, string.length() - 1);
            return substring.replace(BACKTICK + BACKTICK, BACKTICK);
        } else {
            return string;
        }
    }
}
