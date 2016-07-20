package org.neo4j.shell.cli;


import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.SimpleShell;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.io.IOException;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class StringShellRunnerTest {

    private Logger logger = mock(Logger.class);
    private SimpleShell shell = new SimpleShell(logger);

    @Before
    public void setup() throws CommandException {
        connectShell();
    }

    @Test
    public void nullCypherShouldThrowException() throws IOException {
        try {
            new StringShellRunner(new CliArgHelper.CliArgs(), logger);
            fail("Expected an exception");
        } catch (NullPointerException e) {
            assertEquals("No cypher string specified",
                    e.getMessage());
        }
    }

    @Test
    public void cypherShouldBePassedToRun() throws IOException, CommandException {
        String cypherString = "nonsense string";
        StringShellRunner runner = new StringShellRunner(CliArgHelper.parse(cypherString), logger);

        runner.runUntilEnd(shell);
        assertEquals(cypherString, shell.cypher());
    }

    @Test
    public void errorsShouldThrow() throws IOException, CommandException {
        StringShellRunner runner = new StringShellRunner(CliArgHelper.parse(SimpleShell.ERROR), logger);

        try {
            runner.runUntilEnd(shell);
        } catch (ClientException e) {
            assertEquals(SimpleShell.ERROR, e.getMessage());
        }
    }

    private void connectShell() throws CommandException {
        //shell.connect(new ConnectionConfig("bla", 99, "bob", "pass"));
    }
}
