package org.neo4j.shell.cli;

import javax.annotation.Nonnull;

import org.neo4j.shell.Historian;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.log.Logger;

import static org.neo4j.shell.Main.EXIT_FAILURE;
import static org.neo4j.shell.Main.EXIT_SUCCESS;

/**
 * A shell runner which executes a single String and exits afterward. Any errors will throw immediately.
 */
public class StringShellRunner implements ShellRunner {
    private final String cypher;
    private final Logger logger;
    private final StatementExecuter executer;

    public StringShellRunner(@Nonnull CliArgs cliArgs,
                             @Nonnull StatementExecuter executer,
                             @Nonnull Logger logger) {
        this.executer = executer;
        this.logger = logger;
        if (cliArgs.isStringShell()) {
            this.cypher = cliArgs.getCypher().get();
        } else {
            throw new NullPointerException("No cypher string specified");
        }
    }

    @Override
    public int runUntilEnd() {
        int exitCode = EXIT_SUCCESS;
        try {
            executer.execute(cypher.trim());
        } catch (Throwable t) {
            logger.printError(t);
            exitCode = EXIT_FAILURE;
        }
        return exitCode;
    }

    @Nonnull
    @Override
    public Historian getHistorian() {
        return Historian.empty;
    }
}
