package org.neo4j.shell.parser;

import org.neo4j.shell.exception.IncompleteStatementException;
import org.neo4j.shell.exception.UnconsumedStatementException;
import org.neo4j.shell.log.AnsiFormattedText;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * A cypher aware parser which can detect shell commands (:prefixed) or cypher.
 */
public class StatementParser {

    protected static final Pattern shellCmdPattern = Pattern.compile("^\\s*:.+\\s*$");
    private State currentState = State.EMPTY;
    private StringBuilder statementBuilder;
    private final CypherParser cypherParser;

    public StatementParser(@Nonnull CypherParser cypherParser) {
        statementBuilder = new StringBuilder();
        this.cypherParser = cypherParser;
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
    public void parseLine(@Nonnull String line) throws UnconsumedStatementException {
        if (State.COMPLETE == currentState) {
            throw new UnconsumedStatementException();
        } else if (State.EMPTY == currentState && shellCmdPattern.matcher(line).matches()) {
            statementBuilder.append(line);
            currentState = State.COMPLETE;
        } else {
            currentState = cypherParser.parseLine(line, statementBuilder, currentState);
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
    private void reset() {
        currentState = State.EMPTY;
        statementBuilder = new StringBuilder();
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

}
