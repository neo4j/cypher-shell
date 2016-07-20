package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;

/**
 * An object capable of starting, committing, and rolling back transactions.
 */
public interface TransactionHandler {

    /**
     *
     * @throws CommandException if a new transaction could not be started
     */
    void beginTransaction() throws CommandException;

    /**
     *
     * @throws CommandException if current transaction could not be committed
     */
    void commitTransaction() throws CommandException;

    /**
     *
     * @throws CommandException if current transaction could not be rolled back
     */
    void rollbackTransaction() throws CommandException;
}
