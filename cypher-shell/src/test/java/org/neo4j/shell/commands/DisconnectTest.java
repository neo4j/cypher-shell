package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Shell;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.exception.CommandException;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class DisconnectTest {

    private Shell shell;
    private Disconnect cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Disconnect(shell);
    }

    @Test
    public void shouldNotAcceptArgs() {
        try {
            cmd.execute("bob");
            fail("Should not accept args");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }
}
