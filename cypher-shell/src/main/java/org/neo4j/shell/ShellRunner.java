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
     * @param cypherShell
     * @param logger
     * @return a ShellRunner
     * @throws IOException
     */
    @Nonnull
    static ShellRunner getShellRunner(@Nonnull CliArgs cliArgs,
                                      @Nonnull CypherShell cypherShell,
                                      @Nonnull Logger logger) throws IOException {
        if (cliArgs.getCypher().isPresent()) {
            return new StringShellRunner(cliArgs, cypherShell, logger);
        } else if (isInputInteractive()) {
            return new InteractiveShellRunner(cypherShell, cypherShell, logger, new ShellStatementParser(),
                    System.in, FileHistorian.getDefaultHistoryFile());
        } else {
            return new NonInteractiveShellRunner(cliArgs.getFailBehavior(), cypherShell, logger,
                    new ShellStatementParser(), System.in);
        }
    }

    /**
     * Checks if STDIN is a TTY. In case TTY checking is not possible (lack of libc), then the check falls back to
     * the built in Java {@link System#console()} which checks if EITHER STDIN or STDOUT has been redirected.
     *
     * @return true if the shell reading from a TTY, false otherwise (e.g., we are reading from a file)
     */
    static boolean isInputInteractive() {
        try {
            return 1 == isatty(STDIN_FILENO);
        } catch (NoClassDefFoundError e) {
            // system is not using libc (like Alpine Linux)
            // Fallback to checking stdin OR stdout
            return System.console() != null;
        }
    }
}
