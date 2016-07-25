package org.neo4j.shell.commands;

import org.neo4j.shell.Connector;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * Command to connect to an instance of Neo4j.
 */
public class Disconnect implements Command {
    public static final String COMMAND_NAME = ":disconnect";
    private final Logger logger;
    private final Connector connector;

    public Disconnect(@Nonnull final Logger logger, @Nonnull final Connector connector) {
        this.logger = logger;
        this.connector = connector;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Disconnect from neo4j";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Disconnect from neo4j without quitting the shell.";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        simpleArgParse(argString, 0, COMMAND_NAME, getUsage());

        connector.disconnect();
        logger.printOut("Disconnected");
    }
}
