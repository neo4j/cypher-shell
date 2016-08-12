package org.neo4j.shell;

import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.commands.CommandHelper;
import org.neo4j.shell.log.AnsiFormattedText;
import org.neo4j.shell.log.AnsiLogger;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;

import static org.neo4j.shell.exception.Helper.getFormattedMessage;

public class Main {

    public static void main(String[] args) {
        CliArgs cliArgs = CliArgHelper.parse(args);

        Main main = new Main();
        main.startShell(cliArgs);
    }

    void startShell(@Nonnull CliArgs cliArgs) {
        ConnectionConfig connectionConfig = new ConnectionConfig(cliArgs.getHost(),
                cliArgs.getPort(),
                cliArgs.getUsername(),
                cliArgs.getPassword());

        Logger logger = new AnsiLogger();
        try {
            logger.setFormat(cliArgs.getFormat());

            CypherShell shell = new CypherShell(logger);

            ShellRunner shellRunner = ShellRunner.getShellRunner(cliArgs, shell, logger);

            CommandHelper commandHelper = new CommandHelper(logger, shellRunner.getHistorian(), shell);

            shell.setCommandHelper(commandHelper);
            shell.connect(connectionConfig);

            printWelcomeMessage(logger, connectionConfig);

            int code = shellRunner.runUntilEnd();
            System.exit(code);
        } catch (Throwable e) {
            logger.printError(getFormattedMessage(e));
            System.exit(1);
        }
    }

    private static void printWelcomeMessage(@Nonnull Logger logger,
                                            @Nonnull ConnectionConfig connectionConfig) {
        logger.printIfVerbose(AnsiFormattedText
                .from("Connected to Neo4j at ")
                .bold().append(connectionConfig.driverUrl()).boldOff()
                .append(" as user ")
                .bold().append(connectionConfig.username()).boldOff()
                .append(".\nType ")
                .bold().append(":help").boldOff()
                .append(" for a list of available commands.")
                .formattedString());
    }
}
