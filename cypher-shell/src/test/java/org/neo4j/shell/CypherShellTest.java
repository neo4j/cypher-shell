package org.neo4j.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.StringShellRunner;
import org.neo4j.shell.commands.CommandExecutable;
import org.neo4j.shell.commands.CommandHelper;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import java.io.IOException;
import java.util.Optional;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class CypherShellTest {
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    Logger logger = mock(Logger.class);
    private ThrowingShell shell;

    @Before
    public void setup() {
        doReturn(System.out).when(logger).getOutputStream();
        shell = new ThrowingShell(logger);

        CommandHelper commandHelper = new CommandHelper(logger, Historian.empty, shell);

        shell.setCommandHelper(commandHelper);
    }

    @Test
    public void commandNameShouldBeParsed() {

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help    ");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commandNameShouldBeParsedWithNewline() {

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help    \n");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commandWithArgsShouldBeParsed() {

        Optional<CommandExecutable> exe = shell.getCommandExecutable("   :help   arg1 arg2 ");

        assertTrue(exe.isPresent());
    }

    @Test
    public void commentsShouldNotBeExecuted() throws Exception {
        shell.execute("// Hi, I'm a comment!");
        // If no exception was thrown, we have success
    }

    @Test
    public void emptyLinesShouldNotBeExecuted() throws Exception {
        shell.execute("");
        // If no exception was thrown, we have success
    }

    @Test
    public void secondLineCommentsShouldntBeExecuted() throws Exception {
        shell.execute("     \\\n" +
                "// Second line comment, first line escapes newline");
        // If no exception was thrown, we have success
    }

    @Test
    public void specifyingACypherStringShouldGiveAStringRunner() throws IOException {
        CliArgHelper.CliArgs cliArgs = CliArgHelper.parse("MATCH (n) RETURN n");

        ShellRunner shellRunner = ShellRunner.getShellRunner(cliArgs, logger);

        if (!(shellRunner instanceof StringShellRunner)) {
            fail("Expected a different runner than: " + shellRunner.getClass().getSimpleName());
        }
    }

    @Test
    public void shouldParseCommandsAndArgs() {
        assertTrue(shell.getCommandExecutable(":help").isPresent());
        assertTrue(shell.getCommandExecutable(":help :set").isPresent());
        assertTrue(shell.getCommandExecutable(":set \"A piece of string\"").isPresent());
    }

    @Test
    public void unsetAlreadyClearedValue() throws CommandException {
        // when
        // then
        assertFalse("Expected param to be unset", shell.remove("unknown var").isPresent());
    }
}
