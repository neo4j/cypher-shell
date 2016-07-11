package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.exception.CommandException;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class SetTest {

    private TestShell shell = new TestShell();
    private Command cmd;

    @Before
    public void setup() {
        this.cmd = new Set(shell);
    }

    @Test
    public void shouldFailIfNoArgs() {
        try {
            cmd.execute("");
            fail("Expected error");
        } catch (CommandException e) {
            assertTrue("Unexpected error: " + e.getMessage(), e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }

    @Test
    public void shouldFailIfOneArg() {
        try {
            cmd.execute("bob");
            fail("Expected error");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }

    @Test
    public void shouldFailIfTooManyArgs() {
        try {
            cmd.execute("bob mob zob");
            fail("Expected error");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }

    @Test
    public void setValue() throws CommandException {
        shell.connect();
        cmd.execute("bob   9");
        assertEquals("Expected param to be set",
                "9", shell.getQueryParams().get("bob").toString());
    }
}
