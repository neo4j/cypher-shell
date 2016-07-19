package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A shell command
 */
public interface Command {
    @Nonnull
    String getName();

    //Completer getCompleter();

    @Nonnull
    String getDescription();

    @Nonnull
    String getUsage();

    @Nonnull
    String getHelp();

    @Nonnull
    List<String> getAliases();

    void execute(@Nonnull final String args) throws ExitException, CommandException;
}
