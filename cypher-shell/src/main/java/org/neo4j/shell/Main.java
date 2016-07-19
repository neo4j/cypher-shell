package org.neo4j.shell;

import org.fusesource.jansi.AnsiConsole;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.log.StdLogger;

import javax.annotation.Nonnull;

import static org.neo4j.shell.BoltHelper.getSensibleMsg;

public class Main {

    public static void main(String[] args) {
        CliArgHelper.CliArgs cliArgs = CliArgHelper.parse(args);

        configureTerminal(cliArgs.getSuppressColor());

        Main main = new Main();
        main.startShell(cliArgs);
    }

    private static void configureTerminal(boolean suppressColor) {
        if (!suppressColor) {
            AnsiConsole.systemInstall();
        }
    }

    private void startShell(@Nonnull CliArgHelper.CliArgs cliArgs) {
        ConnectionConfig connectionConfig = new ConnectionConfig(cliArgs.getHost(),
                cliArgs.getPort(),
                cliArgs.getUsername(),
                cliArgs.getPassword());

        Logger logger = new StdLogger();
        try {
            ShellRunner shellRunner = ShellRunner.getShellRunner(cliArgs, logger);
            CypherShell shell = new CypherShell(logger, connectionConfig);

            CommandHelper commandHelper = new CommandHelper(logger, shellRunner.getCommandReader(), shell, shell, shell);
            shell.setCommandHelper(commandHelper);

            int code = shellRunner.runUntilEnd(shell);
            System.exit(code);
        } catch (Throwable e) {
            logger.printError(getSensibleMsg(e));
            System.exit(1);
        }
    }
}
