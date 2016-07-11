package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.CypherShell;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This command marks a transaction as failed and closes it.
 */
public class Rollback implements Command {
    public static final String COMMAND_NAME = ":rollback";
    private final CypherShell shell;

    public Rollback(@Nonnull final CypherShell shell) {
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
        return "Rollback the currently open transaction";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Rolls back and closes the currently open transaction";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull List<String> args) throws ExitException, CommandException {
        if (!args.isEmpty()) {
            throw new CommandException(
                    String.format("Too many arguments. @|bold %s|@ does not accept any arguments",
                            COMMAND_NAME));
        }

        if (!shell.isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }

        if (!shell.getCurrentTransaction().isPresent()) {
            throw new CommandException("There is no open transaction to rollback");
        }

        shell.rollbackTransaction();
    }
}
