package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.CypherShell;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This lists all query parameters which have been set
 */
public class Env implements Command {
    public static final String COMMAND_NAME = ":env";
    private final CypherShell shell;

    public Env(@Nonnull final CypherShell shell) {
        this.shell = shell;
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
        return Arrays.asList();
    }

    @Override
    public void execute(@Nonnull final List<String> args) throws CommandException {
        if (args.size() > 0) {
            throw new CommandException(
                    String.format(("Incorrect number of arguments. @|bold %s|@ accepts no arguments.\n"
                                    + "usage: @|bold %s|@ %s"),
                            COMMAND_NAME, COMMAND_NAME, getUsage()));
        }

        List<String> keys = shell.getQueryParams().keySet().stream().collect(Collectors.toList());

        Collections.sort(keys);

        int leftColWidth = 0;
        // Get longest name for alignment
        for (String k: keys) {
            if (k.length() > leftColWidth) {
                leftColWidth = k.length();
            }
        }

        for (String k: keys) {
            shell.printOut(String.format("%-" + leftColWidth + "s: %s", k, shell.getQueryParams().get(k)));
        }
    }
}
