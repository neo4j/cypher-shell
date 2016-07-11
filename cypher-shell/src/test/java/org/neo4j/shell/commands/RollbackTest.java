package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RollbackTest {

    private Command rollbackCommand;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private Shell mockShell = mock(Shell.class);


    @Before
    public void setup() {
        this.rollbackCommand = new Rollback(mockShell);
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        rollbackCommand.execute("bob");
    }

    @Test
    public void needsToBeConnected() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage("Not connected to Neo4j");

        when(mockShell.isConnected()).thenReturn(false);

        rollbackCommand.execute("");
    }

    @Test
    public void rollbackTransaction() throws CommandException {
        when(mockShell.isConnected()).thenReturn(true);

        rollbackCommand.execute("");

        verify(mockShell).rollbackTransaction();
    }
}
