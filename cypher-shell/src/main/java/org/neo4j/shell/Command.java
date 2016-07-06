package org.neo4j.shell;

import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A shell command
 */
public interface Command {
    @Nonnull String getName();

    //Completer getCompleter();

    @Nonnull String getDescription();

    @Nonnull String getUsage();

    @Nonnull String getHelp();

    @Nonnull List<String> getAliases();

    void execute(@Nonnull final List<String> args) throws Exit.ExitException, CommandException;
}
