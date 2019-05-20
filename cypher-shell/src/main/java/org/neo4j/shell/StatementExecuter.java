package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;

/**
 * An interface which executes statements
 */
public interface StatementExecuter {

    /**
     * Execute a statement
     * @param statement to execute
     * @throws ExitException if a command to exit was executed
     * @throws CommandException if something went wrong
     */
    void execute(@Nonnull String statement) throws ExitException, CommandException;

    /**
     * Stops any running statements
     */
    void reset();

    /**
     * Get the error code from the last executed Cypher statement, or null if the last execution was successful.
     */
    String lastNeo4jErrorCode();
}
