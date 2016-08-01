package org.neo4j.shell.parser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.neo4j.shell.cypher.CypherShellBaseListener;
import org.neo4j.shell.cypher.CypherShellLexer;
import org.neo4j.shell.cypher.CypherShellParser;

import javax.annotation.Nonnull;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses Cypher statements from text
 */
public class CypherParserWrapper {

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

        parser.addParseListener(new CypherShellBaseListener() {
            @Override
            public void enterCypherScript(CypherShellParser.CypherScriptContext ctx) {
                System.out.println("enterCypherScript() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void exitCypherScript(CypherShellParser.CypherScriptContext ctx) {
                System.out.println("exitCypherScript() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterCypher(CypherShellParser.CypherContext ctx) {
                System.out.println("enterCypher() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void exitCypher(CypherShellParser.CypherContext ctx) {
                System.out.println("exitCypher() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterQuery(CypherShellParser.QueryContext ctx) {
                System.out.println("enterQuery() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void exitQuery(CypherShellParser.QueryContext ctx) {
                System.out.println("exitQuery() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterStatement(CypherShellParser.StatementContext ctx) {
                System.out.println("enterStatement() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void exitStatement(CypherShellParser.StatementContext ctx) {
                System.out.println("exitStatement() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterCommand(CypherShellParser.CommandContext ctx) {
                System.out.println("enterCommand() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void exitCommand(CypherShellParser.CommandContext ctx) {
                System.out.println("exitCommand() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterRegularQuery(CypherShellParser.RegularQueryContext ctx) {
                System.out.println("enterRegularQuery() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterBulkImportQuery(CypherShellParser.BulkImportQueryContext ctx) {
                System.out.println("enterBulkImportQuery() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterSingleQuery(CypherShellParser.SingleQueryContext ctx) {
                System.out.println("enterSingleQuery() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterClause(CypherShellParser.ClauseContext ctx) {
                System.out.println("enterClause() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterReturnClause(CypherShellParser.ReturnClauseContext ctx) {
                System.out.println("enterReturnClause() called with: " +  "ctx = [" + ctx + "]");
            }

            @Override
            public void enterCreate(CypherShellParser.CreateContext ctx) {
                System.out.println("enterCreate() called with: " +  "ctx = [" + ctx + "]");
            }
        });

        parser.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                System.out.println("syntaxError() called with: " +  "recognizer = [" + recognizer + "], offendingSymbol = [" + offendingSymbol + "], line = [" + line + "], charPositionInLine = [" + charPositionInLine + "], msg = [" + msg + "], e = [" + e + "]");
            }

            @Override
            public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {

            }

            @Override
            public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {

            }

            @Override
            public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {

            }
        });

        return parser;
    }

    /**
     * Parses a cypher text and returns the separate statements within.
     * @param text to parse
     * @return list of statements
     * @throws CypherSyntaxError if the text has syntax problems. If the cypher statement simply isn't finished,
     * an {@link IncompleteCypherError} will be thrown.
     */
    @Nonnull
    public List<String> parse(@Nonnull String text) throws CypherSyntaxError {
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
                if ("<EOF>".equals(ex.getOffendingToken().getText())) {
                    // Incomplete
                    throw new IncompleteCypherError(e);
                }
            }
            throw new CypherSyntaxError(e);
        }
    }

    public static class IncompleteCypherError extends CypherSyntaxError {

        public IncompleteCypherError(ParseCancellationException cause) {
            super(cause);
        }
    }

    public static class CypherSyntaxError extends Exception {
        public CypherSyntaxError(ParseCancellationException cause) {
            super(cause);
        }
    }
}
