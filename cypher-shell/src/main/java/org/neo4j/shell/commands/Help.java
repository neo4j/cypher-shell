package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.CommandHelper;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static org.neo4j.shell.CommandHelper.simpleArgParse;

/**
 * Help command, which prints help documentation.
 */
public class Help implements Command {
    public static final String COMMAND_NAME = ":help";
    private final Shell shell;

    public Help(@Nonnull final Shell shell) {
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
        CommandHelper commandHelper = shell.getCommandHelper();

        Command cmd = commandHelper.getCommand(name);
        if (cmd == null && !name.startsWith(":")) {
            // Be friendly to users and don't force them to type colons for help if possible
            cmd = commandHelper.getCommand(":" + name);
        }

        if (cmd == null) {
            throw new CommandException(String.format("No such command: @|bold %s|@", name));
        }

        shell.printOut(String.format("\nusage: @|bold %s|@ %s\n\n%s\n",
                cmd.getName(), cmd.getUsage(), cmd.getHelp()));
    }

    private void printGeneralHelp() {
        CommandHelper commandHelper = shell.getCommandHelper();

        shell.printOut("\nAvailable commands:");

        // Get longest command so we can align them nicely
        List<Command> allCommands = commandHelper.getAllCommands();

        int leftColWidth = longestCmdLength(allCommands);

        allCommands.stream().forEach(cmd -> {
            shell.printOut(String.format("  @|bold %-" + leftColWidth + "s|@ %s",
                    cmd.getName(), cmd.getDescription()));
        });

        shell.printOut("\nFor help on a specific command type:");
        shell.printOut(String.format("    %s @|bold command|@\n", COMMAND_NAME));
    }

    private int longestCmdLength(List<Command> allCommands) {
        String longestCommand = allCommands.stream()
                                        .map(Command::getName)
                                        .reduce("", (s1, s2) -> s1.length() > s2.length() ? s1 : s2);
        return longestCommand.length();
    }
}
