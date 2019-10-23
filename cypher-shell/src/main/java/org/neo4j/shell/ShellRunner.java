package org.neo4j.shell;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.io.output.WriterOutputStream;

import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.cli.FileHistorian;
import org.neo4j.shell.cli.InteractiveShellRunner;
import org.neo4j.shell.cli.NonInteractiveShellRunner;
import org.neo4j.shell.cli.StringShellRunner;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.parser.ShellStatementParser;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
            return new InteractiveShellRunner(cypherShell, cypherShell, cypherShell, logger, new ShellStatementParser(),
                    System.in, FileHistorian.getDefaultHistoryFile(), userMessagesHandler, connectionConfig);
        } else {

            return new NonInteractiveShellRunner(cliArgs.getFailBehavior(), cypherShell, logger,
                    new ShellStatementParser(), getInputStream(cliArgs));
        }
    }

    /**
     * @param cliArgs
     * @return true if an interactive shellrunner should be used, false otherwise
     */
    static boolean shouldBeInteractive(@Nonnull CliArgs cliArgs) {
        if ( cliArgs.getNonInteractive() || cliArgs.getInputFilename() != null )
        {
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

    /**
     * If an input file has been defined use that, otherwise use STDIN
     * @throws FileNotFoundException if the provided input file doesn't exist
     */
    static InputStream getInputStream(CliArgs cliArgs) throws FileNotFoundException
    {
        if ( cliArgs.getInputFilename() == null )
        {
            return System.in;
        }
        else
        {
            return new BufferedInputStream( new FileInputStream( new File( cliArgs.getInputFilename() ) ) );
        }
    }

    static OutputStream getOutputStreamForInteractivePrompt() {
        if (isWindows()) {
            // Output will never be a TTY on windows and it isatty seems to be able to block forever on Windows so avoid
            // calling it.
            if (System.console() != null) {
                return new WriterOutputStream(System.console().writer(), Charset.defaultCharset());
            }
        } else {
            try {
                if (1 == isatty(STDOUT_FILENO)) {
                    return System.out;
                } else {
                    return new FileOutputStream(new File("/dev/tty"));
                }
            } catch (Throwable ignored) {
                // system is not using libc (like Alpine Linux)
                // Fallback to checking stdin OR stdout
                if (System.console() != null) {
                    return new WriterOutputStream(System.console().writer(), Charset.defaultCharset());
                }
            }
        }
        return new NullOutputStream();
    }
}
