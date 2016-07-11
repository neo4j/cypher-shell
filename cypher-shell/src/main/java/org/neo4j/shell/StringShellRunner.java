package org.neo4j.shell;

import jline.console.history.History;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.cli.CliArgHelper;
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
    private final Shell shell;
    private final String cypher;

    public StringShellRunner(@Nonnull Shell shell, @Nonnull CliArgHelper.CliArgs cliArgs) throws IOException {
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
