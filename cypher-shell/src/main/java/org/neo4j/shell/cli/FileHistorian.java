package org.neo4j.shell.cli;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.MemoryHistory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import org.neo4j.shell.Historian;
import org.neo4j.shell.log.Logger;

import static java.lang.String.format;
import static java.lang.System.getProperty;

/**
 * An historian which stores history in a file in the users home dir. The setup methods install a shutdown hook which
 * will flush the history on exit.
 */
public class FileHistorian implements Historian {

    private final MemoryHistory history;

    private FileHistorian(MemoryHistory history) {
        this.history = history;
    }

    @Nonnull
    public static Historian setupHistory(@Nonnull final ConsoleReader reader,
                                  @Nonnull final Logger logger,
                                  @Nonnull final File historyFile) {
        try {
            File dir = historyFile.getParentFile();
            if (!dir.isDirectory() && !dir.mkdir()) {
                throw new IOException("Failed to create directory for history: " + dir.getAbsolutePath());
            }
            final FileHistory history = new FileHistory(historyFile);
            reader.setHistory(history);

            // Make sure we flush history on exit
            addShutdownHookToFlushHistory(logger, history);

            return new FileHistorian(history);
        } catch (IOException e) {
            logger.printError(format("Could not load history file. Falling back to session-based history.%n%s", e.getMessage()));
            MemoryHistory history = new MemoryHistory();
            reader.setHistory(history);
            return new FileHistorian(history);
        }
    }

    private static void addShutdownHookToFlushHistory(@Nonnull final Logger logger, final FileHistory history) {
        Runtime.getRuntime().addShutdownHook( new Thread( () -> {
            try {
                history.flush();
            } catch (IOException e) {
                logger.printError(format("Failed to save history:%n%s", e.getMessage()));
            }
        } ) );
    }

    @Nonnull
    public static File getDefaultHistoryFile() {
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

    @Override
    public void flushHistory() throws IOException {
        if (history instanceof FileHistory) {
            ((FileHistory) history).flush();
        }
    }
}
