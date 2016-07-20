package org.neo4j.shell;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.StringShellRunner;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.io.IOException;
import java.util.Optional;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class CypherShellTest {

    Logger logger = mock(Logger.class);
    private ThrowingShell shell;

    @Before
    public void setup() {
        doReturn(System.out).when(logger).getOutputStream();
        shell = new ThrowingShell(logger);

        CommandHelper commandHelper = new CommandHelper(logger, Historian.empty, shell);

        shell.setCommandHelper(commandHelper);
    }

    @Test
    public void commandNameShouldBeParsed() {

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help    ");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commandNameShouldBeParsedWithNewline() {

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help    \n");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commandWithArgsShouldBeParsed() {

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help   arg1 arg2 ");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commentsShouldNotBeExecuted() throws Exception {
        shell.execute("// Hi, I'm a comment!");
        // If no exception was thrown, we have success
    }

    @Test
    public void emptyLinesShouldNotBeExecuted() throws Exception {
        shell.execute("");
        // If no exception was thrown, we have success
    }

    @Test
    public void secondLineCommentsShouldntBeExecuted() throws Exception {
        shell.execute("     \\\n" +
                "// Second line comment, first line escapes newline");
        // If no exception was thrown, we have success
    }

    @Test
    public void specifyingACypherStringShouldGiveAStringRunner() throws IOException {
        CliArgHelper.CliArgs cliArgs = CliArgHelper.parse("MATCH (n) RETURN n");

        ShellRunner shellRunner = ShellRunner.getShellRunner(cliArgs, logger);

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
        assertTrue(shell.getCommandExecutable(":help").isPresent());
        assertTrue(shell.getCommandExecutable(":help :set").isPresent());
        assertTrue(shell.getCommandExecutable(":set \"A piece of string\"").isPresent());
    }

    @Test
    public void unsetAlreadyClearedValue() throws CommandException {
        // when
        // then
        assertFalse("Expected param to be unset", shell.remove("unknown var").isPresent());
    }

    private TestShell connectedShell() throws CommandException {
        TestShell shell = new TestShell(logger);
        shell.connect(new ConnectionConfig("bla", 99, "bob", "pass"));
        return shell;
    }
}
