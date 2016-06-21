package org.neo4j.shell;

@FunctionalInterface
public interface CommandExecutable<T> {
    T execute() throws CommandException;
}
