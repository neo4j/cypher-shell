package org.neo4j.shell;

import jline.console.ConsoleReader;

import javax.annotation.Nullable;
import java.io.IOException;

public class CommandReader {
    private final ConsoleReader reader;
    private final Shell shell;

    public CommandReader(ConsoleReader reader, Shell shell) {
        this.reader = reader;
        this.shell = shell;
    }

    @Nullable
    public String readCommand() throws IOException {
        return reader.readLine(shell.prompt(), shell.promptMask());
    }
}
