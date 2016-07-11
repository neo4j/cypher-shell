package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.TestTransaction;
import org.neo4j.shell.exception.CommandException;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.*;

public class RollbackTest {

    private TestShell shell;
    private Command cmd;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.cmd = new Rollback(shell);
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
    public void rollbackTransaction() throws CommandException {
        shell.connect();
        shell.beginTransaction();

        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());

        TestTransaction tx = (TestTransaction) shell.getCurrentTransaction().get();

        cmd.execute(new ArrayList<>());

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertFalse("Transaction should not be successful", tx.isSuccess());
        assertFalse("Expected tx to be gone", shell.getCurrentTransaction().isPresent());
    }

    @Test
    public void closingWhenNoTXOpenShouldThrow() throws CommandException {
        shell.connect();
        assertFalse("Did not expect an open transaction here", shell.getCurrentTransaction().isPresent());
        try {
            cmd.execute(new ArrayList<>());
            fail("Can't commit when no tx is open!");
        } catch (CommandException e) {
            assertTrue("unexpected error", e.getMessage().contains("no open transaction to rollback"));
        }
    }
}
