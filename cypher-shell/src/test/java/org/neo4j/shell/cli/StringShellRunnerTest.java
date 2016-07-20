package org.neo4j.shell.cli;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.CommandExecuter;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class StringShellRunnerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Logger logger = mock(Logger.class);
    private CommandExecuter commandExecuter = mock(CommandExecuter.class);

    @Before
    public void setup() throws CommandException {
    }

    @Test
    public void nullCypherShouldThrowException() throws IOException {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("No cypher string specified");

        new StringShellRunner(new CliArgHelper.CliArgs(), logger);
    }

    @Test
    public void cypherShouldBePassedToRun() throws IOException, CommandException {
        String cypherString = "nonsense string";
        StringShellRunner runner = new StringShellRunner(CliArgHelper.parse(cypherString), logger);

        int code = runner.runUntilEnd(commandExecuter);

        assertEquals("Wrong exit code", 0, code);
        verify(commandExecuter).execute("nonsense string");
        verifyNoMoreInteractions(commandExecuter);
    }

    @Test
    public void errorsShouldThrow() throws IOException, CommandException {
        doThrow(new ClientException("Error kaboom")).when(commandExecuter).execute(anyString());

        StringShellRunner runner = new StringShellRunner(CliArgHelper.parse("nan anana"), logger);

        int code = runner.runUntilEnd(commandExecuter);

        assertEquals("Wrong exit code", 1, code);
        verify(logger).printError("Error kaboom");
    }
}
