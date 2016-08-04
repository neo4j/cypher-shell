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
import java.util.stream.Collectors;

import static org.neo4j.shell.exception.Helper.getFormattedMessage;


/**
 * A shell runner which reads all of STDIN and executes commands until completion. In case of errors, the failBehavior
 * determines if the shell exits immediately, or if it should keep trying the next commands.
 */
public class NonInteractiveShellRunner implements ShellRunner {

    private final CliArgHelper.FailBehavior failBehavior;
    private final Logger logger;
    private final StatementParser statementParser;
    private final InputStream inputStream;

    public NonInteractiveShellRunner(@Nonnull CliArgHelper.FailBehavior failBehavior,
                                     @Nonnull Logger logger,
                                     @Nonnull StatementParser statementParser,
                                     @Nonnull InputStream inputStream) {
        this.failBehavior = failBehavior;
        this.logger = logger;
        this.statementParser = statementParser;
        this.inputStream = inputStream;
    }

    @Override
    public int runUntilEnd(@Nonnull StatementExecuter executer) {
        List<String> statements;
        try {
            String script = readFileToExecute();
            statements = statementParser.parse(script);
        } catch (Throwable e) {
            logger.printError(getFormattedMessage(e));
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
                logger.printError(getFormattedMessage(e));
                if (CliArgHelper.FailBehavior.FAIL_AT_END != failBehavior) {
                    return exitCode;
                }
            }
        }
        return exitCode;
    }

    @Nonnull
    private String readFileToExecute() {
        return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
    }

    @Nonnull
    @Override
    public Historian getHistorian() {
        return Historian.empty;
    }
}
