package org.neo4j.shell.commands;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.neo4j.shell.Historian;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import static java.lang.String.format;
import static org.neo4j.shell.commands.CommandHelper.simpleArgParse;

/**
 * Show command history
 */
public class History implements Command {
    private static final String COMMAND_NAME = ":history";

    private final Logger logger;
    private final Historian historian;
    private final List<String> aliases = new ArrayList<>();

    public History(@Nonnull final Logger logger, @Nonnull final Historian historian) {
        this.logger = logger;
        this.historian = historian;
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

        // Calculate starting position
        int lineCount = 16;

        logger.printOut(printHistory(historian.getHistory(), lineCount));
    }

    /**
     * Prints N last lines of history.
     *
     * @param lineCount number of entries to print
     */
    private String printHistory(@Nonnull final List<String> history, final int lineCount) {
        // for alignment, check the string length of history size
        int colWidth = Integer.toString(history.size()).length();
        String fmt = " %-" + colWidth + "d  %s%n";

        StringBuilder result = new StringBuilder();
        int count = 0;

        for (int i = history.size() - 1; i >= 0 && count < lineCount; i--, count++) {
            String line = history.get(i);
            // Executing old commands with !N actually starts from 1, and not 0, hence increment index by one
            result.insert( 0, format( fmt, i + 1, line ) );
        }

        return result.toString();
    }
}
