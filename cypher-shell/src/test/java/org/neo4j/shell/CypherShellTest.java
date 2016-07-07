package org.neo4j.shell;

import org.junit.Test;
import org.neo4j.driver.v1.Transaction;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
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
    public void specifyingACypherStringShouldGiveAStringRunner() throws IOException {
        CliArgHelper.CliArgs cliArgs = CliArgHelper.parse("MATCH (n) RETURN n");

        ShellRunner shellRunner = new TestShell().getShellRunner(cliArgs);

        if (!(shellRunner instanceof StringShellRunner)) {
            fail("Expected a different runner than: " + shellRunner.getClass().getSimpleName());
        }
    }

    @Test
    public void shouldExecuteInTransactionIfOpen() throws CommandException {
        TestShell shell = new TestShell();
        shell.connect();
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

    private class ThrowingShell extends TestShell {
        @Override
        protected void executeCypher(@Nonnull String line) {
            throw new RuntimeException("Unexpected cypher execution");
        }
    }
}
