package org.neo4j.shell.commands;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

@FunctionalInterface
public interface CommandExecutable {
    void execute() throws CommandException, ExitException;
}
