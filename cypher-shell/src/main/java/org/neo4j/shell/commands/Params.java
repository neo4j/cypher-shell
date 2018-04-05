package org.neo4j.shell.commands;

import org.neo4j.shell.VariableHolder;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.prettyprint.CypherVariablesFormatter;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;
import static org.neo4j.shell.prettyprint.CypherVariablesFormatter.escape;

/**
 * This lists all query parameters which have been set
 */
public class Params implements Command {
    public static final String COMMAND_NAME = ":params";
    private final Logger logger;
    private final VariableHolder variableHolder;
    private static final Pattern backtickPattern = Pattern.compile("^\\s*(?<key>(`([^`])*`)+?)\\s*");

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
        String trim = argString.trim();
        Matcher matcher = backtickPattern.matcher(trim);
        if (trim.startsWith("`") && matcher.matches()) {
            listParam(trim);
        } else {
            String[] args = simpleArgParse(argString, 0, 1, COMMAND_NAME, getUsage());
            if (args.length > 0) {
                listParam(args[0]);
            } else {
                listAllParams();
            }
        }
    }

    private void listParam(@Nonnull String name) throws CommandException {
        String parameterName = CypherVariablesFormatter.unescapedCypherVariable(name);
        if (!this.variableHolder.getAllAsUserInput().containsKey(parameterName)) {
            throw new CommandException("Unknown parameter: " + name);
        }
        listParam(name.length(), name, this.variableHolder.getAllAsUserInput().get(parameterName).getKey());
    }

    private void listParam(int leftColWidth, @Nonnull String key, @Nonnull Object value) {
        logger.printOut(String.format(":param %-" + leftColWidth + "s => %s", key, value));
    }

    private void listAllParams() {
        List<String> keys = variableHolder.getAllAsUserInput().keySet().stream().sorted().collect(Collectors.toList());

        int leftColWidth = keys.stream().map((s) -> escape(s).length()).reduce(0, Math::max);

        keys.forEach(key -> listParam(leftColWidth, escape(key), variableHolder.getAllAsUserInput().get(key).getKey()));
    }
}
