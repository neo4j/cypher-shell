package org.neo4j.shell.commands;

import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.cli.Encryption;
import org.neo4j.shell.exception.CommandException;

import static org.neo4j.shell.DatabaseManager.ABSENT_DB_NAME;

abstract class CypherShellIntegrationTest
{
    CypherShell shell;

    void connect(String password) throws CommandException {
        shell.connect( new ConnectionConfig( "bolt://", "localhost", 7687, "neo4j", password, Encryption.DEFAULT, ABSENT_DB_NAME ) );
    }
}
