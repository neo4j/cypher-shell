package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.shell.CommandHelper.simpleArgParse;

/**
 * This command marks a transaction as failed and closes it.
 */
public class Rollback implements Command {
    public static final String COMMAND_NAME = ":rollback";
    private final Shell shell;

    public Rollback(@Nonnull final Shell shell) {
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
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        simpleArgParse(argString, 0, COMMAND_NAME, getUsage());

        if (!shell.isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }

        shell.rollbackTransaction();
    }
}
