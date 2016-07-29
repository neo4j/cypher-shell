package org.neo4j.shell.cli;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.MemoryHistory;
import org.fusesource.jansi.Ansi;
import org.neo4j.shell.Historian;
import org.neo4j.shell.log.AnsiFormattedText;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.getProperty;

public class CommandReader implements Historian {
    private final static File DEFAULT_HISTORY_FILE = getDefaultHistoryFile();
    private final ConsoleReader reader;
    //Pattern matches a back slash at the end of the line for multiline commands
    static final Pattern MULTILINE_BREAK = Pattern.compile("\\\\\\s*$");
    //Pattern matches comments
    static final Pattern COMMENTS = Pattern.compile("//.*$");
    private final String prompt = Ansi.ansi().render(AnsiFormattedText.s().bold().append("neo4j> ")
                                                                      .formattedString()).toString();
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

    public CommandReader(@Nonnull InputStream inputStream, @Nonnull Logger logger,
                         @Nullable File historyFile) throws IOException {
        reader = new ConsoleReader(inputStream, logger.getOutputStream());
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
        StringBuilder stringBuilder = new StringBuilder();
        boolean reading = true;
        while (reading) {
            String line = reader.readLine(prompt);
            if (line == null) {
                reading = false;
                if (stringBuilder.length() == 0) {
                    return null;
                }
            } else {
                String withoutComments = commentSubstitutedLine(line);
                Matcher m = MULTILINE_BREAK.matcher(withoutComments);
                boolean isMultiline = m.find();
                String parsedString = m.replaceAll("");

                if (!parsedString.trim().isEmpty()) {
                    stringBuilder.append(parsedString).append("\n");
                }
                if (!isMultiline && stringBuilder.length() > 0) {
                    reading = false;
                }
            }
        }
        return stringBuilder.toString();
    }

    private String commentSubstitutedLine(String line) {
        Matcher commentsMatcher = COMMENTS.matcher(line);
        return commentsMatcher.replaceAll("");
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
