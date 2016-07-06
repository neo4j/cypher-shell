package org.neo4j.shell;


import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;

import javax.annotation.Nonnull;

import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class StringShellRunnerTest {

    @Test
    public void nullCypherShouldThrowException() throws IOException {
        String cypherString = null;
        SimpleShell shell = new SimpleShell();
        try {
            new StringShellRunner(shell, new CliArgHelper.CliArgs());
            fail("Expected an exception");
        } catch (NullPointerException e) {
            assertEquals("No cypher string specified",
                    e.getMessage());
        }
    }

    @Test
    public void cypherShouldBePassedToRun() throws IOException, CommandException {
        String cypherString = "nonsense string";
        SimpleShell shell = new SimpleShell();
        StringShellRunner runner = new StringShellRunner(shell, CliArgHelper.parse("--cypher", cypherString));
        runner.run();
        assertEquals(cypherString, shell.cypher);
    }

    @Test
    public void errorsShouldThrow() throws IOException, CommandException {
        SimpleShell shell = new SimpleShell();
        StringShellRunner runner = new StringShellRunner(shell, CliArgHelper.parse("--cypher", SimpleShell.ERROR));
        try {
            runner.run();
        } catch (ClientException e) {
            assertEquals(SimpleShell.ERROR, e.getMessage());
        }
    }

    class SimpleShell extends TestShell {
        static final String ERROR = "error";
        String cypher;

        @Override
        void executeCypher(@Nonnull String cypher) {
            if (ERROR.equals(cypher)) {
                throw new ClientException(ERROR);
            }
            this.cypher = cypher;
        }
    }
}
