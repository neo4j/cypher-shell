package org.neo4j.shell;

import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.internal.CLibrary.STDIN_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;

/**
 * A possibly interactive commandline shell
 */
abstract public class Shell {

    protected InputStream in = System.in;
    protected PrintStream out = System.out;
    protected PrintStream err = System.err;

    public void printOut(@Nonnull final String msg) {
        out.println(ansi().render(msg));
    }

    public void printError(@Nonnull final String msg) {
        err.println(ansi().render(msg));
    }

    @Nonnull
    public InputStream getInputStream() {
        return in;
    }

    @Nonnull
    public PrintStream getOutputStream() {
        return out;
    }

    @Nonnull
    abstract public String prompt();

    @Nullable
    abstract public Character promptMask();

    /**
     *
     * @return true if the shell is a TTY, false otherwise (e.g., we are reading from a file)
     */
    boolean isInteractive() {
        return 1 == isatty(STDIN_FILENO);
    }

    /**
     * Get an appropriate shellrunner depending on the given arguments and if we are running in a TTY.
     * @param cliArgs
     * @return a ShellRunner
     * @throws IOException
     */
    ShellRunner getShellRunner(@Nonnull CliArgHelper.CliArgs cliArgs) throws IOException {
        if (cliArgs.getCypher().isPresent()) {
            return new StringShellRunner(this, cliArgs);
        } else if (isInteractive()) {
            return new InteractiveShellRunner(this);
        } else {
            return new NonInteractiveShellRunner(this, cliArgs);
        }
    }

    /**
     * Handle a single line of input. If this is a part of a multi-line statement, it should be handled accordingly.
     * Otherwise it is expected to be executed immediately.
     *
     * @param line single line of input
     */
    abstract public void executeLine(@Nonnull String line) throws Exit.ExitException, CommandException;

}
