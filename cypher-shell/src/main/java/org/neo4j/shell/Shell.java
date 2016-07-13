package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.PrintStream;

public interface Shell {
    void printOut(@Nonnull String msg);

    void printError(@Nonnull String msg);

    @Nonnull
    InputStream getInputStream();

    @Nonnull
    PrintStream getOutputStream();

    @Nonnull
    String prompt();

    @Nullable
    Character promptMask();

    /**
     * Handle a single line of input. If this is a part of a multi-line statement, it should be handled accordingly.
     * Otherwise it is expected to be executed immediately.
     *
     * @param line single line of input
     */
    void executeLine(@Nonnull String line) throws ExitException, CommandException;
}
