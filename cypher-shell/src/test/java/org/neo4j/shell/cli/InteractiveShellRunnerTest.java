package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class InteractiveShellRunnerTest {
    Logger logger = mock(Logger.class);
    StatementExecuter cmdExecuter = mock(StatementExecuter.class);

    @Before
    public void setup() {
        doReturn(System.out).when(logger).getOutputStream();
    }

    @Test
    public void testSimple() throws Exception {
        String input = "good1\n" +
                "good2\n";
        CommandReader commandReader = new CommandReader(
                new ByteArrayInputStream(input.getBytes()),
                logger);
        InteractiveShellRunner runner = new InteractiveShellRunner(commandReader, logger);
        runner.runUntilEnd(cmdExecuter);

        verify(cmdExecuter).execute("good1\n");
        verify(cmdExecuter).execute("good2\n");
        verifyNoMoreInteractions(cmdExecuter);
    }

    @Test
    public void runUntilEndShouldKeepGoingOnErrors() throws IOException, CommandException {
        String input = "good1\n" +
                "bad1\n" +
                "good2\n" +
                "bad2\n" +
                "good3\n";
        CommandReader commandReader = new CommandReader(
                new ByteArrayInputStream(input.getBytes()),
                logger);
        InteractiveShellRunner runner = new InteractiveShellRunner(commandReader, logger);


        doThrow(new ClientException("bad cmd")).when(cmdExecuter).execute(contains("bad"));

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Wrong exit code", 0, code);

        verify(cmdExecuter).execute("good1\n");
        verify(cmdExecuter).execute("bad1\n");
        verify(cmdExecuter).execute("good2\n");
        verify(cmdExecuter).execute("bad2\n");
        verify(cmdExecuter).execute("good3\n");
        verifyNoMoreInteractions(cmdExecuter);

        verify(logger, times(2)).printError("bad cmd");
    }

    @Test
    public void runUntilEndShouldStopOnExitExceptionAndReturnCode() throws IOException, CommandException {
        String input = "good1\n" +
                "bad1\n" +
                "good2\n" +
                "exit\n" +
                "bad2\n" +
                "good3\n";
        CommandReader commandReader = new CommandReader(
                new ByteArrayInputStream(input.getBytes()),
                logger);
        InteractiveShellRunner runner = new InteractiveShellRunner(commandReader, logger);


        doThrow(new ClientException("bad cmd")).when(cmdExecuter).execute(contains("bad"));
        doThrow(new ExitException(1234)).when(cmdExecuter).execute(contains("exit"));

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Wrong exit code", 1234, code);

        verify(cmdExecuter).execute("good1\n");
        verify(cmdExecuter).execute("bad1\n");
        verify(cmdExecuter).execute("good2\n");
        verify(cmdExecuter).execute("exit\n");
        verifyNoMoreInteractions(cmdExecuter);

        verify(logger).printError("bad cmd");
    }
}
