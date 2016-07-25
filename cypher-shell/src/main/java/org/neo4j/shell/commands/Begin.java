package org.neo4j.shell.commands;

import org.neo4j.shell.TransactionHandler;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * This command starts a transaction.
 */
public class Begin implements Command {
    private static final String COMMAND_NAME = ":begin";
    private final TransactionHandler transactionHandler;

    public Begin(@Nonnull final TransactionHandler transactionHandler) {
        this.transactionHandler = transactionHandler;
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
        return String.format("Start a transaction which will remain open until %s or %s is called",
                Commit.COMMAND_NAME, Rollback.COMMAND_NAME);
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        simpleArgParse(argString, 0, COMMAND_NAME, getUsage());

        transactionHandler.beginTransaction();
    }
}
