package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.exception.CommandException;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class ConnectTest {

    private Shell shell;
    private Command cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Connect(shell);
    }

    @Test
    public void shouldNotAcceptTooManyArgs() {
        try {
            cmd.execute("bob alice");
            fail("Should not accept too many args");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }
}
