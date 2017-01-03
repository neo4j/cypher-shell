package org.neo4j.shell;

import jline.console.ConsoleReader;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.build.Build;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.CliArgs;
import org.neo4j.shell.commands.CommandHelper;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.AnsiLogger;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.PrintStream;

public class Main {
    static final String NEO_CLIENT_ERROR_SECURITY_UNAUTHORIZED = "Neo.ClientError.Security.Unauthorized";
    private final InputStream in;
    private final PrintStream out;

    public static void main(String[] args) {
        CliArgs cliArgs = CliArgHelper.parse(args);

        // if null, then command line parsing went wrong
        // CliArgs has already printed errors.
        if (cliArgs == null) {
            System.exit(1);
        }

        Main main = new Main();
        main.startShell(cliArgs);
    }

    Main() {
        this(System.in, System.out);
    }

    /**
     * For testing purposes
     */
    Main(final InputStream in, final PrintStream out) {
        this.in = in;
        this.out = out;
    }

    void startShell(@Nonnull CliArgs cliArgs) {
        if (cliArgs.getVersion()) {
            out.println("Cypher-Shell " + Build.version());
            return;
        }
        Logger logger = new AnsiLogger(cliArgs.getDebugMode());
        logger.setFormat(cliArgs.getFormat());

        ConnectionConfig connectionConfig = new ConnectionConfig(
                logger,
                cliArgs.getScheme(),
                cliArgs.getHost(),
                cliArgs.getPort(),
                cliArgs.getUsername(),
                cliArgs.getPassword(),
                cliArgs.getEncryption());

        try {
            CypherShell shell = new CypherShell(logger);
            connectInteractively(shell, connectionConfig);

            // Construct shellrunner after connecting, due to interrupt handling
            ShellRunner shellRunner = ShellRunner.getShellRunner(cliArgs, shell, logger, connectionConfig);

            CommandHelper commandHelper = new CommandHelper(logger, shellRunner.getHistorian(), shell);

            shell.setCommandHelper(commandHelper);

            int code = shellRunner.runUntilEnd();
            System.exit(code);
        } catch (Throwable e) {
            logger.printError(e);
            System.exit(1);
        }
    }

    /**
     * Connect the shell to the server, and try to handle missing passwords and such
     */
    void connectInteractively(@Nonnull CypherShell shell, @Nonnull ConnectionConfig connectionConfig)
            throws Exception {
        try {
            shell.connect(connectionConfig);
        } catch (ClientException e) {
            // Only prompt for username/password if they weren't used
            if (!connectionConfig.username().isEmpty() && !connectionConfig.password().isEmpty()) {
                throw e;
            }
            // Errors except authentication related should be shown to user
            if (e.code() == null || !e.code().equals(NEO_CLIENT_ERROR_SECURITY_UNAUTHORIZED)) {
                throw e;
            }
            // else need to prompt for username and password
            if (connectionConfig.username().isEmpty()) {
                connectionConfig.setUsername(promptForNonEmptyText("username", null));
            }
            if (connectionConfig.password().isEmpty()) {
                connectionConfig.setPassword(promptForText("password", '*'));
            }
            // try again
            shell.connect(connectionConfig);
        }
    }

    /**
     * @param prompt
     *         to display to the user
     * @param mask
     *         single character to display instead of what the user is typing, use null if text is not secret
     * @return the text which was entered
     * @throws Exception
     *         in case of errors
     */
    @Nonnull
    private String promptForNonEmptyText(@Nonnull String prompt, @Nullable Character mask) throws Exception {
        String text = promptForText(prompt, mask);
        if (!text.isEmpty()) {
            return text;
        }
        out.println(prompt + " cannot be empty");
        out.println();
        return promptForNonEmptyText(prompt, mask);
    }

    /**
     * @param prompt
     *         to display to the user
     * @param mask
     *         single character to display instead of what the user is typing, use null if text is not secret
     * @return the text which was entered
     * @throws Exception
     *         in case of errors
     */
    @Nonnull
    private String promptForText(@Nonnull String prompt, @Nullable Character mask) throws Exception {
        String line;
        ConsoleReader consoleReader = new ConsoleReader(in, out);
        // Disable expansion of bangs: !
        consoleReader.setExpandEvents(false);
        // Ensure Reader does not handle user input for ctrl+C behaviour
        consoleReader.setHandleUserInterrupt(false);
        line = consoleReader.readLine(prompt + ": ", mask);
        consoleReader.close();

        if (line == null) {
            throw new CommandException("No text could be read, exiting...");
        }

        return line;
    }
}
