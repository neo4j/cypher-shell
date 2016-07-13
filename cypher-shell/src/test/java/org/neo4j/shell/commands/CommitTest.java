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

public class CommitTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private Command commitCommand;
    private Shell mockShell = mock(Shell.class);

    @Before
    public void setup() {
        this.commitCommand = new Commit(mockShell);
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Too many arguments. @|bold :commit|@ does not accept any arguments");

        commitCommand.execute(Arrays.asList("bob"));
        fail("should not accept args");
    }

    @Test
    public void throwExceptionWhenShellIsDisconnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        when(mockShell.isConnected()).thenReturn(false);

        commitCommand.execute(new ArrayList<>());
        fail("shell is disconnected");
    }

    @Test
    public void commitTransactionOnShell() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);

        commitCommand.execute(new ArrayList<>());

        verify(mockShell).commitTransaction();
    }

    @Test
    public void closingWhenNoTXOpenShouldThrow() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);
        CommandException expectedException = new CommandException("no open transaction");
        doThrow(expectedException).when(mockShell).commitTransaction();

        try {
            commitCommand.execute(new ArrayList<>());
            fail("Can't commit when no tx is open!");
        } catch (CommandException actual) {
            assertEquals(expectedException, actual);
        }
    }
}
