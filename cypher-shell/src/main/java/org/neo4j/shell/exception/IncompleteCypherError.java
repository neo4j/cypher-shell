package org.neo4j.shell.exception;

import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * An incomplete cypher statement was encountered
 */
public class IncompleteCypherError extends CypherSyntaxError {

    public IncompleteCypherError(ParseCancellationException cause) {
        super(cause);
    }
}
