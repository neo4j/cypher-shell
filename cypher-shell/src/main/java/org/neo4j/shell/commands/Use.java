package org.neo4j.shell.commands;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.neo4j.shell.DatabaseManager;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * This command starts a transaction.
 */
public class Use implements Command {
    private static final String COMMAND_NAME = ":use";
    @Nonnull private final DatabaseManager databaseManager;
    @Nonnull private String databaseName;

    public Use(@Nonnull final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Set the active database";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "database";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return String.format("Set the active database that transactions are executed on", Commit.COMMAND_NAME);
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        String[] args = simpleArgParse(argString, 1, COMMAND_NAME, getUsage());
        databaseManager.setActiveDatabase(args[0]);
    }
}
