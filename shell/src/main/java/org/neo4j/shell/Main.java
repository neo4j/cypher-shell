package org.neo4j.shell;

import org.fusesource.jansi.AnsiConsole;

public class Main {

    public static void main(String[] args) {
        CliArgHelper.CliArgs cliArgs = CliArgHelper.parse(args);

        configureTerminal(cliArgs.suppressColor());

        Main main = new Main();
        main.startShell(cliArgs);
    }

    private static void configureTerminal(boolean suppressColor) {
        if (!suppressColor) {
            AnsiConsole.systemInstall();
        }
    }

    private void startShell(CliArgHelper.CliArgs cliArgs) {
        CypherShell shell = new CypherShell(cliArgs.host(), cliArgs.port(), cliArgs.username(), cliArgs.password());

        // TODO: 6/21/16 Shutdown hook for recording history

        int code = shell.run();

        System.exit(code);
    }
}
