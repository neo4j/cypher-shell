package org.neo4j.shell.cli;

import org.neo4j.shell.CommandExecuter;
import org.neo4j.shell.Historian;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;

import static org.neo4j.shell.BoltHelper.getSensibleMsg;


/**
 * A shell runner which reads from STDIN and executes commands until completion. In case of errors, the failBehavior
 * determines if the shell exits immediately, or if it should keep trying the next commands.
 */
public class NonInteractiveShellRunner implements ShellRunner {

    private final CliArgHelper.FailBehavior failBehavior;
    private final CommandReader commandReader;
    private final Logger logger;

    public NonInteractiveShellRunner(@Nonnull CliArgHelper.FailBehavior failBehavior,
                                     @Nonnull CommandReader commandReader,
                                     @Nonnull Logger logger) {
        this.failBehavior = failBehavior;
        this.logger = logger;
        this.commandReader = commandReader;
    }

    @Override
    public int runUntilEnd(@Nonnull CommandExecuter executer) {
        String command;
        boolean running = true;
        int exitCode = 0;
        while (running) {
            try {
                command = commandReader.readCommand();
                if (null == command) {
                    running = false;
                    continue;
                }

                if (!command.trim().isEmpty()) {
                    executer.execute(command);
                }
            } catch (ExitException e) {
                // These exceptions are always fatal
                exitCode = 1;
                running = false;
            } catch (Throwable e) {
                exitCode = 1;
                logger.printError(getSensibleMsg(e));
                if (CliArgHelper.FailBehavior.FAIL_AT_END != failBehavior) {
                    running = false;
                }
            }
        }

        return exitCode;
    }

    @Override
    public Historian getHistorian() {
        return commandReader;
    }
}
