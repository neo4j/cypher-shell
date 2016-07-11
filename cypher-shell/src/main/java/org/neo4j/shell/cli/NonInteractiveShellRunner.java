package org.neo4j.shell.cli;

import jline.console.ConsoleReader;
import jline.console.history.History;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.BoltHelper;
import org.neo4j.shell.Shell;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * A shell runner which reads from STDIN and executes commands until completion. In case of errors, the failBehavior
 * determines if the shell exits immediately, or if it should keep trying the next commands.
 */
public class NonInteractiveShellRunner extends ShellRunner {

    private final Shell shell;
    private final CliArgHelper.FailBehavior failBehavior;
    private final CommandReader commandReader;

    public NonInteractiveShellRunner(@Nonnull Shell shell, @Nonnull CliArgHelper.CliArgs cliArgs) throws IOException {
        super();
        failBehavior = cliArgs.getFailBehavior();
        this.shell = shell;
        ConsoleReader reader = new ConsoleReader(shell.getInputStream(), shell.getOutputStream());
        this.commandReader = new CommandReader(reader, this.shell);
    }

    @Override
    public void run() throws CommandException, IOException {
        String command;
        boolean running = true;
        boolean errorOccurred = false;
        while (running) {
            command = commandReader.readCommand();

            try {
                if (null == command) {
                    running = false;
                    continue;
                }

                if (!command.trim().isEmpty()) {
                    shell.executeLine(command);
                }
            } catch (ExitException e) {
                // These exceptions are always fatal
                throw e;
            } catch (ClientException e) {
                errorOccurred = true;
                if (CliArgHelper.FailBehavior.FAIL_AT_END == failBehavior) {
                    shell.printError(BoltHelper.getSensibleMsg(e));
                } else {
                    throw e;
                }
            } catch (Throwable t) {
                errorOccurred = true;
                if (CliArgHelper.FailBehavior.FAIL_AT_END == failBehavior) {
                    shell.printError(t.getMessage());
                } else {
                    throw t;
                }
            }
        }

        // End of input, in case of error, set correct exit code
        if (errorOccurred) {
            throw new ExitException(1);
        }
    }

    @Nullable
    @Override
    public History getHistory() {
        return null;
    }
}
