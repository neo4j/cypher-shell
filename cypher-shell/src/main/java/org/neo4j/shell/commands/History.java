package org.neo4j.shell.commands;

import org.neo4j.shell.Command;
import org.neo4j.shell.Shell;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.neo4j.shell.CommandHelper.simpleArgParse;

/**
 * Show command history
 */
public class History implements Command {
    private static final String COMMAND_NAME = ":history";

    private final Shell shell;
    private final List<String> aliases = new ArrayList<>();

    public History(@Nonnull final Shell shell) {
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
        return "Print a list of the last commands executed";
    }

    @Nonnull
    @Override
    public String getUsage() {
        return "";
    }

    @Nonnull
    @Override
    public String getHelp() {
        return "Prints a list of the last commands executed.";
    }

    @Nonnull
    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public void execute(@Nonnull String argString) throws ExitException, CommandException {
        simpleArgParse(argString, 0, COMMAND_NAME, getUsage());

        Optional<jline.console.history.History> possibleHistory = shell.getHistory();

        if (!possibleHistory.isPresent()) {
            // Nothing to print
            return;
        }

        jline.console.history.History history = possibleHistory.get();


        // Calculate starting position
        int lineCount = 16;

        shell.printOut(printHistory(history, lineCount));
    }

    /**
     * Prints N last lines of history.
     *
     * @param lineCount number of entries to print
     */
    private String printHistory(@Nonnull final jline.console.history.History history, final int lineCount) {
        // for alignment
        int colWidth = Integer.toString(history.size()).length();
        String fmt = " %-" + colWidth + "d  %s\n";

        String result = "";
        int count = 0;

        for (int i = history.size() - 1; i >= 0 && count < lineCount; i--, count++) {
            String line = String.valueOf(history.get(i));
            // Executing old commands with !N actually starts from 1, and not 0, hence increment index by one
            result = String.format(fmt, i + 1, line) + result;
        }

        return result;
    }
}
