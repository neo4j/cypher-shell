package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.CypherShell;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to connect to an instance of Neo4j.
 */
public class Disconnect implements Command {
    public static final String COMMAND_NAME = ":disconnect";

    private final CypherShell shell;

    public Disconnect(@Nonnull final CypherShell shell) {
        this.shell = shell;
    }

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

    @Nonnull
    @Override
    public String getHelp() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull List<String> args) throws Exit.ExitException, CommandException {
        shell.disconnect();
        System.out.println("Disconnected");
    }
}
