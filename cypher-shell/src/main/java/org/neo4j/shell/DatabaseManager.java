package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;

/**
 * An object capable of tracking the active database.
 */
public interface DatabaseManager
{
    String ABSENT_DB_NAME = "";
    String SYSTEM_DB_NAME = "system";
    String DEFAULT_DEFAULT_DB_NAME = "neo4j";

    void setActiveDatabase(String databaseName) throws CommandException;

    String getActiveDatabaseAsSetByUser();

    String getActualDatabaseAsReportedByServer();
}
