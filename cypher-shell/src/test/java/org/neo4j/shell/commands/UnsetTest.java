package org.neo4j.shell.commands;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.exception.CommandException;

import static org.junit.Assert.*;

public class UnsetTest {

    private TestShell shell;
    private Command cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Unset(shell);
    }

    @Test
    public void shouldFailIfNoArgs() {
        try {
            cmd.execute("");
            fail("Expected error");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }

    @Test
    public void shouldFailIfMoreThanOneArg() {
        try {
            cmd.execute("bob nob");
            fail("Expected error");
        } catch (CommandException e) {
            assertTrue("Unexpected error", e.getMessage().startsWith("Incorrect number of arguments"));
        }
    }

    @Test
    public void unsetValue() throws CommandException {
        // given
        shell.connect();
        new Set(shell).execute("bob 9");

        assertEquals("Expected param to be set",
                "9", shell.getQueryParams().get("bob"));

        // when
        cmd.execute("bob");
        // then
        assertFalse("Expected param to be unset",
                shell.getQueryParams().containsKey("bob"));
    }

    @Test
    public void unsetAlreadyClearedValue() throws CommandException {
        // given
        shell.connect();

        assertFalse("Expected param to be unset",
                shell.getQueryParams().containsKey("nob"));

        // when
        cmd.execute("nob");
        // then
        assertFalse("Expected a no-op",
                shell.getQueryParams().containsKey("nob"));
    }
}
