package org.neo4j.shell;

import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.neo4j.shell.Util.asArray;

public class NonInteractiveShellRunnerTest {

    @Test
    public void testSimple() throws Exception {
        TestShell shell = new TestShell(
                "good1\n" +
                        "good2\n");
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(shell, CliArgHelper.parse(asArray("-ff")));
        runner.run();
    }

    @Test
    public void testFailFast() throws Exception {
        TestShell shell = new TestShell(
                "good1\n" +
                        "bad\n" +
                        "good2\n");
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(shell, CliArgHelper.parse(asArray("-ff")));
        try {
            runner.run();
            fail("Expected an exception to be thrown");
        } catch (ClientException e) {
            assertEquals(e.getMessage(), "Found a bad line");
        }
    }

    @Test
    public void testFailAtEnd() throws Exception {
        final String input =  "good1\n" +
                "bad\n" +
                "good2\n";
        final String expectedOut =  "good1\nSuccess\n" +
                "bad\n" +
                "good2\nSuccess\n";
        TestShell shell = new TestShell(input);
        NonInteractiveShellRunner runner = new NonInteractiveShellRunner(shell, CliArgHelper.parse(asArray("-fae")));
        try {
            runner.run();
            fail("Expected an exit code to be set");
        } catch (Exit.ExitException e) {
            assertEquals("Wrong exit code", 1, e.getCode());
            assertEquals("Incorrect STDERR", "Found a bad line\n", shell.getErrLog());
            assertEquals("Incorrect STDOUT", expectedOut, shell.getOutLog().replaceAll("\r", ""));
        }
    }

    private class TestShell extends Shell {
        private final ByteArrayOutputStream errStream;
        private final ByteArrayOutputStream outStream;

        TestShell(@Nonnull final String input) {
            in = new ByteArrayInputStream(input.getBytes());
            errStream = new java.io.ByteArrayOutputStream();
            err = new PrintStream(errStream);
            outStream = new ByteArrayOutputStream();
            out = new PrintStream(outStream);
        }

        @Nonnull
        @Override
        public String prompt() {
            return "";
        }

        @Nullable
        @Override
        public Character promptMask() {
            return null;
        }

        @Override
        public void execute(@Nonnull String line) throws Exit.ExitException, CommandException {
            if (line.contains("bad")) {
                throw new ClientException("Found a bad line");
            } else {
                printOut("Success");
            }
        }

        public String getOutLog() {
            return outStream.toString();
        }

        public String getErrLog() {
            return errStream.toString();
        }
    }
}
