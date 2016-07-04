package org.neo4j.shell;

import jline.console.ConsoleReader;
import jline.console.history.History;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * A shell runner which reads from STDIN and executes commands until completion. In case of errors, the failBehavior
 * determines if the shell exits immediately, or if it should keep trying the next commands.
 */
public class NonInteractiveShellRunner extends ShellRunner {

    private final Shell shell;
    private final ConsoleReader reader;
    private final CliArgHelper.FailBehavior failBehavior;

    public NonInteractiveShellRunner(@Nonnull Shell shell, @Nonnull CliArgHelper.CliArgs cliArgs) throws IOException {
        super();
        failBehavior = cliArgs.getFailBehavior();
        this.shell = shell;
        reader = new ConsoleReader(shell.getInputStream(), shell.getOutputStream());
    }

    @Override
    public void run() throws CommandException, IOException {
        String line;
        boolean running = true;
        boolean errorOccurred = false;
        while (running) {
            line = reader.readLine();

            try {
                if (null == line) {
                    running = false;
                    continue;
                }

                if (!line.trim().isEmpty()) {
                    shell.executeLine(line);
                }
            } catch (Exit.ExitException e) {
                // These exceptions are always fatal
                throw e;
            } catch (ClientException e) {
                errorOccurred = true;
                if (CliArgHelper.FailBehavior.FAIL_AT_END == failBehavior) {
                    shell.printError(BoltHelper.getSensibleMsg(e));
                } else {
                    throw e;
                }
            }
            catch (Throwable t) {
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
            throw new Exit.ExitException(1);
        }
    }

    @Nullable
    @Override
    public History getHistory() {
        return null;
    }
}
