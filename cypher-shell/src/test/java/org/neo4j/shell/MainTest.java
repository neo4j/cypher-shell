package org.neo4j.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.shell.cli.CliArgs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MainTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private CypherShell shell;
    private ConnectionConfig connectionConfig;
    private PrintStream out;
    private AuthenticationException authException;

    @Before
    public void setup() {
        out = mock(PrintStream.class);
        shell = mock(CypherShell.class);
        connectionConfig = mock(ConnectionConfig.class);

        doReturn("").when(connectionConfig).username();
        doReturn("").when(connectionConfig).password();

        // Don't mock because of gradle bug: https://github.com/gradle/gradle/issues/1618
        authException = new AuthenticationException(Main.NEO_CLIENT_ERROR_SECURITY_UNAUTHORIZED, "BOOM");
    }

    @Test
    public void nonEndedStringFails() throws Exception {
        String inputString = "no newline";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        doThrow(authException).when(shell).connect(connectionConfig);

        thrown.expectMessage("No text could be read, exiting");

        Main main = new Main(inputStream, out);
        main.connectMaybeInteractively(shell, connectionConfig, true, true);
        verify(shell, times(1)).connect(connectionConfig);
    }

    @Test
    public void unrelatedErrorDoesNotPrompt() throws Exception {
        doThrow(new RuntimeException("bla")).when(shell).connect(connectionConfig);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("bla");

        Main main = new Main(mock(InputStream.class), out);
        main.connectMaybeInteractively(shell, connectionConfig, true, true);
        verify(shell, times(1)).connect(connectionConfig);
    }

    @Test
    public void promptsForUsernameAndPasswordIfNoneGivenIfInteractive() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);

        String inputString = "bob\nsecret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, true);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(String.format( "username: bob%npassword: ******%n" ), out);
        verify(connectionConfig).setUsername("bob");
        verify(connectionConfig).setPassword("secret");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void promptsSilentlyForUsernameAndPasswordIfNoneGivenIfOutputRedirected() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);

        String inputString = "bob\nsecret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, false);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals("", out);
        verify(connectionConfig).setUsername("bob");
        verify(connectionConfig).setPassword("secret");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void doesNotPromptIfInputRedirected() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);

        String inputString = "bob\nsecret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);

        try {
            main.connectMaybeInteractively(shell, connectionConfig, false, true);
            fail("Expected auth exception");
        } catch (AuthenticationException e) {
            verify(shell, times(1)).connect(connectionConfig);
        }
    }

    @Test
    public void promptsForUserIfPassExistsIfInteractive() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);
        doReturn("secret").when(connectionConfig).password();

        String inputString = "bob\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, true);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(out, String.format( "username: bob%n" ));
        verify(connectionConfig).setUsername("bob");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void promptsSilentlyForUserIfPassExistsIfOutputRedirected() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);
        doReturn("secret").when(connectionConfig).password();

        String inputString = "bob\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, false);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(out, "");
        verify(connectionConfig).setUsername("bob");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void promptsForPassIfUserExistsIfInteractive() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);
        doReturn("bob").when(connectionConfig).username();

        String inputString = "secret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, true);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(out, String.format("password: ******%n"));
        verify(connectionConfig).setPassword("secret");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void promptsSielntlyForPassIfUserExistsIfOutputRedirected() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);
        doReturn("bob").when(connectionConfig).username();

        String inputString = "secret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, false);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(out, "");
        verify(connectionConfig).setPassword("secret");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void promptsHandlesBang() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);

        String inputString = "bo!b\nsec!ret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, true);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(String.format("username: bo!b%npassword: *******%n"), out);
        verify(connectionConfig).setUsername("bo!b");
        verify(connectionConfig).setPassword("sec!ret");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void triesOnlyOnceIfUserPassExists() throws Exception {
        doThrow(authException).doThrow(new RuntimeException("second try")).when(shell).connect(connectionConfig);
        doReturn("bob").when(connectionConfig).username();
        doReturn("secret").when(connectionConfig).password();

        InputStream inputStream = new ByteArrayInputStream("".getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);

        try {
            main.connectMaybeInteractively(shell, connectionConfig, true, true);
            fail("Expected an exception");
        } catch (Neo4jException e) {
            assertEquals(authException.code(), e.code());
            verify(shell, times(1)).connect(connectionConfig);
        }
    }

    @Test
    public void repromptsIfUserIsNotProvidedIfInteractive() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);

        String inputString = "\nbob\nsecret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, true);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(String.format( "username: %nusername cannot be empty%n%nusername: bob%npassword: ******%n"), out );
        verify(connectionConfig).setUsername("bob");
        verify(connectionConfig).setPassword("secret");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void doesNotRepromptsIfUserIsNotProvidedIfOutputRedirected() throws Exception {
        doThrow(authException).doNothing().when(shell).connect(connectionConfig);

        String inputString = "\nsecret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectMaybeInteractively(shell, connectionConfig, true, false);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals("", out );
        verify(connectionConfig).setUsername("");
        verify(connectionConfig).setPassword("secret");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void printsVersionAndExits() {
        CliArgs args = new CliArgs();
        args.setVersion(true);

        PrintStream printStream = mock(PrintStream.class);

        Main main = new Main(System.in, printStream);
        main.startShell(args);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(printStream).println(argument.capture());
        assertTrue(argument.getValue().matches("Cypher-Shell \\d+\\.\\d+\\.\\d+.*"));
    }

    @Test
    public void printsDriverVersionAndExits() {
        CliArgs args = new CliArgs();
        args.setDriverVersion(true);

        PrintStream printStream = mock(PrintStream.class);

        Main main = new Main(System.in, printStream);
        main.startShell(args);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(printStream).println(argument.capture());
        assertTrue(argument.getValue().matches("Neo4j Driver \\d+\\.\\d+\\.\\d+.*"));
    }
}
