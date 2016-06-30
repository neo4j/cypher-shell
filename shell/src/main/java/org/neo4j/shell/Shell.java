package org.neo4j.shell;

import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.internal.CLibrary.STDIN_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;

/**
 * A possibly interactive commandline shell
 */
abstract public class Shell {

    public void printOut(@Nonnull final String msg) {
        System.out.println(ansi().render(msg));
    }

    public void printError(@Nonnull final String msg) {
        System.err.println(ansi().render(msg));
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

    ShellRunner getShellRunner(@Nonnull CliArgHelper.CliArgs cliArgs) throws IOException {
        if (isInteractive()) {
            return new InteractiveShellRunner(this);
        } else {
            return new NonInteractiveShellRunner(this, cliArgs);
        }
    }

    abstract public void execute(@Nonnull String line) throws Exit.ExitException, CommandException;
}
