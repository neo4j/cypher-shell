package org.neo4j.shell.parser;

import org.neo4j.shell.exception.UnconsumedStatementException;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for Cypher.
 */
public class CypherParser {
    //Pattern matches a back slash at the end of the line for multiline commands
    private static final Pattern MULTILINE_BREAK = Pattern.compile("\\\\\\s*$");
    //Pattern matches comments
    private static final Pattern COMMENTS = Pattern.compile("//.*$");

    /**
     * Parse the given line, appending it to the statementBuilder as appropriate, depending on the state.
     * @param line to parse
     * @param statementBuilder to append correct lines to
     * @param currentState of the parser
     * @return the next state of the parser
     * @throws UnconsumedStatementException if current state is COMPLETE
     */
    @Nonnull
    public StatementParser.State parseLine(@Nonnull String line,
                                           @Nonnull StringBuilder statementBuilder,
                                           @Nonnull StatementParser.State currentState) {
        if (StatementParser.State.COMPLETE == currentState) {
            throw new UnconsumedStatementException();
        }

        String withoutComments = commentSubstitutedLine(line);
        Matcher m = MULTILINE_BREAK.matcher(withoutComments);
        boolean isMultiline = m.find();
        String parsedString = m.replaceAll("");

        if (!parsedString.trim().isEmpty()) {
            statementBuilder.append(parsedString).append("\n");
        }

        if (!isMultiline && statementBuilder.length() > 0) {
            return StatementParser.State.COMPLETE;
        } else if (isMultiline){
            return StatementParser.State.INCOMPLETE;
        } else {
            return currentState;
        }
    }

    private static String commentSubstitutedLine(String line) {
        Matcher commentsMatcher = COMMENTS.matcher(line);
        return commentsMatcher.replaceAll("");
    }
}
