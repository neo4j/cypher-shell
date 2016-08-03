package org.neo4j.shell.cli;

import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import org.neo4j.shell.Historian;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.CypherSyntaxError;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.exception.IncompleteCypherError;
import org.neo4j.shell.exception.NoMoreInputException;
import org.neo4j.shell.log.AnsiFormattedText;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.parser.StatementParser;

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
public class InteractiveShellRunner implements ShellRunner {
    private final static String freshPrompt = AnsiFormattedText.s().bold().append("neo4j> ").renderedString();
    private final static String continuationPrompt = AnsiFormattedText.s().bold().append("  ...> ").renderedString();
    private final Logger logger;
    private final ConsoleReader reader;
    private final Historian historian;
    private final StatementParser statementParser;

    public InteractiveShellRunner(@Nonnull Logger logger,
                                  @Nonnull StatementParser statementParser,
                                  @Nonnull InputStream inputStream,
                                  @Nonnull File historyFile) throws IOException {
        this.logger = logger;
        this.statementParser = statementParser;
        reader = setupConsoleReader(logger, inputStream);
        this.historian = FileHistorian.setupHistory(reader, logger, historyFile);
    }

    @Override
    public int runUntilEnd(@Nonnull StatementExecuter executer) {
        int exitCode = 0;
        boolean running = true;
        while (running) {
            try {
                for (String statement: readUntilStatement()) {
                    executer.execute(statement);
                }
            } catch (ExitException e) {
                exitCode = e.getCode();
                running = false;
            } catch (UserInterruptException e) {
                // Not very nice to print "UserInterruptException"
                logger.printError(AnsiFormattedText.s().colorRed().append("KeyboardInterrupt").formattedString());
            } catch (NoMoreInputException e) {
                // User pressed Ctrl-D and wants to exit
                running = false;
            } catch (Throwable e) {
                logger.printError(getFormattedMessage(e));
            }
        }
        return exitCode;
    }

    @Override
    public Historian getHistorian() {
        return historian;
    }

    /**
     * Reads from the InputStream until one or more statements can be found.
     * @return a list of command statements
     * @throws IOException
     * @throws NoMoreInputException
     * @throws CypherSyntaxError
     */
    @Nonnull
    public List<String> readUntilStatement() throws IOException, NoMoreInputException, CypherSyntaxError {
        StringBuilder sb = new StringBuilder();

        while (true) {
            String line = reader.readLine(sb.length() == 0 ? freshPrompt : continuationPrompt);
            if (line == null) {
                // User hit CTRL-D, or file ended
                throw new NoMoreInputException();
            }
            if (sb.length() == 0 && line.trim().isEmpty()) {
                // Ignore empty lines when nothing has been parsed yet
                continue;
            }
            sb.append(line).append("\n");
            try {
                return statementParser.parse(sb.toString());
            } catch (IncompleteCypherError ignored) {
                // Try to read more lines
            }
        }
    }

    private static ConsoleReader setupConsoleReader(@Nonnull Logger logger,
                                                    @Nonnull InputStream inputStream) throws IOException {
        ConsoleReader reader = new ConsoleReader(inputStream, logger.getOutputStream());
        // Disable expansion of bangs: !
        reader.setExpandEvents(false);
        // Have JLine throw exception on Ctrl-C so one can abort search and stuff without quitting
        reader.setHandleUserInterrupt(true);

        return reader;
    }
}
