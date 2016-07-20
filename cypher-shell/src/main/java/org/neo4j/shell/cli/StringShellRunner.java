package org.neo4j.shell.cli;

import org.neo4j.shell.CommandExecuter;
import org.neo4j.shell.Historian;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.neo4j.shell.BoltHelper.getSensibleMsg;

/**
 * A shell runner which executes a single String and exits afterward. Any errors will throw immediately.
 */
public class StringShellRunner implements ShellRunner {
    private final String cypher;
    private final Logger logger;

    public StringShellRunner(@Nonnull CliArgHelper.CliArgs cliArgs,
                             @Nonnull Logger logger) {
        this.logger = logger;
        Optional<String> cypherString = cliArgs.getCypher();
        if (cypherString.isPresent()) {
            this.cypher = cypherString.get();
        } else {
            throw new NullPointerException("No cypher string specified");
        }
    }

    @Override
    public int runUntilEnd(@Nonnull CommandExecuter executer) {
        int exitCode = 0;
        try {
            executer.execute(cypher.trim());
        } catch (Throwable t) {
            logger.printError(getSensibleMsg(t));
            exitCode = 1;
        }
        return exitCode;
    }

    @Override
    public Historian getHistorian() {
        return new Historian() {
            @Nonnull
            @Override
            public List<String> getHistory() {
                return new ArrayList<>();
            }
        };
    }
}
