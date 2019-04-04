package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;

/**
 * An object capable of tracking the active database.
 */
public interface DatabaseManager
{
    String DEFAULT_DEFAULT_DB_NAME = "neo4j";
    String SYSTEM_DB_NAME = "system";

    void setActiveDatabase(String databaseName) throws CommandException;

    String getActiveDatabase();
}
