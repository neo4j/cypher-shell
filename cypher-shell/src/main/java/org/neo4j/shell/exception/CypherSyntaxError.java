package org.neo4j.shell.exception;

import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * A cypher syntax error
 */
public class CypherSyntaxError extends Exception {
    public CypherSyntaxError(ParseCancellationException cause) {
        super(cause);
    }
}
