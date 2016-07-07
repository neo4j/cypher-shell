package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.TestShell;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.*;

public class BeginTest {

    private TestShell shell;
    private Command cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Begin(shell);
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

    @Test
    public void needsToBeConnected() throws CommandException {
        shell.disconnect();
        try {
            cmd.execute(new ArrayList<>());
            fail("Should throw");
        } catch (CommandException e) {
            assertTrue("unexepcted error", e.getMessage().contains("Not connected"));
        }
    }

    @Test
    public void openTransaction() throws CommandException {
        shell.connect();
        assertFalse("Did not expect an open transaction here", shell.getCurrentTransaction().isPresent());

        cmd.execute(new ArrayList<>());

        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());
    }

    @Test
    public void nestedTransactionsAreNotSupported() throws CommandException {
        shell.connect();
        assertFalse("Did not expect an open transaction here", shell.getCurrentTransaction().isPresent());
        cmd.execute(new ArrayList<>());
        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());

        try {
            cmd.execute(new ArrayList<>());
            fail("Should throw");
        } catch (CommandException e) {
            assertTrue("unexpected error", e.getMessage().contains("already an open transaction"));
        }
    }
}
