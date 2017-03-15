package org.neo4j.shell;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.cli.NonInteractiveShellRunner;
import org.neo4j.shell.log.Logger;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.neo4j.shell.ShellRunner.getShellRunner;

public class ShellRunnerTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    private final ConnectionConfig connectionConfig = mock(ConnectionConfig.class);

    @Test
    public void inputIsNonInteractiveIfForced() throws Exception {
        CliArgs args = new CliArgs();
        args.setNonInteractive(true);
        ShellRunner runner = getShellRunner(args, mock(CypherShell.class), mock(Logger.class), connectionConfig);
        assertTrue("Should be non-interactive shell runner when forced",
                runner instanceof NonInteractiveShellRunner);
    }
}
