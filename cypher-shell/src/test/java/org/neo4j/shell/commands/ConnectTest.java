package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConnectTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Shell shell = mock(Shell.class);
    private Command connectCommand;

    @Before
    public void setup() {
        this.connectCommand = new Connect(shell);
    }

    @Test
    public void shouldNotAcceptTooManyArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        connectCommand.execute("bob alice");
        fail("Should not accept too many args");
    }

    @Test
    public void shouldConnectShell() throws CommandException {
        connectCommand.execute("bolt://localhost");

        verify(shell).connect(any(ConnectionConfig.class));
    }

    @Test
    public void shouldOnlyAcceptBoltProtocol() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Unsupported protocol"));
        connectCommand.execute("http://localhost");
    }
}
