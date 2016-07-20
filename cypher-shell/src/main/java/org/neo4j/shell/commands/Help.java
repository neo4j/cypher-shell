package org.neo4j.shell.commands;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * Help command, which prints help documentation.
 */
public class Help implements Command {
    public static final String COMMAND_NAME = ":help";
    private final Logger logger;
    private final CommandHelper commandHelper;

    public Help(@Nonnull final Logger shell, @Nonnull final CommandHelper commandHelper) {
        this.logger = shell;
        this.commandHelper = commandHelper;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Nonnull
    @Override
    public String getDescription() {
        return "Show this help message";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "[command]";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Show the list of available commands or help for a specific command.";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return Arrays.asList(":man");
    }

    @Override
    public void execute(@Nonnull final String argString) throws CommandException {
        String[] args = simpleArgParse(argString, 0, 1, COMMAND_NAME, getUsage());
        if (args.length == 0) {
            printGeneralHelp();
        } else {
            printHelpFor(args[0]);
        }
    }

    private void printHelpFor(@Nonnull final String name) throws CommandException {
        Command cmd = commandHelper.getCommand(name);
        if (cmd == null && !name.startsWith(":")) {
            // Be friendly to users and don't force them to type colons for help if possible
            cmd = commandHelper.getCommand(":" + name);
        }

        if (cmd == null) {
            throw new CommandException(String.format("No such command: @|bold %s|@", name));
        }

        logger.printOut(String.format("\nusage: @|bold %s|@ %s\n\n%s\n",
                cmd.getName(), cmd.getUsage(), cmd.getHelp()));
    }

    private void printGeneralHelp() {
        logger.printOut("\nAvailable commands:");

        // Get longest command so we can align them nicely
        List<Command> allCommands = commandHelper.getAllCommands();

        int leftColWidth = longestCmdLength(allCommands);

        allCommands.stream().forEach(cmd -> {
            logger.printOut(String.format("  @|bold %-" + leftColWidth + "s|@ %s",
                    cmd.getName(), cmd.getDescription()));
        });

        logger.printOut("\nFor help on a specific command type:");
        logger.printOut(String.format("    %s @|bold command|@\n", COMMAND_NAME));
    }

    private int longestCmdLength(List<Command> allCommands) {
        String longestCommand = allCommands.stream()
                                        .map(Command::getName)
                                        .reduce("", (s1, s2) -> s1.length() > s2.length() ? s1 : s2);
        return longestCommand.length();
    }
}
