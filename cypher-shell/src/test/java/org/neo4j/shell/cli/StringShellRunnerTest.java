package org.neo4j.shell.cli;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.SimpleShell;
import org.neo4j.shell.exception.CommandException;

import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class StringShellRunnerTest {

    private SimpleShell shell;

    @Before
    public void setup() throws CommandException {
        shell = new SimpleShell();
        connectShell();
    }

    @Test
    public void nullCypherShouldThrowException() throws IOException {
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
        StringShellRunner runner = new StringShellRunner(shell, CliArgHelper.parse(cypherString));

        runner.run();
        assertEquals(cypherString, shell.cypher());
    }

    @Test
    public void errorsShouldThrow() throws IOException, CommandException {
        StringShellRunner runner = new StringShellRunner(shell, CliArgHelper.parse(SimpleShell.ERROR));

        try {
            runner.run();
        } catch (ClientException e) {
            assertEquals(SimpleShell.ERROR, e.getMessage());
        }
    }

    private void connectShell() throws CommandException {
        shell.connect(new ConnectionConfig("bla", 99, "bob", "pass"));
    }
}
