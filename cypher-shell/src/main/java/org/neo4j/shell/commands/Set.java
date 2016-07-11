package org.neo4j.shell.commands;

import org.neo4j.driver.v1.Record;
import org.neo4j.shell.Command;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.exception.CommandException;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This command sets a variable to a name, for use as query parameter.
 */
public class Set implements Command {
    // Match arguments such as "(key) (value with possible spaces)" where key and value are any strings
    private static final Pattern argPattern = Pattern.compile("^\\s*(?<key>[^\\s]+)\\s+(?<value>.+)$");
    public static final String COMMAND_NAME = ":set";
    private final CypherShell shell;

    public Set(@Nonnull final CypherShell shell) {
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
        return Arrays.asList(":export");
    }

    @Override
    public void execute(@Nonnull final String argString) throws CommandException {
        Matcher m = argPattern.matcher(argString);

        if (!m.matches()) {
            throw new CommandException(
                    String.format(("Incorrect number of arguments.\nusage: @|bold %s|@ %s"),
                            COMMAND_NAME, getUsage()));
        }

        if (!shell.isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }

        String name = m.group("key");
        String valueString = m.group("value");

        Record record = shell.doCypherSilently("RETURN " + valueString + " as " + name).single();
        shell.getQueryParams().put(name, record.get(name).asObject());
    }

}
