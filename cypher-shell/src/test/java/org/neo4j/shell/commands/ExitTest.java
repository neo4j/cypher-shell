package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ExitTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Shell shell = mock(Shell.class);
    private Exit cmd;

    @Before
    public void setup() {
        this.cmd = new Exit(shell);
    }

    @Test
    public void shouldNotAcceptArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob");
        fail("Should not accept args");
    }

    @Test
    public void shouldExitShell() throws CommandException {
        thrown.expect(ExitException.class);

        cmd.execute("");

        verify(shell).printOut("Exiting. Bye bye.");
    }
}
