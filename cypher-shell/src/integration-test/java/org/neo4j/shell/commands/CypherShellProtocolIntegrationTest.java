package org.neo4j.shell.commands;

import org.junit.Test;

import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.ShellParameterMap;
import org.neo4j.shell.StringLinePrinter;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.prettyprint.PrettyConfig;
import static org.junit.Assert.assertTrue;

import static org.neo4j.shell.DatabaseManager.ABSENT_DB_NAME;

public class CypherShellProtocolIntegrationTest{

    @Test
    public void shouldConnectWithNeo4jProtocol() throws Exception {
        CypherShell shell = new CypherShell( new StringLinePrinter(), new PrettyConfig( Format.PLAIN, true, 1000), false, new ShellParameterMap());
        // This should work even on older databases without the neo4j protocol, by falling back to bolt
        shell.connect( new ConnectionConfig( "neo4j://", "localhost", 7687, "neo4j", "neo", false, ABSENT_DB_NAME ) );
        assertTrue(shell.isConnected());
    }
}
