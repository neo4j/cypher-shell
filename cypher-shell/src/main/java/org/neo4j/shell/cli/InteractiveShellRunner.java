package org.neo4j.shell.cli;

import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.CommandExecuter;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.neo4j.shell.BoltHelper.getSensibleMsg;

/**
 * An interactive shell
 */
public class InteractiveShellRunner implements ShellRunner {
    private final CommandReader commandReader;
    private final Logger logger;

    public InteractiveShellRunner(@Nonnull CommandReader commandReader,
                                  @Nonnull Logger logger) {
        this.commandReader = commandReader;
        this.logger = logger;
    }

    @Override
    public int runUntilEnd(@Nonnull CommandExecuter executer) {
        int exitCode = 0;
        boolean running = true;
        while (running) {
            try {
                running = work(executer);
            } catch (ExitException e) {
                exitCode = e.getCode();
                running = false;
            } catch (ClientException e) {
                logger.printError(getSensibleMsg(e));
                exitCode = 1;
                running = false;
            }
            catch (CommandException e) {
                logger.printError(e.getMessage());
                exitCode = 1;
                running = false;
            } catch (Throwable t) {
                // TODO: 6/21/16 Unknown errors maybe should be handled differently
                logger.printError(t.getMessage());
                exitCode = 1;
                running = false;
            }
        }
        return exitCode;
    }

    private boolean work(@Nonnull CommandExecuter runner) throws IOException, ExitException, CommandException {
        String line = commandReader.readCommand();

        if (null == line) {
            return false;
        }

        if (!line.trim().isEmpty()) {
            runner.execute(line);
        }

        return true;
    }
}
