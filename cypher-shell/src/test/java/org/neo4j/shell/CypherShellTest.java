package org.neo4j.shell;

import org.junit.Test;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.StringShellRunner;
import org.neo4j.shell.exception.CommandException;

import java.io.IOException;
import java.util.Optional;

import static junit.framework.Assert.*;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.fail;


public class CypherShellTest {

    @Test
    public void commentsShouldNotBeExecuted() throws Exception {
        ThrowingShell shell = new ThrowingShell();
        shell.executeLine("// Hi, I'm a comment!");
        // If no exception was thrown, we have success
    }

    @Test
    public void emptyLinesShouldNotBeExecuted() throws Exception {
        ThrowingShell shell = new ThrowingShell();
        shell.executeLine("");
        // If no exception was thrown, we have success
    }

    @Test
    public void secondLineCommentsShouldntBeExecuted() throws Exception {
        ThrowingShell shell = new ThrowingShell();
        shell.executeLine("     \\\n" +
                "// Second line comment, first line escapes newline");
        // If no exception was thrown, we have success
    }

    @Test
    public void specifyingACypherStringShouldGiveAStringRunner() throws IOException {
        CliArgHelper.CliArgs cliArgs = CliArgHelper.parse("MATCH (n) RETURN n");

        ShellRunner shellRunner = new TestShell().getShellRunner(cliArgs);

        if (!(shellRunner instanceof StringShellRunner)) {
            fail("Expected a different runner than: " + shellRunner.getClass().getSimpleName());
        }
    }

    @Test
    public void successiveBeginTransactionsThrowsError() throws CommandException {
        CypherShell shell = connectedShell();
        shell.beginTransaction();

        try {
            shell.beginTransaction();
            fail("Should throw");
        } catch (CommandException e) {
            assertTrue("unexpected error", e.getMessage().contains("already an open transaction"));
        }
    }

    @Test
    public void beginTransaction() throws CommandException {
        CypherShell shell = connectedShell();
        shell.beginTransaction();

        assertNotNull(shell.getCurrentTransaction().get());
    }

    @Test
    public void closeTransactionAfterRollback() throws CommandException {
        CypherShell shell = connectedShell();
        shell.beginTransaction();

        assertNotNull(shell.getCurrentTransaction().get());

        TestTransaction tx = (TestTransaction) shell.tx;

        shell.rollbackTransaction();

        assertEquals(Optional.empty(), shell.getCurrentTransaction());
        assertFalse("Transaction should not still be open", tx.isOpen());
        assertFalse("Transaction should not be successful", tx.isSuccess());
    }

    @Test
    public void closeTransactionAfterCommit() throws CommandException {
        CypherShell shell = connectedShell();
        shell.beginTransaction();

        assertNotNull(shell.getCurrentTransaction().get());

        TestTransaction tx = (TestTransaction) shell.tx;

        shell.commitTransaction();

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertTrue("Transaction should be successful", tx.isSuccess());
        assertEquals(Optional.empty(), shell.getCurrentTransaction());
    }

    @Test
    public void cannotCommitWhenThereIsNoTransaction() throws CommandException {
        CypherShell shell = connectedShell();
        try {
            shell.commitTransaction();
            fail("Can't commit when no tx is open!");
        } catch (CommandException e) {
            assertTrue("unexpected error", e.getMessage().contains("no open transaction to commit"));
        }
    }

    @Test
    public void cannotRollbackWhenThereIsNoTransaction() throws CommandException {
        CypherShell shell = connectedShell();
        try {
            shell.rollbackTransaction();
            fail("Can't commit when no tx is open!");
        } catch (CommandException e) {
            assertTrue("unexpected error", e.getMessage().contains("no open transaction to rollback"));
        }
    }

    @Test
    public void shouldExecuteInTransactionIfOpen() throws CommandException {
        CypherShell shell = connectedShell();
        shell.beginTransaction();

        TestTransaction tx = null;
        Optional<Transaction> txo = shell.getCurrentTransaction();
        if (txo.isPresent()) {
            tx = (TestTransaction) txo.get();
        } else {
            fail("No transaction");
        }

        String cypherLine = "THIS IS CYPEHR";

        shell.executeCypher(cypherLine);

        assertEquals("did not execute in TX correctly", cypherLine, tx.getLastCypherStatement());
    }

    private CypherShell connectedShell() throws CommandException {
        CypherShell shell = new TestShell();
//        CypherShell shell = new CypherShell("bla", 99, "bob", "pass");
        shell.connect("bla", 99, "bob", "pass");
        return shell;
    }

}
