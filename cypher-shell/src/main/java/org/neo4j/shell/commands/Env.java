package org.neo4j.shell.commands;

import org.neo4j.shell.VariableHolder;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * This lists all query parameters which have been set
 */
public class Env implements Command {
    public static final String COMMAND_NAME = ":env";
    private final Logger logger;
    private final VariableHolder variableHolder;

    public Env(@Nonnull Logger logger, @Nonnull VariableHolder variableHolder) {
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
        return "Prints all variables and their values";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Print a table of all currently set variables";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        simpleArgParse(argString, 0, COMMAND_NAME, getUsage());

        List<String> keys = variableHolder.getAll().keySet().stream().sorted().collect(Collectors.toList());

        int leftColWidth = getMaxLeftColumnWidth(keys);

        keys.stream().forEach(k -> logger.printOut(String.format("%-" + leftColWidth + "s: %s", k,
                variableHolder.getAll().get(k))));
    }

    private static int getMaxLeftColumnWidth(List<String> keys) {
        String reduce = keys.stream().reduce("", (s1, s2) -> s1.length() > s2.length() ? s1 : s2);
        return reduce.length();
    }
}
