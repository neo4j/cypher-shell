package org.neo4j.shell;

import jline.console.history.History;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nullable;
import java.io.IOException;

public abstract class ShellRunner {
    /**
     * Run and handle user input until end of file
     */
    abstract public void run() throws IOException, ExitException, ClientException, CommandException;

    @Nullable
    public abstract History getHistory();
}
