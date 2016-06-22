package org.neo4j.shell;

import org.neo4j.shell.commands.Exit;

@FunctionalInterface
public interface CommandExecutable<T> {
    T execute() throws CommandException, Exit.ExitException;
}
