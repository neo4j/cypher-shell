package org.neo4j.shell;

import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.cli.FileHistorian;
import org.neo4j.shell.cli.InteractiveShellRunner;
import org.neo4j.shell.cli.NonInteractiveShellRunner;
import org.neo4j.shell.cli.StringShellRunner;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.parser.ShellStatementParser;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.fusesource.jansi.internal.CLibrary.STDIN_FILENO;
import static org.fusesource.jansi.internal.CLibrary.STDOUT_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;
import static org.neo4j.shell.system.Utils.isWindows;

public interface ShellRunner {

    /**
     * Run and handle user input until end of file
     *
     * @return error code to exit with
     */
    int runUntilEnd();

    /**
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
     * @param connectionConfig
     * @return a ShellRunner
     * @throws IOException
     */
    @Nonnull
    static ShellRunner getShellRunner(@Nonnull CliArgs cliArgs,
                                      @Nonnull CypherShell cypherShell,
                                      @Nonnull Logger logger,
                                      @Nonnull ConnectionConfig connectionConfig) throws IOException {
        if (cliArgs.getCypher().isPresent()) {
            return new StringShellRunner(cliArgs, cypherShell, logger);
        } else if (shouldBeInteractive(cliArgs)) {
            UserMessagesHandler userMessagesHandler =
                    new UserMessagesHandler(connectionConfig, cypherShell.getServerVersion());
            return new InteractiveShellRunner(cypherShell, cypherShell, logger, new ShellStatementParser(),
                    System.in, FileHistorian.getDefaultHistoryFile(), userMessagesHandler);
        } else {
            return new NonInteractiveShellRunner(cliArgs.getFailBehavior(), cypherShell, logger,
                    new ShellStatementParser(), System.in);
        }
    }

    /**
     * @param cliArgs
     * @return true if an interactive shellrunner should be used, false otherwise
     */
    static boolean shouldBeInteractive(@Nonnull CliArgs cliArgs) {
        if (cliArgs.getNonInteractive()) {
            return false;
        }

        return isInputInteractive();
    }

    /**
     * Checks if STDIN is a TTY. In case TTY checking is not possible (lack of libc), then the check falls back to
     * the built in Java {@link System#console()} which checks if EITHER STDIN or STDOUT has been redirected.
     *
     * @return true if the shell is reading from an interactive terminal, false otherwise (e.g., we are reading from a
     * file).
     */
    static boolean isInputInteractive() {
        if (isWindows()) {
            // Input will never be a TTY on windows and it isatty seems to be able to block forever on Windows so avoid
            // calling it.
            return System.console() != null;
        }
        try {
            return 1 == isatty(STDIN_FILENO);
        } catch (Throwable ignored) {
            // system is not using libc (like Alpine Linux)
            // Fallback to checking stdin OR stdout
            return System.console() != null;
        }
    }

    /**
     * Checks if STDOUT is a TTY. In case TTY checking is not possible (lack of libc), then the check falls back to
     * the built in Java {@link System#console()} which checks if EITHER STDIN or STDOUT has been redirected.
     *
     * @return true if the shell is outputting to an interactive terminal, false otherwise (e.g., we are outputting
     * to a file)
     */
    static boolean isOutputInteractive() {
        if (isWindows()) {
            // Input will never be a TTY on windows and it isatty seems to be able to block forever on Windows so avoid
            // calling it.
            return System.console() != null;
        }
        try {
            return 1 == isatty(STDOUT_FILENO);
        } catch (Throwable ignored) {
            // system is not using libc (like Alpine Linux)
            // Fallback to checking stdin OR stdout
            return System.console() != null;
        }
    }
}
