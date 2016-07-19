package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.exception.CommandException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.containsString;

public class HelpTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    private Shell shell = new TestShell();
    private Command cmd;

    @Before
    public void setup() {
        this.cmd = new Help(shell);
    }

    @Test
    public void shouldAcceptNoArgs() throws CommandException {
        cmd.execute("");
        // Should not throw
    }

    @Test
    public void shouldNotAcceptTooManyArgs() throws CommandException {
        thrown.expect(CommandException.class);
        thrown.expectMessage(containsString("Incorrect number of arguments"));

        cmd.execute("bob alice");
        fail("Should not accept too many args");
    }
}
