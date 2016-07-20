package org.neo4j.shell.cli;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.shell.CommandExecuter;
import org.neo4j.shell.log.Logger;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.*;

public class InteractiveShellRunnerTest {
    Logger logger = mock(Logger.class);
    CommandExecuter cmdExecuter = mock(CommandExecuter.class);

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
}
