package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.VariableHolder;
import org.neo4j.shell.exception.CommandException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.shell.CommandHelper.simpleArgParse;

/**
 * This command clears a previously set variable, or does nothing in case it is already cleared.
 */
public class Unset implements Command {
    public static final String COMMAND_NAME = ":unset";
    private final VariableHolder variableHolder;

    public Unset(@Nonnull final VariableHolder variableHolder) {
        this.variableHolder = variableHolder;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Unset the value of a query parameter";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "name";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Clear the specified query parameter, or do nothing in case it is not set";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull final String argString) throws CommandException {
        String[] args = simpleArgParse(argString, 1, COMMAND_NAME, getUsage());

        variableHolder.remove(args[0]);
    }
}
