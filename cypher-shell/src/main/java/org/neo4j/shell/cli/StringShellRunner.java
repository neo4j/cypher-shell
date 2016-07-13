package org.neo4j.shell.cli;

import jline.console.history.History;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.IShell;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;

/**
 * A shell runner which executes a single String and exits afterward. Any errors will throw immediately.
 */
public class StringShellRunner extends ShellRunner {
    private final IShell shell;
    private final String cypher;

    public StringShellRunner(@Nonnull IShell shell, @Nonnull CliArgHelper.CliArgs cliArgs) throws IOException {
        super();
        this.shell = shell;
        Optional<String> cypherString = cliArgs.getCypher();
        if (cypherString.isPresent()) {
            this.cypher = cypherString.get();
        } else {
            throw new NullPointerException("No cypher string specified");
        }
    }

    @Override
    public void run() throws IOException, ExitException, ClientException, CommandException {
        shell.executeLine(cypher.trim());
    }

    @Nullable
    @Override
    public History getHistory() {
        return null;
    }
}
