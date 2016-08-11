package org.neo4j.shell.cli;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.Historian;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class StringShellRunnerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Logger logger = mock(Logger.class);
    private StatementExecuter statementExecuter = mock(StatementExecuter.class);

    @Test
    public void nullCypherShouldThrowException() throws IOException {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("No cypher string specified");

        new StringShellRunner(new CliArgs(), statementExecuter, logger);
    }

    @Test
    public void cypherShouldBePassedToRun() throws IOException, CommandException {
        String cypherString = "nonsense string";
        StringShellRunner runner = new StringShellRunner(CliArgHelper.parse(cypherString), statementExecuter, logger);

        int code = runner.runUntilEnd();

        assertEquals("Wrong exit code", 0, code);
        verify(statementExecuter).execute("nonsense string");
        verifyNoMoreInteractions(statementExecuter);
    }

    @Test
    public void errorsShouldThrow() throws IOException, CommandException {
        doThrow(new ClientException("Error kaboom")).when(statementExecuter).execute(anyString());

        StringShellRunner runner = new StringShellRunner(CliArgHelper.parse("nan anana"), statementExecuter, logger);

        int code = runner.runUntilEnd();

        assertEquals("Wrong exit code", 1, code);
        verify(logger).printError("@|RED Error kaboom|@");
    }

    @Test
    public void shellRunnerHasNoHistory() throws Exception {
        // given
        StringShellRunner runner = new StringShellRunner(CliArgHelper.parse("nan anana"), statementExecuter, logger);

        // when then
        assertEquals(Historian.empty, runner.getHistorian());
    }
}
