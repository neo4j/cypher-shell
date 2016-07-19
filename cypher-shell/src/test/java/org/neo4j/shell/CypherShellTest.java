package org.neo4j.shell;

import org.junit.Test;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.StringShellRunner;
import org.neo4j.shell.exception.CommandException;

import java.io.IOException;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;


public class CypherShellTest {

    @Test
    public void commandNameShouldBeParsed() {
        ThrowingShell shell = new ThrowingShell();

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help    ");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commandNameShouldBeParsedWithNewline() {
        ThrowingShell shell = new ThrowingShell();

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help    \n");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commandWithArgsShouldBeParsed() {
        ThrowingShell shell = new ThrowingShell();

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help   arg1 arg2 ");

        assertTrue(exe.isPresent());
    }

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
    public void closeTransactionAfterRollback() throws CommandException {
        TestShell shell = connectedShell();
        shell.beginTransaction();

        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());

        TestTransaction tx = (TestTransaction) shell.getCurrentTransaction().get();

        shell.rollbackTransaction();

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertFalse("Transaction should not be successful", tx.isSuccess());
        assertFalse("Expected tx to be gone", shell.getCurrentTransaction().isPresent());
    }

    @Test
    public void closeTransactionAfterCommit() throws CommandException {
        TestShell shell = connectedShell();
        shell.beginTransaction();

        assertTrue("Expected an open transaction", shell.getCurrentTransaction().isPresent());

        TestTransaction tx = (TestTransaction) shell.getCurrentTransaction().get();

        shell.commitTransaction();

        assertFalse("Transaction should not still be open", tx.isOpen());
        assertTrue("Transaction should be successful", tx.isSuccess());
        assertFalse("Expected tx to be gone", shell.getCurrentTransaction().isPresent());
    }

    @Test
    public void shouldExecuteInTransactionIfOpen() throws CommandException {
        TestShell shell = connectedShell();
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

    @Test
    public void shouldParseCommandsAndArgs() {
        TestShell shell = new TestShell();
        assertTrue(shell.getCommandExecutable(":help").isPresent());
        assertTrue(shell.getCommandExecutable(":help :set").isPresent());
        assertTrue(shell.getCommandExecutable(":set \"A piece of string\"").isPresent());
    }

    private TestShell connectedShell() throws CommandException {
        TestShell shell = new TestShell();
        shell.connect("bla", 99, "bob", "pass");
        return shell;
    }
}
