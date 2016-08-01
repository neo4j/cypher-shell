package org.neo4j.shell.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.neo4j.shell.cypher.CypherShellLexer;
import org.neo4j.shell.cypher.CypherShellParser;
import org.neo4j.shell.exception.IncompleteStatementException;
import org.neo4j.shell.exception.UnconsumedStatementException;
import org.neo4j.shell.log.AnsiFormattedText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A cypher aware parser which can detect shell commands (:prefixed) or cypher.
 */
public class StatementParser {

    protected static final Pattern shellCmdPattern = Pattern.compile("^\\s*:.+\\s*$");
    private static CypherParserWrapper cypherParserWrapper;
    private State currentState = State.EMPTY;
    private StringBuilder statementBuilder;

    public StatementParser(@Nonnull WTFParser WTFParser) {
        statementBuilder = new StringBuilder();
        cypherParserWrapper = new CypherParserWrapper();
    }

    /**
     * Returns a context dependent prompt. A different prompt is returned depending on if the line is in the middle of
     * a multiline statement.
     * @return the prompt which should be displayed to the user
     */
    @Nonnull
    public AnsiFormattedText getPrompt() {
        if (State.INCOMPLETE == currentState) {
            return AnsiFormattedText.s().bold().append(".....> ");
        } else {
            return AnsiFormattedText.s().bold().append("neo4j> ");
        }
    }

    /**
     * Takes a single line of input and parses it.
     * @param line to parse
     * @throws UnconsumedStatementException in case a complete statement already has been parsed but not consumed yet
     */
    public void parseLine(@Nonnull String line) throws UnconsumedStatementException, IOException {
        if (State.COMPLETE == currentState) {
            throw new UnconsumedStatementException();
        } else if (State.EMPTY == currentState && shellCmdPattern.matcher(line).matches()) {
            statementBuilder.append(line);
            currentState = State.COMPLETE;
        } else {
            statementBuilder.append(line);

            CypherShellLexer lexer = new CypherShellLexer(new ANTLRInputStream(statementBuilder.toString()));

            CypherShellParser parser = new CypherShellParser(new CommonTokenStream(lexer));
            parser.setErrorHandler(new BailErrorStrategy());

            try {
                CypherShellParser.CypherScriptContext context = parser.cypherScript();
                // Completely valid cypher(s)
                System.out.println("Parser " + context.toString());
            } catch (ParseCancellationException e) {
                System.out.println("Exc: " + e);
                throw e;
            }

        }
    }

    /**
     * Returns true if the parser contains a complete statement ready for client to {@link #consumeStatement()}
     * @return true if the line(s) read so far make up a complete statement, false otherwise (also the case if no lines
     * have been parsed yet)
     */
    public boolean isStatementComplete() {
        return State.COMPLETE == currentState;
    }

    /**
     * Is only expected to be called once {@link #isStatementComplete()} returns true.
     * Will reset the parser to empty once it returns.
     * @return a complete statement
     * @throws IncompleteStatementException in case a complete statement has not been parsed yet
     */
    @Nonnull
    public String consumeStatement() throws IncompleteStatementException {
        switch (currentState) {
            case COMPLETE:
                String statement = statementBuilder.toString();
                reset();
                return statement;
            case INCOMPLETE:
            case EMPTY:
            default:
                throw new IncompleteStatementException();
        }
    }

    /**
     * Resets the parser to empty state.
     */
    public void reset() {
        currentState = State.EMPTY;
        statementBuilder = new StringBuilder();
    }

    /**
     * Parses a text for shell commands and cypher statements
     * @param text to parse
     * @return list of statements (shell commands, and cypher statements)
     */
    @Nonnull
    public static List<String> parse(@Nonnull String text) throws CypherParserWrapper.CypherSyntaxError {
        try {
            return cypherParserWrapper.parse(text);
        } catch (CypherParserWrapper.CypherSyntaxError e) {
            System.out.println(e);

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

    /**
     * Enum of internal states.
     */
    enum State {
        // No lines have been parsed so far
        EMPTY,
        // Some lines have been parsed, but they do not make up a complete statement
        INCOMPLETE,
        // A complete statement has been parsed and is ready to be consumed
        COMPLETE
    }

    public static class LineSplitResult {
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
