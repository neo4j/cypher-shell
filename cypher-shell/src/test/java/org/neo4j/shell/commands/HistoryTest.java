package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Historian;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.util.Arrays;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;

public class HistoryTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Logger logger = mock(Logger.class);
    private Historian historian = mock(Historian.class);
    private Command cmd;

    @Before
    public void setup() {
        this.cmd = new History(logger, historian);
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob");
        fail("Should not accept args");
    }

    @Test
    public void shouldPrintHistoryCorrectlyNumberedFrom1() throws CommandException {
        when(historian.getHistory()).thenReturn(Arrays.asList(":help", ":exit"));

        cmd.execute("");

        verify(logger).printOut(eq(" 1  :help\n" +
                " 2  :exit\n"));
    }
}
