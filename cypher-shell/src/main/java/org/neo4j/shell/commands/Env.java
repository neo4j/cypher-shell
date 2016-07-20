package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static org.neo4j.shell.CommandHelper.simpleArgParse;

/**
 * This lists all query parameters which have been set
 */
public class Env implements Command {
    public static final String COMMAND_NAME = ":env";
    private final Shell shell;

    public Env(@Nonnull final Shell shell) {
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
        return Collections.EMPTY_LIST;
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        simpleArgParse(argString, 0, COMMAND_NAME, getUsage());

        List<String> keys = shell.getQueryParams().keySet().stream().sorted().collect(Collectors.toList());

        int leftColWidth = getMaxLeftColumnWidth(keys);

        keys.stream().forEach(k -> {
            shell.printOut(String.format("%-" + leftColWidth + "s: %s", k, shell.getQueryParams().get(k)));
        });
    }

    private static int getMaxLeftColumnWidth(List<String> keys) {
        String reduce = keys.stream().reduce("", (s1, s2) -> s1.length() > s2.length() ? s1 : s2);
        return reduce.length();
    }
}
