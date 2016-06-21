package org.neo4j.shell;

import java.util.List;

/**
 * A shell command
 */
public interface Command {
    String getName();

    //String getShortcut();

    //Completer getCompleter();

    String getDescription();

    String getUsage();

    String getHelp();

    List/*<CommandAlias>*/ getAliases();

    Object execute(final List<String> args);

    //boolean getHidden();
}
