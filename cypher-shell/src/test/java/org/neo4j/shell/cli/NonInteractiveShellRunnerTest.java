package org.neo4j.shell.cli;

import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.StreamShell;
import org.neo4j.shell.exception.ExitException;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.neo4j.shell.Util.asArray;

public class NonInteractiveShellRunnerTest {

    @Test
    public void testSimple() throws Exception {
        StreamShell shell = new StreamShell(
                "good1\n" +
                        "good2\n");
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(shell,
                CliArgHelper.parse(asArray("--fail-fast")));
        runner.run();

        assertThat(shell.getErrLog(), is(""));
    }

    @Test
    public void testFailFast() throws Exception {
        StreamShell shell = new StreamShell(
                "good1\n" +
                        "bad\n" +
                        "good2\n");
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(shell,
                CliArgHelper.parse(asArray("--fail-fast")));
        try {
            runner.run();
            fail("Expected an exception to be thrown");
        } catch (ClientException e) {
            assertEquals(e.getMessage(), "Found a bad line");
        }
    }

    @Test
    public void testFailAtEnd() throws Exception {
        final String input = "good1\n" +
                "bad\n" +
                "good2\n";
        final String expectedOut = "good1\nSuccess\n" +
                "bad\n" +
                "good2\nSuccess\n";
        StreamShell shell = new StreamShell(input);
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(shell,
                CliArgHelper.parse(asArray("--fail-at-end")));
        try {
            runner.run();
            fail("Expected an exit code to be set");
        } catch (ExitException e) {
            assertEquals("Wrong exit code", 1, e.getCode());
            assertEquals("Incorrect STDERR", "Found a bad line\n", shell.getErrLog());
            assertEquals("Incorrect STDOUT", expectedOut, shell.getOutLog().replaceAll("\r", ""));
        }
    }
}
