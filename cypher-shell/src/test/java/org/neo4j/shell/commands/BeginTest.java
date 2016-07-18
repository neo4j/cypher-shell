package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
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
        thrown.expectMessage("Too many arguments. @|bold :begin|@ does not accept any arguments");

        beginCommand.execute(Arrays.asList("bob"));
        fail("should not accept args");
    }

    @Test
    public void needsToBeConnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        when(mockShell.isConnected()).thenReturn(false);

        beginCommand.execute(new ArrayList<>());
        fail("shell is disconnected");
    }

    @Test
    public void beginTransactionOnShell() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);

        beginCommand.execute(new ArrayList<>());

        verify(mockShell).beginTransaction();
    }

    @Test
    public void nestedTransactionsAreNotSupported() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);
        CommandException expectedException = new CommandException("transaction already open");
        doThrow(expectedException).when(mockShell).beginTransaction();

        try {
            beginCommand.execute(new ArrayList<>());
            fail("Should throw");
        } catch (CommandException actual) {
            assertEquals(expectedException, actual);
        }
    }
}
