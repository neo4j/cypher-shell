package org.neo4j.shell.commands;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * Command to exit the logger. Equivalent to hitting Ctrl-D.
 */
public class Exit implements Command {
    public static final String COMMAND_NAME = ":exit";
    private final Logger logger;

    public Exit(@Nonnull final Logger logger) {
        this.logger = logger;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Exit the logger";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Exit the logger. Corresponds to entering @|bold CTRL-D|@.";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Arrays.asList(":quit");
    }

    @Override
    public void execute(@Nonnull final String argString) throws ExitException, CommandException {
        simpleArgParse(argString, 0, COMMAND_NAME, getUsage());

        logger.printOut("Exiting. Bye bye.");

        throw new ExitException(0);
    }

}
