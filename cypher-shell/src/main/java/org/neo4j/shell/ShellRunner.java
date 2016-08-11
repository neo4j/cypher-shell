package org.neo4j.shell;

import org.neo4j.shell.cli.*;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.parser.ShellStatementParser;

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
    int runUntilEnd();

    /**
     *
     * @return an object which can provide the history of commands executed
     */
    @Nonnull
    Historian getHistorian();

    /**
     * Get an appropriate shellrunner depending on the given arguments and if we are running in a TTY.
     *
     * @param cliArgs
     * @return a ShellRunner
     * @throws IOException
     */
    @Nonnull
    static ShellRunner getShellRunner(@Nonnull CliArgs cliArgs,
                                      @Nonnull StatementExecuter executer,
                                      @Nonnull Logger logger) throws IOException {
        if (cliArgs.getCypher().isPresent()) {
            return new StringShellRunner(cliArgs, executer, logger);
        } else if (isInputInteractive()) {
            return new InteractiveShellRunner(executer, logger, new ShellStatementParser(),
                    System.in, FileHistorian.getDefaultHistoryFile());
        } else {
            return new NonInteractiveShellRunner(cliArgs.getFailBehavior(), executer, logger,
                    new ShellStatementParser(), System.in);
        }
    }

    /**
     * @return true if the shell reading from a TTY, false otherwise (e.g., we are reading from a file)
     */
    static boolean isInputInteractive() {
        return 1 == isatty(STDIN_FILENO);
    }
}
