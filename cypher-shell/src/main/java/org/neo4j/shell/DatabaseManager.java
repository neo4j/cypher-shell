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

    String DATABASE_UNAVAILABLE_ERROR_CODE = "Neo.TransientError.General.DatabaseUnavailable";

    /**
     * Sets the active database name as set by the user.
     * If the current state is connected, try to reconnect to that database.
     * If the current state is disconnected, simply update `activeDatabaseAsSetByUser`.
     */
    void setActiveDatabase(String databaseName) throws CommandException;

    String getActiveDatabaseAsSetByUser();

    String getActualDatabaseAsReportedByServer();
}
