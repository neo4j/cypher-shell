package org.neo4j.shell.commands;

import org.neo4j.shell.VariableHolder;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * This lists all query parameters which have been set
 */
public class Params implements Command {
    public static final String COMMAND_NAME = ":params";
    private final Logger logger;
    private final VariableHolder variableHolder;

    public Params(@Nonnull Logger logger, @Nonnull VariableHolder variableHolder) {
        this.logger = logger;
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
        return "Prints all currently set query parameters and their values";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "[parameter]";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Print a table of all currently set query parameters or the value for the given parameter";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Arrays.asList(":parameters");
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        String[] args = simpleArgParse(argString, 0, 1, COMMAND_NAME, getUsage());

        if (args.length == 0) {
            listAllParams();
        } else {
            listParam(args[0]);
        }
    }

    private void listParam(@Nonnull String name) throws CommandException {
        if (!variableHolder.getAll().containsKey(name)) {
            throw new CommandException("Unknown parameter: " + name);
        }
        listParam(name.length(), name, variableHolder.getAll().get(name));
    }

    private void listParam(int leftColWidth, @Nonnull String key, @Nonnull Object value) {
        logger.printOut(String.format("%-" + leftColWidth + "s: %s", key, value));
    }

    private void listAllParams() {
        List<String> keys = variableHolder.getAll().keySet().stream().sorted().collect(Collectors.toList());

        int leftColWidth = getMaxLeftColumnWidth(keys);

        keys.stream().forEach(k -> listParam(leftColWidth, k, variableHolder.getAll().get(k)));
    }

    private static int getMaxLeftColumnWidth(List<String> keys) {
        return keys.stream().map(String::length).reduce(0, Math::max);
    }
}
