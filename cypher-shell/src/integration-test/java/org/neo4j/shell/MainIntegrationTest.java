package org.neo4j.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.log.AnsiLogger;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.prettyprint.LinePrinter;
import org.neo4j.shell.prettyprint.PrettyConfig;
import org.neo4j.shell.prettyprint.ToStringLinePrinter;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class MainIntegrationTest
{

    private static class ShellAndConnection
    {
        CypherShell shell;
        ConnectionConfig connectionConfig;

        ShellAndConnection( CypherShell shell, ConnectionConfig connectionConfig )
        {
            this.shell = shell;
            this.connectionConfig = connectionConfig;
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private String inputString = String.format( "neo4j%nneo%n" );
    private ByteArrayOutputStream baos;
    private ConnectionConfig connectionConfig;
    private CypherShell shell;
    private Main main;

    @Before
    public void setup() {
        // given
        InputStream inputStream = new ByteArrayInputStream( inputString.getBytes() );

        baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream( baos );

        main = new Main( inputStream, ps );

        CliArgs cliArgs = new CliArgs();
        cliArgs.setUsername("", "");
        cliArgs.setPassword( "", "" );

        ShellAndConnection sac = getShell( cliArgs );
        shell = sac.shell;
        connectionConfig = sac.connectionConfig;
    }

    @Test
    public void promptsOnWrongAuthenticationIfInteractive() throws Exception {
        // when
        assertEquals("", connectionConfig.username());
        assertEquals("", connectionConfig.password());

        main.connectMaybeInteractively(shell, connectionConfig, true, true);

        // then
        // should be connected
        assertTrue(shell.isConnected());
        // should have prompted and set the username and password
        assertEquals("neo4j", connectionConfig.username());
        assertEquals("neo", connectionConfig.password());

        String out = baos.toString();
        assertEquals( format( "username: neo4j%npassword: ***%n" ), out );
    }

    @Test
    public void promptsSilentlyOnWrongAuthenticationIfOutputRedirected() throws Exception {
        // when
        assertEquals("", connectionConfig.username());
        assertEquals("", connectionConfig.password());

        main.connectMaybeInteractively(shell, connectionConfig, true, false);

        // then
        // should be connected
        assertTrue(shell.isConnected());
        // should have prompted silently and set the username and password
        assertEquals("neo4j", connectionConfig.username());
        assertEquals("neo", connectionConfig.password());

        String out = baos.toString();
        assertEquals( "", out );
    }

    @Test
    public void wrongPortWithBolt() throws Exception
    {
        // given
        CliArgs cliArgs = new CliArgs();
        cliArgs.setScheme( "bolt://", "" );
        cliArgs.setPort( 1234 );

        ShellAndConnection sac = getShell( cliArgs );
        CypherShell shell = sac.shell;
        ConnectionConfig connectionConfig = sac.connectionConfig;

        exception.expect( ServiceUnavailableException.class );
        exception.expectMessage( "Unable to connect to localhost:1234, ensure the database is running and that there is a working network connection to it" );
        main.connectMaybeInteractively( shell, connectionConfig, true, true );
    }

    @Test
    public void wrongPortWithNeo4j() throws Exception
    {
        // given
        CliArgs cliArgs = new CliArgs();
        cliArgs.setScheme( "neo4j://", "" );
        cliArgs.setPort( 1234 );

        ShellAndConnection sac = getShell( cliArgs );
        CypherShell shell = sac.shell;
        ConnectionConfig connectionConfig = sac.connectionConfig;

        exception.expect( ServiceUnavailableException.class );
        exception.expectMessage( "Unable to connect to database, ensure the database is running and that there is a working network connection to it" );
        main.connectMaybeInteractively( shell, connectionConfig, true, true );
    }

    @Test
    public void shouldReadCypherStatementsFromFile() throws Exception {
        // given
        CliArgs cliArgs = new CliArgs();
        cliArgs.setInputFilename( fileFromResource("test.cypher") );

        // when
        ToStringLinePrinter linePrinter = new ToStringLinePrinter();
        ShellAndConnection sac = getShell( cliArgs, linePrinter );
        CypherShell shell = sac.shell;
        ConnectionConfig connectionConfig = sac.connectionConfig;
        main.connectMaybeInteractively( shell, connectionConfig, true, true );
        ShellRunner shellRunner = ShellRunner.getShellRunner(cliArgs, shell, mock(Logger.class), connectionConfig);
        shellRunner.runUntilEnd();

        // then
        assertEquals( format("result%n42%n"), linePrinter.result() );
    }

    @Test
    public void shouldFailIfInputFileDoesntExist() throws Exception {
        // given
        CliArgs cliArgs = new CliArgs();
        cliArgs.setInputFilename( "what.cypher" );

        // when
        ShellAndConnection sac = getShell( cliArgs );
        CypherShell shell = sac.shell;
        main.connectMaybeInteractively( shell, sac.connectionConfig, true, true );

        // expect
        exception.expect( IOException.class);
        ShellRunner.getShellRunner(cliArgs, shell, mock(Logger.class), sac.connectionConfig );
    }

    private String fileFromResource(String filename)
    {
        return getClass().getClassLoader().getResource(filename).getFile();
    }

    private ShellAndConnection getShell( CliArgs cliArgs )
    {
        Logger logger = new AnsiLogger( cliArgs.getDebugMode() );
        return getShell( cliArgs, logger );
    }

    private ShellAndConnection getShell( CliArgs cliArgs, LinePrinter linePrinter )
    {
        PrettyConfig prettyConfig = new PrettyConfig( cliArgs );
        ConnectionConfig connectionConfig = new ConnectionConfig(
                cliArgs.getScheme(),
                cliArgs.getHost(),
                cliArgs.getPort(),
                cliArgs.getUsername(),
                cliArgs.getPassword(),
                cliArgs.getEncryption(),
                cliArgs.getDatabase() );

        return new ShellAndConnection( new CypherShell( linePrinter, prettyConfig, true, new ShellParameterMap() ), connectionConfig );
    }
}
