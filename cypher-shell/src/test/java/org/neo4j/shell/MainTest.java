package org.neo4j.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.exceptions.ClientException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
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

    @Before
    public void setup() {
        out = mock(PrintStream.class);
        shell = mock(CypherShell.class);
        connectionConfig = mock(ConnectionConfig.class);

        doReturn("").when(connectionConfig).username();
        doReturn("").when(connectionConfig).password();
    }

    @Test
    public void connectInteractivelyNonEndedStringFails() throws Exception {
        String inputString = "no newline";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        doThrow(new ClientException("Missing username and password")).when(shell).connect(connectionConfig);

        thrown.expectMessage("No text could be read, exiting");

        Main main = new Main(inputStream, out);
        main.connectInteractively(shell, connectionConfig);
        verify(shell, times(1)).connect(connectionConfig);
    }

    @Test
    public void connectInteractivelyNullMessageDoesNotPrompt() throws Exception {
        doThrow(new ClientException(null)).when(shell).connect(connectionConfig);

        thrown.expect(ClientException.class);

        Main main = new Main(mock(InputStream.class), out);
        main.connectInteractively(shell, connectionConfig);
        verify(shell, times(1)).connect(connectionConfig);
    }

    @Test
    public void connectInteractivelyUnrelatedErrorDoesNotPrompt() throws Exception {
        doThrow(new RuntimeException("bla")).when(shell).connect(connectionConfig);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("bla");

        Main main = new Main(mock(InputStream.class), out);
        main.connectInteractively(shell, connectionConfig);
        verify(shell, times(1)).connect(connectionConfig);
    }

    @Test
    public void connectInteractivelyPromptsForBothIfNone() throws Exception {
        doThrow(new ClientException("Missing username and password")).doNothing().when(shell).connect(connectionConfig);

        String inputString = "bob\nsecret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectInteractively(shell, connectionConfig);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(out, "username: bob\r\npassword: ******\r\n");
        verify(connectionConfig).setUsername("bob");
        verify(connectionConfig).setPassword("secret");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void connectInteractivelyPromptsForUserIfPassExists() throws Exception {
        doThrow(new ClientException("Missing username and password")).doNothing().when(shell).connect(connectionConfig);
        doReturn("secret").when(connectionConfig).password();

        String inputString = "bob\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectInteractively(shell, connectionConfig);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(out, "username: bob\r\n");
        verify(connectionConfig).setUsername("bob");
        verify(shell, times(2)).connect(connectionConfig);
    }

    @Test
    public void connectInteractivelyPromptsForPassIfUserExists() throws Exception {
        doThrow(new ClientException("Missing username and password")).doNothing().when(shell).connect(connectionConfig);
        doReturn("bob").when(connectionConfig).username();

        String inputString = "secret\n";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        Main main = new Main(inputStream, ps);
        main.connectInteractively(shell, connectionConfig);

        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(out, "password: ******\r\n");
        verify(connectionConfig).setPassword("secret");
        verify(shell, times(2)).connect(connectionConfig);
    }
}
