package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;

/**
 * An object capable of tracking the active database.
 */
public interface DatabaseManager
{
    void setActiveDatabase(String databaseName) throws CommandException;

    String getActiveDatabase();
}
