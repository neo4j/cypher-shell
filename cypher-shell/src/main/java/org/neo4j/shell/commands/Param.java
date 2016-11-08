package org.neo4j.shell.commands;

import org.neo4j.shell.VariableHolder;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.AnsiFormattedText;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This command sets a variable to a name, for use as query parameter.
 */
public class Param implements Command {
    // Match arguments such as "(key) (value with possible spaces)" where key and value are any strings
    private static final Pattern backtickPattern = Pattern.compile("^\\s*(?<key>(`([^`])*`)+?):?\\s+(?<value>.+)$");
    private static final Pattern argPattern = Pattern.compile("^\\s*(?<key>[\\p{L}_][\\p{L}0-9_]*):?\\s+(?<value>.+)$");

    public static final String COMMAND_NAME = ":param";
    private final VariableHolder variableHolder;

    public Param(@Nonnull final VariableHolder variableHolder) {
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
        return "Set the value of a query parameter";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "name value";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Set the specified query parameter to the value given";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(@Nonnull final String argString) throws CommandException {
        Matcher alphanumericMatcher = argPattern.matcher(argString);
        if (alphanumericMatcher.matches()) {
            variableHolder.set(alphanumericMatcher.group("key"), alphanumericMatcher.group("value"));
        } else {
            checkForBackticks(argString);
        }
    }

    private void checkForBackticks(@Nonnull String argString) throws CommandException {
        Matcher matcher = backtickPattern.matcher(argString);
        if (argString.trim().startsWith("`") && matcher.matches() && matcher.group("key").length() > 2) {
            variableHolder.set(matcher.group("key"), matcher.group("value"));
        } else {
            throw new CommandException(AnsiFormattedText.from("Incorrect number of arguments.\nusage: ")
                    .bold().append(COMMAND_NAME).boldOff().append(" ").append(getUsage()));
        }
    }
}
