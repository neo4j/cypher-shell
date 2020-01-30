package org.neo4j.shell;

import javax.annotation.Nonnull;

import org.neo4j.function.ThrowingAction;
import org.neo4j.shell.exception.CommandException;

/**
 * An object with the ability to connect and disconnect.
 */
public interface Connector {

    /**
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     *
     * @throws CommandException if connection failed
     */
    default void connect(@Nonnull ConnectionConfig connectionConfig) throws CommandException {
        connect( connectionConfig, null );
    }

    /**
     *
     * @throws CommandException if connection failed
     */
    void connect( @Nonnull ConnectionConfig connectionConfig, ThrowingAction<CommandException> action) throws CommandException;

    /**
     * Returns the version of Neo4j which the shell is connected to. If the version is before 3.1.0-M09, or we are not
     * connected yet, this returns the empty string.
     *
     * @return the version of neo4j (like '3.1.0') if connected and available, an empty string otherwise
     */
    @Nonnull
    String getServerVersion();
}
