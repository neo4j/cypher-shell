package org.neo4j.shell.cli;

import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.CommandExecuter;
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
    CommandExecuter cmdExecuter = new GoodBadExecuter();

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
        verifyZeroInteractions(logger);
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
        verifyNoMoreInteractions(logger);
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
        verify(logger).printError(eq("Found a bad line"));
        verify(logger).printError(eq("Found a bad line"));
        verifyNoMoreInteractions(logger);
    }

    private class GoodBadExecuter implements CommandExecuter {
        @Override
        public void execute(@Nonnull String command) throws ExitException, CommandException {
            if (command.contains("bad")) {
                throw new ClientException("Found a bad line");
            }
        }
    }
}
