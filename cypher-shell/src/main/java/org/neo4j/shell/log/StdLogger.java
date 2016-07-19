package org.neo4j.shell.log;

import javax.annotation.Nonnull;
import java.io.PrintStream;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * A basic logger which prints to STDOUT and STDERR
 */
public class StdLogger implements Logger {
    private PrintStream out = System.out;
    private PrintStream err = System.err;

    @Nonnull
    @Override
    public PrintStream getOutputStream() {
        return out;
    }

    @Override
    public void printError(@Nonnull String s) {
        err.println(ansi().render(s));
    }

    @Override
    public void printOut(@Nonnull final String msg) {
        out.println(ansi().render(msg));
    }
}
