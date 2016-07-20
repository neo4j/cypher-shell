package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;

import java.util.Optional;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HistoryTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Shell shell = mock(Shell.class);
    private Command cmd;

    @Before
    public void setup() {
        this.cmd = new History(shell);
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob");
        fail("Should not accept args");
    }

    @Test
    public void shouldPrintHistory() throws CommandException {
        when(shell.getHistory()).thenReturn(Optional.ofNullable(mock(jline.console.history.History.class)));

        cmd.execute("");

        verify(shell).printOut(anyString());
    }
}
