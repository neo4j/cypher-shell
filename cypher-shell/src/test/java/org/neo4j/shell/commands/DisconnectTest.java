package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.TestShell;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class DisconnectTest {

    private TestShell shell;
    private Disconnect cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Disconnect(shell);
    }

    @Test
    public void shouldNotAcceptArgs() {
        try {
            cmd.execute(Arrays.asList("bob"));
            fail("Should not accept args");
        } catch (CommandException e) {
            assertTrue("Unexepcted error", e.getMessage().startsWith("Too many arguments"));
        }
    }
}
