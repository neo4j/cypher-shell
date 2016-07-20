package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Connector;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DisconnectTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Disconnect cmd;
    private Logger logger = mock(Logger.class);
    private Connector connector = mock(Connector.class);

    @Before
    public void setup() {
        this.cmd = new Disconnect(logger, connector);
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob");
        fail("Should not accept args");
    }

    @Test
    public void shouldDisconnectShell() throws CommandException {
        cmd.execute("");

        verify(connector).disconnect();
    }
}
