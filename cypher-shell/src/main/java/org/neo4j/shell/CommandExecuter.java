package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;

/**
 * An interface which executes commands
 */
public interface CommandExecuter {

    void execute(@Nonnull String command) throws ExitException, CommandException;
}
