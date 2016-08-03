package org.neo4j.shell.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.neo4j.shell.cypher.CypherShellLexer;
import org.neo4j.shell.cypher.CypherShellParser;
import org.neo4j.shell.exception.CypherSyntaxError;
import org.neo4j.shell.exception.IncompleteCypherError;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses Cypher statements from text
 */
public class CypherParserWrapper {

    private CypherParserWrapper() {}

    /**
     * Construct an ANTLR parser for the given cypher text.
     * @param text to parse
     * @return a parser ready to parse the given text
     */
    public static CypherShellParser getParser(@Nonnull String text) {
        CypherShellParser parser =
                new CypherShellParser(new CommonTokenStream(new CypherShellLexer(new ANTLRInputStream(text))));
        // Don't want to print errors to console
        parser.removeErrorListeners();
        // Throw exception on parsing error
        parser.setErrorHandler(new BailErrorStrategy());

        return parser;
    }

    /**
     * Parses a cypher text and returns the separate statements within.
     * @param text to parse
     * @return list of statements
     * @throws org.neo4j.shell.exception.CypherSyntaxError if the text has syntax problems. If the cypher statement
     * simply isn't finished, an {@link org.neo4j.shell.exception.IncompleteCypherError} will be thrown.
     */
    @Nonnull
    public static List<String> parse(@Nonnull String text) throws CypherSyntaxError {
        CypherShellParser parser = getParser(text);

        try {
            CypherShellParser.CypherScriptContext context = parser.cypherScript();
            return context.children.stream()
                                   // Last child is EOF, so skip that
                                   .limit(context.getChildCount() - 1)
                                   .map(ParseTree::getText).collect(Collectors.toList());
        } catch (ParseCancellationException e) {
            if (e.getCause() instanceof NoViableAltException) {
                NoViableAltException ex = (NoViableAltException) e.getCause();
                Token token = ex.getOffendingToken();
                if ("<EOF>".equals(token.getText())) {
                    // Incomplete
                    throw new IncompleteCypherError(e);
                }
                // Explain what failed
                throw new CypherSyntaxError(
                        String.format("Invalid input '%s' (line %d, position %d)",
                                token.getText(),
                                token.getLine(),
                                token.getCharPositionInLine()),
                        e);
            }
            throw new CypherSyntaxError(e);
        }
    }
}
