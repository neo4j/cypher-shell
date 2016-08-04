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
import org.neo4j.shell.util.Pair;

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
    private final static AnsiFormattedText freshPrompt = AnsiFormattedText.s().bold().append("neo4j> ");
    private final static AnsiFormattedText continuationPrompt = AnsiFormattedText.s().bold().append(".....> ");

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

    private static ConsoleReader setupConsoleReader(@Nonnull Logger logger,
                                                    @Nonnull InputStream inputStream) throws IOException {
        ConsoleReader reader = new ConsoleReader(inputStream, logger.getOutputStream());
        // Disable expansion of bangs: !
        reader.setExpandEvents(false);
        // Have JLine throw exception on Ctrl-C so one can abort search and stuff without quitting
        reader.setHandleUserInterrupt(true);

        return reader;
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

    @Nonnull
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
        boolean inMultiline = false;

        while (true) {
            String line = reader.readLine(getPrompt(sb).renderedString());
            if (line == null) {
                // User hit CTRL-D, or file ended
                throw new NoMoreInputException();
            }

            // If a newline is escaped, always add to string builder and wait for more input
            Pair<Boolean, String> withoutBackslash = lineWithoutEscapedNewline(line);
            line = withoutBackslash.getSecond();
            if (withoutBackslash.getFirst()) {
                sb.append(line);
                inMultiline = true;
                continue;
            }

            // There was no escaped newline, empty lines are ignored unless in multiline mode
            if (!inMultiline && line.trim().isEmpty()) {
                continue;
            }
            sb.append(line).append("\n");

            if (inMultiline &&
                    (line.trim().isEmpty() || line.trim().endsWith(";"))) {
                // An empty line or a semicolon in multiline mode is a signal to execute
                return statementParser.parse(sb.toString());
            } else if (!inMultiline) {
                // Try to execute the line directly if not in multiline mode
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    return statementParser.parse(sb.toString());
                } catch (IncompleteCypherError ignored) {
                    // The parser has detected an incomplete query, wait for more input
                    inMultiline = true;
                }
            }
        }
    }

    /**
     * @param sb if non-empty means multiline mode
     * @return a prompt string depending on if we currently in multiline mode
     */
    @Nonnull
    AnsiFormattedText getPrompt(@Nonnull StringBuilder sb) {
        return sb.length() == 0 ? freshPrompt : continuationPrompt;
    }

    /**
     * Parses a line for escaped newlines (trailing backslashes).
     * These newlines can be escaped (e.g. double backslash), in which case the method will de-duplicate
     * it for you (but still return false in that case).
     *
     * @param line to parse
     * @return a pair of (had escaped newline, line without escaped newline / line with escaped backslash de-duplicated)
     */
    static Pair<Boolean, String> lineWithoutEscapedNewline(@Nonnull String line) {
        final int lastBackSlashIndex = line.lastIndexOf('\\');

        // no backslash found
        if (lastBackSlashIndex < 0) {
            return new Pair<>(false, line);
        }

        // It only escapes stuff it it is the last character (except for whitespace)
        if (lastBackSlashIndex != line.length() - 1 &&
                !line.substring(lastBackSlashIndex + 1).trim().isEmpty()) {
            return new Pair<>(false, line);
        }

        // Make sure it is not an escaped backslash
        if (line.length() > 1 && line.charAt(lastBackSlashIndex - 1) == '\\') {
            if (lastBackSlashIndex == line.length() - 1) {
                return new Pair<>(false, line.substring(0, lastBackSlashIndex));
            } else {
                return new Pair<>(false,
                        line.substring(0, lastBackSlashIndex) + line.substring(lastBackSlashIndex + 1));
            }
        }

        return new Pair<>(true, line.substring(0, lastBackSlashIndex));
    }
}
