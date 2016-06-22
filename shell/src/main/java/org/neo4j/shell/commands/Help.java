package org.neo4j.shell.commands;

import org.neo4j.shell.Command;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Help command, which prints help documentation.
 */
public class Help implements Command {
    public static final String COMMAND_NAME = ":help";

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Arrays.asList(":man");
    }

    @Override
    public Object execute(@Nonnull List<String> args) {
        System.out.println("HELP I AM BEING ATTACKED");

        return null;
    }
}
