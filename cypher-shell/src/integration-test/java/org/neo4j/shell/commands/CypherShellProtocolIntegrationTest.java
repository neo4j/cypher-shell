package org.neo4j.shell.commands;

import org.junit.Test;

import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.ShellParameterMap;
import org.neo4j.shell.StringLinePrinter;
import org.neo4j.shell.cli.Encryption;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.prettyprint.PrettyConfig;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.neo4j.shell.DatabaseManager.ABSENT_DB_NAME;
import static org.neo4j.shell.util.Versions.majorVersion;

public class CypherShellProtocolIntegrationTest{

    @Test
    public void shouldConnectWithBoltProtocol() throws Exception {
        CypherShell shell = new CypherShell( new StringLinePrinter(), new PrettyConfig( Format.PLAIN, true, 1000), false, new ShellParameterMap());
        shell.connect( new ConnectionConfig( "bolt://", "localhost", 7687, "neo4j", "neo", Encryption.DEFAULT, ABSENT_DB_NAME ) );
        assertTrue(shell.isConnected());
    }

    @Test
    public void shouldConnectWithNeo4jProtocol() throws Exception {
        CypherShell shell = new CypherShell( new StringLinePrinter(), new PrettyConfig( Format.PLAIN, true, 1000), false, new ShellParameterMap());
        // This should work even on older databases without the neo4j protocol, by falling back to bolt
        shell.connect( new ConnectionConfig( "neo4j://", "localhost", 7687, "neo4j", "neo", Encryption.DEFAULT, ABSENT_DB_NAME ) );
        assertTrue(shell.isConnected());
    }

    @Test
    public void shouldConnectWithBoltSSCProtocol() throws Exception {
        CypherShell shell = new CypherShell( new StringLinePrinter(), new PrettyConfig( Format.PLAIN, true, 1000), false, new ShellParameterMap());
        // Given 3.X series, where SSC are the default. Hard to test in 4.0 sadly.
        onlyIn3x(shell);
        shell.connect( new ConnectionConfig( "bolt+ssc://", "localhost", 7687, "neo4j", "neo", Encryption.DEFAULT, ABSENT_DB_NAME ) );
        assertTrue(shell.isConnected());
    }

    @Test
    public void shouldConnectWithNeo4jSSCProtocol() throws Exception {
        CypherShell shell = new CypherShell( new StringLinePrinter(), new PrettyConfig( Format.PLAIN, true, 1000), false, new ShellParameterMap());
        // Given 3.X series, where SSC are the default. Hard to test in 4.0 sadly.
        onlyIn3x(shell);
        // This should work by falling back to bolt+ssc
        shell.connect( new ConnectionConfig( "neo4j+ssc://", "localhost", 7687, "neo4j", "neo", Encryption.DEFAULT, ABSENT_DB_NAME ) );
        assertTrue(shell.isConnected());
    }

    // Here should be tests for "neo4j+s" and "bolt+s", but we don't have the infrastructure for those.

    private void onlyIn3x(CypherShell shell) throws Exception {
        // Default connection settings
        shell.connect( new ConnectionConfig( "bolt://", "localhost", 7687, "neo4j", "neo", Encryption.DEFAULT, ABSENT_DB_NAME ) );
        assumeTrue( majorVersion( shell.getServerVersion() ) < 4 );
        shell.disconnect();
    }
}
