package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.neo4j.shell.test.Util.ctrl;

public class NonInteractiveShellRunnerTest {

    private Logger logger = mock(Logger.class);
    private StatementExecuter cmdExecuter = mock(StatementExecuter.class);

    @Before
    public void setup() throws CommandException {
        doThrow(new ClientException("Found a bad line")).when(cmdExecuter).execute(contains("bad"));
        doReturn(System.out).when(logger).getOutputStream();
    }

    @Test
    public void testSimple() throws Exception {
        String input = "good1\n" +
                "good2\n";
        CommandReader commandReader = new CommandReader(
                new ByteArrayInputStream(input.getBytes()),
                logger);
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(
                CliArgHelper.FailBehavior.FAIL_FAST,
                logger,
                new ByteArrayInputStream(input.getBytes()));
        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Exit code incorrect", 0, code);
        verify(logger, times(0)).printError(anyString());
    }

    @Test
    public void testFailFast() throws Exception {
        String input =
                "good1\n" +
                        "bad\n" +
                        "good2\n" +
                        "bad\n";
        CommandReader commandReader = new CommandReader(
                new ByteArrayInputStream(input.getBytes()),
                logger);
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(
                CliArgHelper.FailBehavior.FAIL_FAST,
                logger,
                new ByteArrayInputStream(input.getBytes()));

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Exit code incorrect", 1, code);
        verify(logger).printError(eq("@|RED Found a bad line|@"));
    }

    @Test
    public void testFailAtEnd() throws Exception {
        String input =
                "good1\n" +
                        "bad\n" +
                        "good2\n" +
                        "bad\n";
        CommandReader commandReader = new CommandReader(
                new ByteArrayInputStream(input.getBytes()),
                logger);
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(
                CliArgHelper.FailBehavior.FAIL_AT_END,
                logger,
                new ByteArrayInputStream(input.getBytes()));

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Exit code incorrect", 1, code);
        verify(logger, times(2)).printError(eq("@|RED Found a bad line|@"));
    }

    @Test
    public void ctrlCKillsNonInteractiveReadingCommandsShellFailFast() throws Exception {
        String input = "good1\n" +
                "good2\n" +
                ctrl('C') +
                "good3\n";
        CommandReader commandReader = new CommandReader(
                new ByteArrayInputStream(input.getBytes()),
                logger);
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(CliArgHelper.FailBehavior.FAIL_FAST,
                logger, new ByteArrayInputStream(input.getBytes()));

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Wrong exit code", 1, code);

        verify(cmdExecuter).execute("good1\n");
        verify(cmdExecuter).execute("good2\n");
        verifyNoMoreInteractions(cmdExecuter);
    }

    @Test
    public void ctrlCKillsNonInteractiveShellReadingCommandsFailEnd() throws Exception {
        String input = "good1\n" +
                "good2\n" +
                ctrl('C') +
                "good3\n";
        CommandReader commandReader = new CommandReader(
                new ByteArrayInputStream(input.getBytes()),
                logger);
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(CliArgHelper.FailBehavior.FAIL_AT_END,
                logger, new ByteArrayInputStream(input.getBytes()));

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Wrong exit code", 1, code);

        verify(cmdExecuter).execute("good1\n");
        verify(cmdExecuter).execute("good2\n");
        verifyNoMoreInteractions(cmdExecuter);
    }
}
