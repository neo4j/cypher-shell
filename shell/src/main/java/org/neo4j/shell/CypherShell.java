package org.neo4j.shell;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiRenderer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;
import static org.neo4j.shell.CommandHelper.registerCommands;

/**
 * An interactive shell for evaluating cypher statements.
 */
public class CypherShell {

    private final List<Command> commands;

    CypherShell() {
        commands = new ArrayList<>();

        registerCommands(commands, this);
    }

    int run() {
        int exitCode;

        try {
            InteractiveShellRunner runner = new InteractiveShellRunner(this, this::renderPrompt);

            runner.run();

            exitCode = 0;
        } catch (Throwable t) {
            // TODO: 6/21/16 Print to error log
            exitCode = 1;
        }

        return exitCode;
    }

    @Nonnull
    private String renderPrompt() {
        return AnsiRenderer.render(buildPrompt());
    }

    @Nonnull
    private String buildPrompt() {
        // TODO: 6/21/16 Line number

        return "@|bold cypher:|@1@|bold >|@ ";
    }

    void execute(@Nonnull final String line) {
        // TODO: 6/21/16 handle command

        // See if it's a shell command
        CommandExecutable<Object> cmd = getCommandExecutable(line);
        if (cmd != null) {
            executeCmd(cmd);
            return;
        }

        // Else it will be parsed as Cypher
        System.out.println("Executing: " + line);
    }

    @Nullable
    private CommandExecutable<Object> getCommandExecutable(@Nonnull final String line) {
        String[] parts = line.trim().split("\\s");

        if (parts.length < 1) {
            return null;
        }

        String name = parts[0];

        Command cmd = getCommand(name);

        if (cmd != null) {
            List<String> args = Arrays.asList(parts);
            args.remove(0);
            return () -> cmd.execute(args);
        }

        return null;
    }

    @Nullable
    private Command getCommand(@Nonnull final String name) {
        for (Command command: commands) {
            if (name.equals(command.getName())) {
                return command;
            }
        }
        return null;
    }

    private void executeCmd(@Nonnull final CommandExecutable<Object> cmdExe) {
        try {
            Object result = cmdExe.execute();
        } catch (CommandException e) {
            System.err.println(ansi().a(Ansi.Attribute.INTENSITY_BOLD).fgRed().a(e.getMessage()).reset());
        }
    }
}
