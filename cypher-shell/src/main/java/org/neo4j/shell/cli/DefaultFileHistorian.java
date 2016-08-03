package org.neo4j.shell.cli;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.MemoryHistory;
import org.neo4j.shell.Historian;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.getProperty;

/**
 * An historian which stores history in a file in the users home dir. The setup methods install a shutdown hook which
 * will flush the history on exit.
 */
public class DefaultFileHistorian implements Historian {

    private final MemoryHistory history;

    private DefaultFileHistorian(MemoryHistory history) {
        this.history = history;
    }

    @Nonnull
    public static Historian setupHistory(@Nonnull final ConsoleReader reader,
                                         @Nonnull final Logger logger) throws IOException {
        return setupHistory(reader, logger, getDefaultHistoryFile());
    }

    @Nonnull
    private static Historian setupHistory(@Nonnull final ConsoleReader reader,
                                          @Nonnull final Logger logger,
                                          @Nonnull final File historyFile) throws IOException {
        try {
            File dir = historyFile.getParentFile();
            if (!dir.isDirectory() && !dir.mkdir()) {
                throw new IOException("Failed to create directory for history: " + dir.getAbsolutePath());
            }
            final FileHistory history = new FileHistory(historyFile);
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

            return new DefaultFileHistorian(history);
        } catch (IOException e) {
            logger.printError("Could not load history file. Falling back to session-based history.\n"
                    + e.getMessage());
            MemoryHistory history = new MemoryHistory();
            reader.setHistory(history);
            return new DefaultFileHistorian(history);
        }
    }

    @Nonnull
    private static File getDefaultHistoryFile() {
        // Storing in same directory as driver uses
        File dir = new File(getProperty("user.home"), ".neo4j");
        return new File(dir, ".neo4j_history");
    }

    @Nonnull
    @Override
    public List<String> getHistory() {
        List<String> result =  new ArrayList<>();

        history.forEach(entry -> result.add(String.valueOf(entry.value())));

        return result;
    }
}
