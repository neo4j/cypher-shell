package org.neo4j.shell.cli;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.MemoryHistory;
import org.fusesource.jansi.AnsiRenderer;
import org.neo4j.shell.Historian;
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
    private final ConsoleReader reader;
    //Pattern matches a back slash at the end of the line for multiline commands
    static final Pattern MULTILINE_BREAK = Pattern.compile("\\\\\\s*$");
    //Pattern matches comments
    static final Pattern COMMENTS = Pattern.compile("//.*$");
    private final String prompt = AnsiRenderer.render("@|bold neo4j>|@ ");

    public CommandReader(@Nonnull Logger logger) throws IOException {
        this(System.in, logger);
    }

    public CommandReader(@Nonnull InputStream inputStream, @Nonnull Logger logger) throws IOException {
        this(inputStream, logger, true);
    }

    public CommandReader(@Nonnull InputStream inputStream, @Nonnull Logger logger, final boolean useHistory)
            throws IOException {
        reader = new ConsoleReader(inputStream, logger.getOutputStream());
        if (useHistory) {
            setupHistory(reader, logger);
        }
    }

    @Nonnull
    public List<String> getHistory() {
        History history = reader.getHistory();
        List<String> result =  new ArrayList<>();
        if (history == null) {
            return result;
        }

        history.forEach(entry -> result.add(String.valueOf(entry)));

        return result;
    }

    private void setupHistory(@Nonnull final ConsoleReader reader, @Nonnull final Logger logger) throws IOException {
        try {
            final FileHistory history = new FileHistory(getHistoryFile());
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
    private File getHistoryFile() throws IOException {
        // Storing in same directory as driver uses
        File dir = new File(getProperty("user.home"), ".neo4j");
        if (!dir.isDirectory() && !dir.mkdir()) {
            throw new IOException("Failed to create directory for history: " + dir.getAbsolutePath());
        }
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
        StringBuffer stringBuffer = new StringBuffer();
        boolean reading = true;
        while (reading) {
            String line = reader.readLine(prompt);
            if (line == null) {
                reading = false;
                if (stringBuffer.length() == 0) {
                    return null;
                }
            } else {
                String withoutComments = commentSubstitutedLine(line);
                Matcher m = MULTILINE_BREAK.matcher(withoutComments);
                boolean isMultiline = m.find();
                String parsedString = m.replaceAll("");

                if (!parsedString.trim().isEmpty()) {
                    stringBuffer.append(parsedString).append("\n");
                }
                if (!isMultiline && stringBuffer.length() > 0) {
                    reading = false;
                }
            }
        }
        return stringBuffer.toString();
    }

    private String commentSubstitutedLine(String line) {
        Matcher commentsMatcher = COMMENTS.matcher(line);
        return commentsMatcher.replaceAll("");
    }
}
