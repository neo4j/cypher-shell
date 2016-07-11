package org.neo4j.shell;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;
import jline.console.history.MemoryHistory;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

import static java.lang.System.getProperty;

/**
 * An interactive shell
 */
public class InteractiveShellRunner extends ShellRunner {
    private final Shell shell;
    private final MemoryHistory history;
    private final CommandReader commandReader;

    public InteractiveShellRunner(@Nonnull final Shell shell) throws IOException {
        super();
        this.shell = shell;
        ConsoleReader reader = new ConsoleReader(shell.getInputStream(), shell.getOutputStream());
        this.history = setupHistory(reader, this.shell);
        this.commandReader = new CommandReader(reader, this.shell);
    }

    @Nonnull
    private MemoryHistory setupHistory(@Nonnull final ConsoleReader reader, @Nonnull final Shell shell) throws IOException {
        try {
            final FileHistory history = new FileHistory(getHistoryFile());
            history.setIgnoreDuplicates(true);
            reader.setHistory(history);

            // Make sure we flush history on exit
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        history.flush();
                    } catch (IOException e) {
                        shell.printError("Failed to save history:\n" + e.getMessage());
                    }
                }
            });

            return history;
        } catch (IOException e) {
            shell.printError("Could not load history file. Falling back to session-based history.\n"
                    + e.getMessage());
            MemoryHistory history = new MemoryHistory();
            history.setIgnoreDuplicates(true);
            return history;
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

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            try {
                running = work();
            } catch (ExitException e) {
                throw e;
            } catch (ClientException e) {
                shell.printError(BoltHelper.getSensibleMsg(e));
            } catch (CommandException e) {
                shell.printError(e.getMessage());
            } catch (Throwable t) {
                // TODO: 6/21/16 Unknown errors maybe should be handled differently
                shell.printError(t.getMessage());
            }
        }
    }

    @Override
    @Nullable
    public History getHistory() {
        return history;
    }

    private boolean work() throws IOException, ExitException, CommandException {
        String line = commandReader.readCommand();

        if (null == line) {
            return false;
        }

        if (!line.trim().isEmpty()) {
            shell.executeLine(line);
        }

        return true;
    }
}
