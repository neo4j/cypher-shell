package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;

public class BeginTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private Command beginCommand;
    private Shell mockShell = mock(Shell.class);

    @Before
    public void setup() {
        this.beginCommand = new Begin(mockShell);
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        beginCommand.execute("bob");
    }

    @Test
    public void needsToBeConnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        when(mockShell.isConnected()).thenReturn(false);

        beginCommand.execute("");
    }

    @Test
    public void beginTransactionOnShell() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);

        beginCommand.execute("");

        verify(mockShell).beginTransaction();
    }
}
