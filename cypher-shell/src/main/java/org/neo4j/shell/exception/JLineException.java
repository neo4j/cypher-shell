package org.neo4j.shell.exception;

import javax.annotation.Nullable;

/**
 * A generic exception supposed to be thrown in JLine when {@link org.neo4j.shell.cli.ErrorPassingPrintStream} is
 * invoked.
 */
public class JLineException extends RuntimeException {
    public JLineException() {
        super("Unable to parse input");
    }

    public JLineException(@Nullable String message) {
        super(message);
    }
}
