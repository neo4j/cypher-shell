package org.neo4j.shell;

import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.commands.CommandHelper;
import org.neo4j.shell.log.AnsiLogger;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;

import static org.neo4j.shell.exception.Helper.getFormattedMessage;

public class Main {

    public static void main(String[] args) {
        CliArgHelper.CliArgs cliArgs = CliArgHelper.parse(args);

        Main main = new Main();
        main.startShell(cliArgs);
    }

    private void startShell(@Nonnull CliArgHelper.CliArgs cliArgs) {
        ConnectionConfig connectionConfig = new ConnectionConfig(cliArgs.getHost(),
                cliArgs.getPort(),
                cliArgs.getUsername(),
                cliArgs.getPassword());

        Logger logger = new AnsiLogger();
        try {
            ShellRunner shellRunner = ShellRunner.getShellRunner(cliArgs, logger);

            CypherShell shell = new CypherShell(logger, cliArgs.getFormat());

            addRuntimeHookToResetShell(shell);

            CommandHelper commandHelper = new CommandHelper(logger, shellRunner.getHistorian(), shell);

            shell.setCommandHelper(commandHelper);
            shell.connect(connectionConfig);

            int code = shellRunner.runUntilEnd(shell);
            System.exit(code);
        } catch (Throwable e) {
            logger.printError(getFormattedMessage(e));
            System.exit(1);
        }
    }

    private void addRuntimeHookToResetShell(final CypherShell shell) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shell.reset();
            }
        });
    }
}
