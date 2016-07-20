package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.TransactionHandler;
import org.neo4j.shell.exception.CommandException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BeginTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private Command beginCommand;
    private TransactionHandler mockShell = mock(TransactionHandler.class);

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
    public void beginTransactionOnShell() throws CommandException {
        beginCommand.execute("");

        verify(mockShell).beginTransaction();
    }
}
