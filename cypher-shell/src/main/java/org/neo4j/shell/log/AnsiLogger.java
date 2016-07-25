package org.neo4j.shell.log;

import org.fusesource.jansi.Ansi;

import javax.annotation.Nonnull;
import java.io.PrintStream;

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
}
