package org.neo4j.shell.log;

import org.fusesource.jansi.Ansi;

import javax.annotation.Nonnull;
import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * A basic logger which prints Ansi formatted text to STDOUT and STDERR
 */
public class AnsiLogger implements Logger {
    private final PrintStream out;
    private final PrintStream err;
    private final Ansi ansi;

    public AnsiLogger() {
        this(System.out, System.err, new StringBuilder());
    }

    public AnsiLogger(@Nonnull PrintStream out, @Nonnull PrintStream err, @Nonnull StringBuilder sb) {
        this.out = out;
        this.err = err;
        this.ansi = ansi(sb);
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
        err.println(ansi.render(s));
    }

    @Override
    public void printOut(@Nonnull final String msg) {
        out.println(ansi.render(msg));
    }
}
