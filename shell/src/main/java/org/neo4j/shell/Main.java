package org.neo4j.shell;

import org.fusesource.jansi.AnsiConsole;

public class Main {

    public static void main(String[] args) {
        // TODO: 6/21/16 command line arguments

        configureTerminal(false);

        Main main = new Main();
        main.startShell();
    }

    private static void configureTerminal(boolean suppressColor) {
        if (!suppressColor) {
            AnsiConsole.systemInstall();
        }
    }

    private void startShell() {
        CypherShell shell = new CypherShell();

        // TODO: 6/21/16 Shutdown hook for recording history

        int code = shell.run();

        System.exit(code);
    }
}
