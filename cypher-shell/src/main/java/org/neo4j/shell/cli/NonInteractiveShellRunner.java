package org.neo4j.shell.cli;

import org.neo4j.shell.Historian;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.parser.StatementParser;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;


/**
 * A shell runner which reads all of STDIN and executes commands until completion. In case of errors, the failBehavior
 * determines if the shell exits immediately, or if it should keep trying the next commands.
 */
public class NonInteractiveShellRunner implements ShellRunner {

    private final FailBehavior failBehavior;
    @Nonnull
    private final StatementExecuter executer;
    private final Logger logger;
    private final StatementParser statementParser;
    private final InputStream inputStream;

    public NonInteractiveShellRunner(@Nonnull FailBehavior failBehavior,
                                     @Nonnull StatementExecuter executer,
                                     @Nonnull Logger logger,
                                     @Nonnull StatementParser statementParser,
                                     @Nonnull InputStream inputStream) {
        this.failBehavior = failBehavior;
        this.executer = executer;
        this.logger = logger;
        this.statementParser = statementParser;
        this.inputStream = inputStream;
    }

    @Override
    public int runUntilEnd() {
        List<String> statements;
        try {
            new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .forEach(line -> statementParser.parseMoreText(line + "\n"));
            statements = statementParser.consumeStatements();
        } catch (Throwable e) {
            logger.printError(e);
            return 1;
        }

        int exitCode = 0;
        for (String statement : statements) {
            try {
                executer.execute(statement);
            } catch (ExitException e) {
                // These exceptions are always fatal
                return e.getCode();
            } catch (Throwable e) {
                exitCode = 1;
                logger.printError(e);
                if (FailBehavior.FAIL_AT_END != failBehavior) {
                    return exitCode;
                }
            }
        }
        return exitCode;
    }

    @Nonnull
    @Override
    public Historian getHistorian() {
        return Historian.empty;
    }
}
