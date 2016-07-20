package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class NonInteractiveShellRunnerTest {

    Logger logger = mock(Logger.class);
    StatementExecuter cmdExecuter = new GoodBadExecuter();

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
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(
                CliArgHelper.FailBehavior.FAIL_FAST,
                commandReader,
                logger);
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
                commandReader,
                logger);

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Exit code incorrect", 1, code);
        verify(logger).printError(eq("Found a bad line"));
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
                commandReader,
                logger);

        int code = runner.runUntilEnd(cmdExecuter);

        assertEquals("Exit code incorrect", 1, code);
        verify(logger, times(2)).printError(eq("Found a bad line"));
    }

    private class GoodBadExecuter implements StatementExecuter {
        @Override
        public void execute(@Nonnull String command) throws ExitException, CommandException {
            if (command.contains("bad")) {
                throw new ClientException("Found a bad line");
            }
        }
    }
}
