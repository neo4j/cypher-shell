package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;

import javax.annotation.Nonnull;

/**
 * An object with the ability to connect and disconnect.
 */
public interface Connector {

    /**
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     *
     * @throws CommandException if connection failed
     */
    void connect(@Nonnull ConnectionConfig connectionConfig) throws CommandException;

    /**
     *
     * @throws CommandException if disconnection failed
     */
    void disconnect() throws CommandException;
}
