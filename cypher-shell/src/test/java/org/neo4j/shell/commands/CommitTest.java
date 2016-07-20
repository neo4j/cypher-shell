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


public class CommitTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private Command commitCommand;
    private TransactionHandler mockShell = mock(TransactionHandler.class);

    @Before
    public void setup() {
        this.commitCommand = new Commit(mockShell);
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        commitCommand.execute("bob");
    }

    @Test
    public void commitTransactionOnShell() throws CommandException {
        commitCommand.execute("");

        verify(mockShell).commitTransaction();
    }
}
