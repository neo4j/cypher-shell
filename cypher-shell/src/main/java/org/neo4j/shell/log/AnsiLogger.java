package org.neo4j.shell.log;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import javax.annotation.Nonnull;
import java.io.PrintStream;

import static org.fusesource.jansi.internal.CLibrary.STDERR_FILENO;
import static org.fusesource.jansi.internal.CLibrary.STDOUT_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;

/**
 * A basic logger which prints Ansi formatted text to STDOUT and STDERR
 */
public class AnsiLogger implements Logger {
    private final PrintStream out;
    private final PrintStream err;

    public AnsiLogger() {
        this(System.out, System.err);
    }

    public AnsiLogger(@Nonnull PrintStream out, @Nonnull PrintStream err) {
        this.out = out;
        this.err = err;

        if (isOutputInteractive()) {
            Ansi.setEnabled(true);
            AnsiConsole.systemInstall();
        } else {
            Ansi.setEnabled(false);
        }
    }

    @Nonnull
    @Override
    public PrintStream getOutputStream() {
        return out;
    }

    @Nonnull
    @Override
    public PrintStream getErrorStream() {
        return err;
    }

    @Override
    public void printError(@Nonnull String s) {
        err.println(Ansi.ansi().render(s).toString());
    }

    @Override
    public void printOut(@Nonnull final String msg) {
        out.println(Ansi.ansi().render(msg).toString());
    }

    /**
     * @return true if the shell is outputting to a TTY, false otherwise (e.g., we are writing to a file)
     */
    private static boolean isOutputInteractive() {
        return 1 == isatty(STDOUT_FILENO) && 1 == isatty(STDERR_FILENO);
    }
}
