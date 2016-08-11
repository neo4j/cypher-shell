package org.neo4j.shell.cli;

import jline.console.ConsoleReader;
import org.neo4j.shell.Historian;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.exception.NoMoreInputException;
import org.neo4j.shell.log.AnsiFormattedText;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.parser.StatementParser;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.neo4j.shell.exception.Helper.getFormattedMessage;

/**
 * A shell runner intended for interactive sessions where lines are input one by one and execution should happen
 * along the way.
 */
public class InteractiveShellRunner implements ShellRunner, SignalHandler {
    private final static AnsiFormattedText freshPrompt = AnsiFormattedText.s().bold().append("neo4j> ");
    private final static AnsiFormattedText continuationPrompt = AnsiFormattedText.s().bold().append(".....> ");
    static final String INTERRUPT_SIGNAL = "INT";

    private final Logger logger;
    private final ConsoleReader reader;
    private final Historian historian;
    private final StatementParser statementParser;
    private StatementExecuter executer;

    public InteractiveShellRunner(@Nonnull StatementExecuter executer,
                                  @Nonnull Logger logger,
                                  @Nonnull StatementParser statementParser,
                                  @Nonnull InputStream inputStream,
                                  @Nonnull File historyFile) throws IOException {
        this.executer = executer;
        this.logger = logger;
        this.statementParser = statementParser;
        this.reader = setupConsoleReader(logger, inputStream);
        this.historian = FileHistorian.setupHistory(reader, logger, historyFile);

        // Catch ctrl-c
        Signal.handle(new Signal(INTERRUPT_SIGNAL), this);
    }

    private ConsoleReader setupConsoleReader(@Nonnull Logger logger,
                                             @Nonnull InputStream inputStream) throws IOException {
        ConsoleReader reader = new ConsoleReader(inputStream, logger.getOutputStream());
        // Disable expansion of bangs: !
        reader.setExpandEvents(false);
        // Ensure Reader does not handle user input for ctrl+C behaviour
        reader.setHandleUserInterrupt(false);
        return reader;
    }

    @Override
    public int runUntilEnd() {
        int exitCode = 0;
        boolean running = true;
        while (running) {
            try {
                for (String statement : readUntilStatement()) {
                    executer.execute(statement);
                }
            } catch (ExitException e) {
                exitCode = e.getCode();
                running = false;
            } catch (NoMoreInputException e) {
                // User pressed Ctrl-D and wants to exit
                running = false;
            } catch (Throwable e) {
                logger.printError(getFormattedMessage(e));
            }
        }
        return exitCode;
    }

    @Nonnull
    @Override
    public Historian getHistorian() {
        return historian;
    }

    /**
     * Reads from the InputStream until one or more statements can be found.
     *
     * @return a list of command statements
     * @throws IOException
     * @throws NoMoreInputException
     */
    @Nonnull
    public List<String> readUntilStatement() throws IOException, NoMoreInputException {
        while (true) {
            String line = reader.readLine(getPrompt().renderedString());
            if (line == null) {
                // User hit CTRL-D, or file ended
                throw new NoMoreInputException();
            }

            // Empty lines are ignored if nothing has been read yet
            if (line.trim().isEmpty() && !statementParser.containsText()) {
                continue;
            }

            statementParser.parseMoreText(line + "\n");

            if (statementParser.hasStatements()) {
                return statementParser.consumeStatements();
            }
        }
    }

    /**
     * @return suitable prompt depending on current parsing state
     */
    AnsiFormattedText getPrompt() {
        return statementParser.containsText() ? continuationPrompt : freshPrompt;
    }

    /**
     * Catch Ctrl-C from user and handle it nicely
     *
     * @param signal to handle
     */
    @Override
    public void handle(final Signal signal) {
        // Stop any running cypher statements
        if (executer != null) {
            executer.reset();
        }
        // Print a literal newline here to get around us being in the middle of the prompt
        logger.printError(AnsiFormattedText.s().colorRed().append("\nKeyboardInterrupt").formattedString());
        // Clear any text which has been inputted
        resetPrompt();
    }

    /**
     * Clears the prompt of any text which has been inputted and redraws it.
     */
    private void resetPrompt() {
        try {
            // Clear whatever text has currently been inputted
            boolean more = true;
            while (more) {
                more = reader.delete();
            }
            more = true;
            while (more) {
                more = reader.backspace();
            }
            // Clear parser state
            statementParser.reset();

            // Redraw the prompt now because the error message has changed the terminal text
            reader.setPrompt(getPrompt().renderedString());
            reader.redrawLine();
            reader.flush();
        } catch (IOException e) {
            logger.printError(getFormattedMessage(e));
        }
    }
}
