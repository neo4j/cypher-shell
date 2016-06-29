package org.neo4j.shell;

import jline.console.ConsoleReader;
import jline.console.history.History;
import org.neo4j.shell.commands.Exit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * A shell runner which reads from STDIN and executes commands until completion.
 */
public class NonInteractiveShellRunner extends ShellRunner {

    private final Shell shell;
    private final ConsoleReader reader;

    public NonInteractiveShellRunner(@Nonnull Shell shell) throws IOException {
        super();
        this.shell = shell;
        this.reader = new ConsoleReader();
    }

    @Override
    public void run() throws CommandException, IOException {
        String line;
        boolean running = true;
        while (running) {
            line = reader.readLine();

            try {
                if (null == line) {
                    running = false;
                    continue;
                }

                if (!line.trim().isEmpty()) {
                    shell.execute(line);
                }
            } catch (Exit.ExitException e) {
                // These exceptions are always fatal
                throw e;
            } catch (CommandException e) {
                // TODO: 6/29/16 let a flag control if we should exit directly on errors or keep going
                throw e;
            }
        }
    }

    @Nullable
    @Override
    public History getHistory() {
        return null;
    }
}
