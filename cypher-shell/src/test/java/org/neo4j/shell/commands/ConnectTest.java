package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.exception.CommandException;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class ConnectTest {

    private TestShell shell;
    private Command cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Connect(shell);
    }

    @Test
    public void shouldNotAcceptTooManyArgs() {
        try {
            cmd.execute(Arrays.asList("bob", "alice"));
            fail("Should not accept too many args");
        } catch (CommandException e) {
            assertTrue("Unexepcted error", e.getMessage().startsWith("Too many arguments"));
        }
    }
}
