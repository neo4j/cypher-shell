package org.neo4j.shell;

import org.neo4j.shell.commands.*;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.DuplicateCommandException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Utility methods for dealing with commands
 */
public class CommandHelper {
    private final TreeMap<String, Command> commands = new TreeMap<>();

    public CommandHelper(@Nonnull final CypherShell cypherShell) {
        registerAllCommands(cypherShell);
    }

    public void registerAllCommands(@Nonnull final CypherShell cypherShell) {
        registerCommand(new Help(cypherShell));
        registerCommand(new Exit(cypherShell));
        registerCommand(new Connect(cypherShell));
        registerCommand(new Disconnect(cypherShell));
        registerCommand(new History(cypherShell));
        registerCommand(new Begin(cypherShell));
        registerCommand(new Commit(cypherShell));
        registerCommand(new Rollback(cypherShell));
        registerCommand(new Set(cypherShell));
        registerCommand(new Env(cypherShell));
    }

    private void registerCommand(@Nonnull final Command command) throws DuplicateCommandException {
        if (commands.containsKey(command.getName())) {
            throw new DuplicateCommandException("This command name has already been registered: " + command.getName());
        }

        commands.put(command.getName(), command);

        for (String alias: command.getAliases()) {
            if (commands.containsKey(alias)) {
                throw new DuplicateCommandException("This command alias has already been registered: " + alias);
            }
            commands.put(alias, command);
        }

    }

    /**
     * Get a command corresponding to the given name, or null if no such command has been registered.
     */
    @Nullable
    public Command getCommand(@Nonnull final String name) {
        if (commands.containsKey(name)) {
            return commands.get(name);
        }
        return null;
    }

    /**
     * Get a list of all registered commands
     */
    @Nonnull
    public Iterable<Command> getAllCommands() {
        return commands.values().stream().distinct().collect(Collectors.toList());
    }

    /**
     * Split an argument string on whitespace
     */
    @Nonnull
    public static String[] simpleArgParse(@Nonnull final String argString, int expectedCount,
                                          @Nonnull final String commandName, @Nonnull final String usage)
            throws CommandException {
        return simpleArgParse(argString, expectedCount, expectedCount, commandName, usage);
    }

    /**
     * Split an argument string on whitespace
     */
    @Nonnull
    public static String[] simpleArgParse(@Nonnull final String argString, int minCount, int maxCount,
                                          @Nonnull final String commandName, @Nonnull final String usage)
            throws CommandException {
        final String[] args;
        if (argString.trim().isEmpty()) {
            args = new String[] {};
        } else {
            args = argString.trim().split("\\s+");
        }

        if (args.length < minCount || args.length > maxCount) {
            throw new CommandException(
                    String.format(("Incorrect number of arguments.\nusage: @|bold %s|@ %s"),
                            commandName, usage));
        }

        return args;
    }
}
