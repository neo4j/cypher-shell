package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.TestShell;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.*;

public class SetTest {

    private TestShell shell;
    private Command cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Set(shell);
    }

    @Test
    public void shouldFailIfNoArgs() {
        try {
            cmd.execute(new ArrayList<>());
            fail("Expected error");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }

    @Test
    public void shouldFailIfOneArg() {
        try {
            cmd.execute(Arrays.asList("bob"));
            fail("Expected error");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }

    @Test
    public void shouldFailIfTooManyArgs() {
        try {
            cmd.execute(Arrays.asList("bob", "mob", "zob"));
            fail("Expected error");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }

    @Test
    public void setValue() throws CommandException {
        shell.connect();
        cmd.execute(Arrays.asList("bob", "9"));
        assertEquals("Expected param to be set",
                "9", shell.getQueryParams().get("bob").toString());
    }
}
