package org.neo4j.shell.commands;

import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.exception.CommandException;

import static org.neo4j.driver.internal.messaging.request.MultiDatabaseUtil.ABSENT_DB_NAME;

abstract class CypherShellIntegrationTest
{
    CypherShell shell;

    void connect(String password) throws CommandException {
        // Try with encryption off first, which is the default for 4.X
        try
        {
            shell.connect( new ConnectionConfig( "bolt://", "localhost", 7687, "neo4j", password, false, ABSENT_DB_NAME ) );
        }
        catch ( ServiceUnavailableException e )
        {
            // This means we are probably in 3.X, let's retry with encryption on
            shell.connect( new ConnectionConfig( "bolt://", "localhost", 7687, "neo4j", password, true, ABSENT_DB_NAME ) );
        }
    }
}
