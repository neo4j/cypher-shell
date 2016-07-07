package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.CypherShell;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This command starts a transaction.
 */
public class Begin implements Command {
    private static final String COMMAND_NAME = ":begin";
    private final CypherShell shell;

    public Begin(@Nonnull final CypherShell shell) {
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
        return "Open a transaction";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Start a transaction which will be open until combla or rollbla is called";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull List<String> args) throws Exit.ExitException, CommandException {
        if (!args.isEmpty()) {
            throw new CommandException(
                    String.format(("Too many arguments. @|bold %s|@ does not accepts any arguments"),
                            COMMAND_NAME));
        }

        if (!shell.isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }

        if (shell.getCurrentTransaction().isPresent()) {
            throw new CommandException("There is already an open transaction");
        }

        shell.beginTransaction();
    }
}
