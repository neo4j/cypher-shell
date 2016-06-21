package org.neo4j.shell.commands;

import org.neo4j.shell.Command;

import java.util.List;

/**
 * Help command, which prints help documentation.
 */
public class Help implements Command {
    public static final String COMMAND_NAME = ":help";

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

    @Override
    public List getAliases() {
        return null;
    }

    @Override
    public Object execute(List<String> args) {
        return null;
    }
}
