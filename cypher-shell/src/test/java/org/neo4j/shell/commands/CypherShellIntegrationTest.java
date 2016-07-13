package org.neo4j.shell.commands;


import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.shell.Command;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.TestTransaction;
import org.neo4j.shell.exception.CommandException;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.*;

@Ignore
public class CypherShellIntegrationTest {

    private CypherShell shell = new CypherShell("localhost", 7474, "neo4j", "neo");
    private Command rollbackCommand = new Rollback(shell);
    private Command commitCommand = new Commit(shell);
    private Command beginCommand = new Begin(shell);

    @Test
    public void shouldNotAcceptArgs() {
        try {
            rollbackCommand.execute(Arrays.asList("bob"));
            fail("Should not accept args");
        } catch (CommandException e) {
            assertTrue("Unexepcted error", e.getMessage().startsWith("Too many arguments"));
        }
    }

    @Test
    public void connectAndDisconnect() throws CommandException {
        shell.connect("localhost", 7474, "neo4j", "neo");
        try {
            rollbackCommand.execute(new ArrayList<>());
            fail("Should throw");
        } catch (CommandException e) {
            assertTrue("unexepcted error", e.getMessage().contains("Not connected"));
        }
    }

    @Test
    public void rollbackTransaction() throws CommandException {
        connectShell();
        shell.beginTransaction();

        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());

        TestTransaction tx = (TestTransaction) shell.getCurrentTransaction().get();

        rollbackCommand.execute(new ArrayList<>());

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertFalse("Transaction should not be successful", tx.isSuccess());
        assertFalse("Expected tx to be gone", shell.getCurrentTransaction().isPresent());
    }

    @Test
    public void closingWhenNoTXOpenShouldThrow() throws CommandException {
        connectShell();
        assertFalse("Did not expect an open transaction here", shell.getCurrentTransaction().isPresent());
        try {
            rollbackCommand.execute(new ArrayList<>());
            fail("Can't rolback when no tx is open!");
        } catch (CommandException e) {
            assertTrue("unexpected error", e.getMessage().contains("no open transaction to rollback"));
        }
    }

    private void connectShell() throws CommandException {
        shell.connect("bla", 99, "bob", "pass");
    }
}
