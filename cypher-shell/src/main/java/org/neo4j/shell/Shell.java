package org.neo4j.shell;

import jline.console.history.History;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Optional;

public interface Shell {
    void printOut(@Nonnull String msg);

    void printError(@Nonnull String msg);

    @Nonnull
    InputStream getInputStream();

    @Nonnull
    PrintStream getOutputStream();

    @Nonnull
    String prompt();

    //TODO:DELETE IT - PRAVEENA
    @Nullable
    Character promptMask();

    /**
     * Handle a single line of input. If this is a part of a multi-line statement, it should be handled accordingly.
     * Otherwise it is expected to be executed immediately.
     *
     * @param line single line of input
     */
    void executeLine(@Nonnull String line) throws ExitException, CommandException;

    boolean isConnected();

    void connect(@Nonnull String host, int port, @Nonnull String username,
                 @Nonnull String password) throws CommandException;

    void disconnect() throws CommandException;

    void beginTransaction() throws CommandException;

    void commitTransaction() throws CommandException;

    CommandHelper getCommandHelper();

    Optional<History> getHistory();

    void rollbackTransaction() throws CommandException;

    @Nonnull
    Map<String, Object> getQueryParams();

    void set(@Nonnull String name, String valueString);
}
