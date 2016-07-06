package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.CommandException;
import org.neo4j.shell.CommandHelper;
import org.neo4j.shell.CypherShell;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Help command, which prints help documentation.
 */
public class Help implements Command {
    public static final String COMMAND_NAME = ":help";
    private final CypherShell shell;

    public Help(@Nonnull final CypherShell shell) {
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
    public void execute(@Nonnull final List<String> args) throws CommandException {
        if (args.size() > 1) {
            throw new CommandException(
                    String.format(("Too many arguments. @|bold %s|@ accepts a single optional argument.\n"
                                    + "usage: @|bold %s|@ %s"),
                            COMMAND_NAME, COMMAND_NAME, getUsage()));
        } else if (args.isEmpty()) {
            printGeneralHelp();
        } else {
            printHelpFor(args.get(0));
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

        shell.printOut(String.format(("\nusage: @|bold %s|@ %s\n\n%s\n"),
                cmd.getName(), cmd.getUsage(), cmd.getHelp()));
    }

    private void printGeneralHelp() {
        CommandHelper commandHelper = shell.getCommandHelper();

        shell.printOut("\nAvailable commands:");

        for (Command cmd: commandHelper.getAllCommands()) {
            shell.printOut(String.format("  @|bold %s|@ %s", cmd.getName(), cmd.getDescription()));
        }

        shell.printOut("\nFor help on a specific command type:");
        shell.printOut(String.format("    %s @|bold command|@\n", COMMAND_NAME));
    }
}
