package org.neo4j.shell;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.cli.InteractiveShellRunner;
import org.neo4j.shell.cli.NonInteractiveShellRunner;
import org.neo4j.shell.log.Logger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.neo4j.shell.ShellRunner.getShellRunner;
import static org.neo4j.shell.ShellRunner.isInputInteractive;

public class ShellRunnerTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void isInputInteractiveThrowsOnWindows() throws Exception {
        assertFalse("No result should be returned on Windows",
                isInputInteractive("Windows 7").isPresent());
    }

    @Test
    public void isInputInteractiveDoesNotThrowOnNonWindows() throws Exception {
        assertTrue(isInputInteractive(null).isPresent());
        assertTrue(isInputInteractive("").isPresent());
        assertTrue(isInputInteractive("Linux").isPresent());
        assertTrue(isInputInteractive("BeOS").isPresent());
    }

    @Test
    public void inputIsInteractiveByDefaultOnWindows() throws Exception {
        assumeTrue(System.getProperty("os.name", "").toLowerCase().contains("windows"));
        ShellRunner runner = getShellRunner(new CliArgs(), mock(CypherShell.class), mock(Logger.class));
        assertTrue("Should be interactive shell runner by default on windows",
                runner instanceof InteractiveShellRunner);
    }

    @Test
    public void inputIsNonInteractiveIfForced() throws Exception {
        CliArgs args = new CliArgs();
        args.setNonInteractive(true);
        ShellRunner runner = getShellRunner(args, mock(CypherShell.class), mock(Logger.class));
        assertTrue("Should be non-interactive shell runner when forced",
                runner instanceof NonInteractiveShellRunner);
    }
}
