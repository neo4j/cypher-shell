package org.neo4j.shell.cli;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.MemoryHistory;
import org.neo4j.shell.Historian;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.parser.CypherParser;
import org.neo4j.shell.parser.StatementParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.getProperty;

public class CommandReader implements Historian {
    private final static File DEFAULT_HISTORY_FILE = getDefaultHistoryFile();
    private final ConsoleReader reader;
    private final StatementParser parser;
    private FileHistory fileHistory;

    public CommandReader(@Nonnull Logger logger, final boolean useHistoryFile) throws IOException {
        this(System.in, logger, useHistoryFile);
    }

    public CommandReader(@Nonnull InputStream inputStream, @Nonnull Logger logger) throws IOException {
        this(inputStream, logger, false);
    }

    public CommandReader(@Nonnull InputStream inputStream, @Nonnull Logger logger, final boolean useHistoryFile)
            throws IOException {
        this(inputStream, logger, useHistoryFile ? DEFAULT_HISTORY_FILE : null);
    }

    public CommandReader(@Nonnull InputStream inputStream, @Nonnull Logger logger, @Nullable File historyFile)
            throws IOException {
        this(inputStream, logger, new StatementParser(new CypherParser()), historyFile);
    }

    public CommandReader(@Nonnull InputStream inputStream, @Nonnull Logger logger, @Nonnull StatementParser parser,
                         @Nullable File historyFile) throws IOException {
        this.parser = parser;
        reader = new ConsoleReader(inputStream, logger.getOutputStream());
        // Disable expansion of bangs: !
        reader.setExpandEvents(false);
        // Have JLine throw exception on Ctrl-C so one can abort search and stuff without quitting
        reader.setHandleUserInterrupt(true);
        if (historyFile != null) {
            setupHistoryFile(reader, logger, historyFile);
        }
    }

    @Nonnull
    public List<String> getHistory() {
        History history = reader.getHistory();
        List<String> result =  new ArrayList<>();

        history.forEach(entry -> result.add(String.valueOf(entry.value())));

        return result;
    }

    private void setupHistoryFile(@Nonnull final ConsoleReader reader,
                                  @Nonnull final Logger logger,
                                  @Nonnull final File historyFile) throws IOException {
        try {
            File dir = historyFile.getParentFile();
            if (!dir.isDirectory() && !dir.mkdir()) {
                throw new IOException("Failed to create directory for history: " + dir.getAbsolutePath());
            }
            final FileHistory history = new FileHistory(historyFile);
            this.fileHistory = history;
            reader.setHistory(history);

            // Make sure we flush history on exit
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        history.flush();
                    } catch (IOException e) {
                        logger.printError("Failed to save history:\n" + e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            logger.printError("Could not load history file. Falling back to session-based history.\n"
                    + e.getMessage());
            reader.setHistory(new MemoryHistory());
        }
    }

    @Nonnull
    static File getDefaultHistoryFile() {
        // Storing in same directory as driver uses
        File dir = new File(getProperty("user.home"), ".neo4j");
        return new File(dir, ".neo4j_history");
    }

    /**
     * Reads from the InputStream until a non-empty statement can be found.
     * Empty statements are either lines consisting of all whitespace, or comments (prefixed by //)
     * @return a command string, or null if EOF
     * @throws IOException
     */
    @Nullable
    public String readCommand() throws IOException {
        while (!parser.isStatementComplete()) {
            String line = reader.readLine(parser.getPrompt().renderedString());
            if (line == null) {
                return null;
            }

            parser.parseLine(line);
        }
        return parser.consumeStatement();
    }

    /**
     * Useful in tests only
     */
    void flushHistory() throws IOException {
        if (fileHistory != null) {
            fileHistory.flush();
        }
    }
}
