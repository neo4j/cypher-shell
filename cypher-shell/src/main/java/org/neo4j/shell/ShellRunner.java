package org.neo4j.shell;

import org.neo4j.shell.cli.*;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.fusesource.jansi.internal.CLibrary.STDIN_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;

public interface ShellRunner {
    /**
     * Run and handle user input until end of file
     *
     * @return error code to exit with
     */
    int runUntilEnd(@Nonnull StatementExecuter executer);

    /**
     *
     * @return an object which can provide the history of commands executed
     */
    Historian getHistorian();

    /**
     * Get an appropriate shellrunner depending on the given arguments and if we are running in a TTY.
     *
     * @param cliArgs
     * @return a ShellRunner
     * @throws IOException
     */
    static ShellRunner getShellRunner(@Nonnull CliArgHelper.CliArgs cliArgs, @Nonnull Logger logger) throws IOException {
        CommandReader commandReader = new CommandReader(logger);
        if (cliArgs.getCypher().isPresent()) {
            return new StringShellRunner(cliArgs, logger);
        } else if (isInteractive()) {
            return new InteractiveShellRunner(commandReader, logger);
        } else {
            return new NonInteractiveShellRunner(cliArgs.getFailBehavior(), commandReader, logger);
        }
    }

    /**
     * @return true if the shell is a TTY, false otherwise (e.g., we are reading from a file)
     */
    static boolean isInteractive() {
        return 1 == isatty(STDIN_FILENO);
    }
}
