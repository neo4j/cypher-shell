package org.neo4j.shell;

import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A shell command
 */
public interface Command {
    @Nonnull String getName();

    //String getShortcut();

    //Completer getCompleter();

    String getDescription();

    String getUsage();

    @Nonnull String getHelp();

    @Nonnull List<String> getAliases();

    Object execute(@Nonnull final List<String> args) throws Exit.ExitException;

    //boolean getHidden();
}
