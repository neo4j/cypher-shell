package org.neo4j.shell.cli;

import jline.console.UserInterruptException;
import org.neo4j.shell.Historian;
import org.neo4j.shell.ShellRunner;
import org.neo4j.shell.StatementExecuter;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.AnsiFormattedText;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.neo4j.shell.exception.Helper.getFormattedMessage;

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
    public int runUntilEnd(@Nonnull StatementExecuter executer) {
        int exitCode = 0;
        boolean running = true;
        while (running) {
            try {
                running = work(executer);
            } catch (ExitException e) {
                exitCode = e.getCode();
                running = false;
            } catch (UserInterruptException e) {
                // Not very nice to print "UserInterruptException"
                logger.printError(AnsiFormattedText.s().colorRed().append("KeyboardInterrupt").formattedString());
            } catch (Throwable e) {
                logger.printError(getFormattedMessage(e));
            }
        }
        return exitCode;
    }

    @Override
    public Historian getHistorian() {
        return commandReader;
    }

    private boolean work(@Nonnull StatementExecuter runner) throws IOException, ExitException, CommandException {
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
