package org.neo4j.shell.commands;

import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.exception.CommandException;

abstract class CypherShellIntegrationTest
{
    CypherShell shell;

    void connect(String password) throws CommandException {
        // Try with encryption first, which is the default for 3.X
        try
        {
            shell.connect( new ConnectionConfig( "bolt://", "localhost", 7687, "neo4j", password, true ) );
        }
        catch ( ServiceUnavailableException e )
        {
            // This means we are probablyin 4.X, let's retry with encryption off
            shell.connect( new ConnectionConfig( "bolt://", "localhost", 7687, "neo4j", password, false ) );
        }
    }
}
