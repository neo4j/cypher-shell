package org.neo4j.shell;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;
import jline.console.history.History;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

import static java.lang.System.getProperty;

/**
 * An interactive shell
 */
public class InteractiveShellRunner extends ShellRunner {
    private final ConsoleReader reader;
    private final Shell shell;
    private final FileHistory history;

    public InteractiveShellRunner(@Nonnull final Shell shell) throws IOException {
        super();
        this.shell = shell;
        this.reader = new ConsoleReader();
        this.history = setupHistory(this.reader, this.shell);
    }

    @Nonnull
    private FileHistory setupHistory(@Nonnull final ConsoleReader reader, @Nonnull final Shell shell) throws IOException {
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
            } catch (Exit.ExitException e) {
                throw e;
            } catch (ClientException e) {
                shell.printError(BoltHelper.getSensibleMsg(e));
            }
            catch (CommandException e) {
                shell.printError(e.getMessage());
            }
            catch (Throwable t) {
                // TODO: 6/21/16 Unknown errors maybe should be handled differently
                shell.printError(t.getMessage());
            }
        }
    }

    @Override
    @Nonnull
    public History getHistory() {
        return history;
    }

    private boolean work() throws IOException, Exit.ExitException, CommandException {
        String line = readLine();

        if (null == line) {
            return false;
        }

        if (!line.trim().isEmpty()) {
            shell.execute(line);
        }

        return true;
    }

    @Nullable
    private String readLine() throws IOException {
        return reader.readLine(shell.prompt(), shell.promptMask());
    }
}
