package org.neo4j.shell;

import jline.console.ConsoleReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * An interactive shell
 */
public class InteractiveShellRunner {
    private final ConsoleReader reader;
    private final CypherShell shell;
    private final Supplier<String> prompt;

    public InteractiveShellRunner(@Nonnull CypherShell shell, @Nonnull Supplier<String> prompt) throws IOException {
        this.shell = shell;
        this.prompt = prompt;
        this.reader = new ConsoleReader();
    }

    public void run() {
        boolean running = true;
        while (running) {
            try {
                running = work();
            } catch (Throwable t) {
                // TODO: 6/21/16 Do error handling
                System.err.println("Error: " + t.getMessage());
            }
        }
    }

    private boolean work() throws IOException {
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
        return reader.readLine(prompt.get());

    }
}
