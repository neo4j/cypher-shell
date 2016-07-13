package org.neo4j.shell.commands;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.TestShell;
import org.neo4j.shell.TestTransaction;
import org.neo4j.shell.exception.CommandException;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.mock;

@Ignore
public class CommitIntegrationTest {
    private TestShell shell;
    private Command commitCommand;

    @Before
    public void setup() {
        this.shell = new TestShell();
        this.commitCommand = new Commit(shell);
    }

    @Test
    public void closeTransactionAfterCommit() throws CommandException {
        connectShell();
        shell.beginTransaction();

        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());

        TestTransaction tx = (TestTransaction) shell.getCurrentTransaction().get();

        commitCommand.execute(new ArrayList<>());

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertTrue("Transaction should be successful", tx.isSuccess());
        assertFalse("Expected tx to be gone", shell.getCurrentTransaction().isPresent());
    }

    @Test
    public void closingWhenNoTXOpenShouldThrow() throws CommandException {
        connectShell();
        assertFalse("Did not expect an open transaction here", shell.getCurrentTransaction().isPresent());
        try {
            commitCommand.execute(new ArrayList<>());
            fail("Can't commit when no tx is open!");
        } catch (CommandException e) {
            assertTrue("unexpected error", e.getMessage().contains("no open transaction to commit"));
        }
    }

    private void connectShell() throws CommandException {
        shell.connect("bla", 99, "bob", "pass");
    }
}
