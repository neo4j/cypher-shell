package org.neo4j.shell.parser;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.neo4j.shell.exception.CypherSyntaxError;
import org.neo4j.shell.exception.IncompleteCypherError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A cypher aware parser which can detect shell commands (:prefixed) or cypher.
 */
public class ShellStatementParser implements StatementParser {

    private static final Pattern shellCmdPattern = Pattern.compile("^\\s*:.+\\s*$");

    public ShellStatementParser() {
    }

    /**
     * Parses a text for shell commands and cypher statements
     * @param text to parse
     * @return list of statements (shell commands, and cypher statements)
     */
    @Nonnull
    @Override
    public List<String> parse(@Nonnull String text) throws CypherSyntaxError {
        try {
            return CypherParserWrapper.parse(text);
        } catch (IncompleteCypherError e) {
            // Should be rethrown directly
            throw e;
        } catch (CypherSyntaxError e) {
            if (e.getCause() instanceof ParseCancellationException &&
                    e.getCause().getCause() instanceof RecognitionException) {
                RecognitionException ex = (RecognitionException) e.getCause().getCause();
                Token token = ex.getOffendingToken();
                LineSplitResult splitText = splitOnLine(text, token.getLine() - 1);

                // See if offending line was a shell command
                if (shellCmdPattern.matcher(splitText.getLine()).matches()) {
                    final List<String> statements = new ArrayList<>();

                    if (splitText.getBefore() != null) {
                        statements.addAll(parse(splitText.getBefore()));
                    }

                    statements.add(splitText.getLine());

                    if (splitText.getAfter() != null) {
                        statements.addAll(parse(splitText.getAfter()));
                    }

                    return statements;
                }
            }

            throw e;
        }
    }


    /**
     * Split a piece of text on a particular line. Returns a result such that the following is true (for non-null pieces)
     *
     * original text = String.join("\n", before, line, after);
     *
     * @param text to split
     * @param linenumber to split on
     * @return LineSplitResult
     */
    static LineSplitResult splitOnLine(@Nonnull String text, final int linenumber) {
        String[] splitText = text.split("\n");

        if (splitText.length <= linenumber || linenumber < 0) {
            throw new IllegalArgumentException("Linenumber must be 0 <= linenumber < linecount");
        }

        final String before;
        if (linenumber == 0) {
            before = null;
        } else {
            before = String.join("\n", Arrays.stream(splitText).limit(linenumber).collect(Collectors.toList()));
        }
        String after;

        if (linenumber == splitText.length - 1) {
            after = null;
        } else {
            after = String.join("\n", Arrays.stream(splitText).skip(linenumber + 1).collect(Collectors.toList()));
        }

        return new LineSplitResult(before, splitText[linenumber], after);
    }

    static class LineSplitResult {
        private final String before;
        private final String after;
        private final String line;

        public LineSplitResult(@Nullable String before, @Nonnull String line, @Nullable String after) {
            this.before = before;
            this.after = after;
            this.line = line;
        }

        @Nullable
        public String getBefore() {
            return before;
        }

        @Nullable
        public String getAfter() {
            return after;
        }

        @Nonnull
        public String getLine() {
            return line;
        }

        @Nonnull
        public String getOriginalText() {
            if (before != null && after != null) {
                return String.join("\n", before, line, after);
            } else if (before != null) {
                return String.join("\n", before, line);
            } else if (after != null) {
                return String.join("\n", line, after);
            } else {
                return line;
            }
        }
    }
}
