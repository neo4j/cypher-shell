package org.neo4j.shell;

/**
 * And exception indicating that a command invocation failed.
 */
public class CommandException extends Exception {
    public CommandException(String msg) {
        super(msg);
    }
}
