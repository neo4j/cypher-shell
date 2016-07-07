package org.neo4j.shell;

import jline.console.history.History;
import org.fusesource.jansi.AnsiRenderer;
import org.neo4j.driver.internal.logging.ConsoleLogging;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.commands.Disconnect;
import org.neo4j.shell.commands.Exit;
import org.neo4j.shell.prettyprint.PrettyPrinter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * A possibly interactive shell for evaluating cypher statements.
 */
public class CypherShell extends Shell {

    protected static final String COMMENT_PREFIX = "//";
    protected final CommandHelper commandHelper;
    protected final String host;
    protected final int port;
    protected final String username;
    protected final String password;
    protected Driver driver;
    protected Session session;
    protected ShellRunner runner = null;
    protected Transaction tx = null;

    CypherShell(@Nonnull String host, int port, @Nonnull String username, @Nonnull String password) {
        super();
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        commandHelper = new CommandHelper(this);
    }

    int run(@Nonnull CliArgHelper.CliArgs cliArgs) {
        int exitCode;

        try {
            connect(host, port, username, password);

            runner = getShellRunner(cliArgs);
            runner.run();

            exitCode = 0;
        } catch (Exit.ExitException e) {
            exitCode = e.getCode();
        }
        catch (ClientException e) {
            // When connect throws
            printError(BoltHelper.getSensibleMsg(e));
            exitCode = 1;
        }
        catch (Throwable t) {
            printError(t.getMessage());
            exitCode = 1;
        }

        return exitCode;
    }

    @Override
    public void executeLine(@Nonnull final String line) throws Exit.ExitException, CommandException {
        // See if it's a shell command
        CommandExecutable cmd = getCommandExecutable(line);
        if (cmd != null) {
            executeCmd(cmd);
            return;
        }
        // Comments and empty lines have to be ignored (Bolt throws errors on "empty" lines)
        if (line.isEmpty() || line.startsWith(COMMENT_PREFIX)) {
            return;
        }

        // Else it will be parsed as Cypher, but for that we need to be connected
        if (!isConnected()) {
            printError("Not connected to Neo4j");
            return;
        }

        executeCypher(line);
    }

    @Nonnull
    public Optional<History> getHistory() {
        if (runner == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(runner.getHistory());
        }
    }

    /**
     * Executes a piece of text as if it were Cypher. By default, all of the cypher is executed in single statement
     * (with an implicit transaction).
     *
     * @param cypher non-empty cypher text to executeLine
     */
    void executeCypher(@Nonnull final String cypher) {
        final StatementResult result;
        if (tx != null) {
            result = tx.run(cypher);
        } else {
            result = session.run(cypher);
        }

        printOut(PrettyPrinter.format(result));
    }

    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    @Nullable
    CommandExecutable getCommandExecutable(@Nonnull final String line) {
        String[] parts = line.trim().split("\\s");

        if (parts.length < 1) {
            return null;
        }

        String name = parts[0];

        Command cmd = commandHelper.getCommand(name);

        if (cmd != null) {
            List<String> args = new ArrayList<>();
            for (int i = 1; i < parts.length; i++) {
                args.add(parts[i]);
            }
            return () -> cmd.execute(args);
        }

        return null;
    }

    void executeCmd(@Nonnull final CommandExecutable cmdExe) throws Exit.ExitException, CommandException {
        cmdExe.execute();
    }

    /**
     * Open a session to Neo4j
     */
    public void connect(@Nonnull final String host, final int port, @Nonnull final String username,
                        @Nonnull final String password) throws CommandException {
        if (isConnected()) {
            throw new CommandException(String.format("Already connected. Call @|bold %s|@ first.",
                    Disconnect.COMMAND_NAME));
        }

        final AuthToken authToken;
        if (username.isEmpty() && password.isEmpty()) {
            authToken = null;
        } else if (!username.isEmpty() && !password.isEmpty()) {
            authToken = AuthTokens.basic(username, password);
        } else if (username.isEmpty()) {
            throw new CommandException("Specified password but no username");
        } else {
            throw new CommandException("Specified username but no password");
        }

        try {
            // TODO: 6/23/16 Expose some connection config functionality via cmdline arguments
            driver = GraphDatabase.driver(String.format("bolt://%s:%d", host, port),
                    authToken, Config.build().withLogging(new ConsoleLogging(Level.OFF)).toConfig());
            session = driver.session();
            // Bug in Java driver forces us to run a statement to make it actually connect
            session.run("RETURN 1").consume();
        } catch (Throwable t) {
            try {
                silentDisconnect();
            } catch (Throwable ignore) {}
            throw t;
        }
    }

    /**
     * Disconnect from Neo4j, clearing up any session resources, but don't give any output.
     */
    void silentDisconnect() {
        try {
            if (session != null) {
                session.close();
            }
            if (driver != null) {
                driver.close();
            }
        } finally {
            session = null;
            driver = null;
        }
    }

    public void disconnect() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected, nothing to disconnect from.");
        }
        silentDisconnect();
    }

    @Nonnull
    public CommandHelper getCommandHelper() {
        return commandHelper;
    }

    @Override
    @Nonnull
    public String prompt() {
        return AnsiRenderer.render("@|bold neo4j>|@ ");
    }

    @Override
    @Nullable
    public Character promptMask() {
        // If STDIN is a TTY, then echo what user types
        if (isInteractive()) {
            return null;
        } else {
            // Suppress echo
            return 0;
        }
    }

    @Nonnull
    public Optional<Transaction> getCurrentTransaction() {
        return Optional.ofNullable(tx);
    }

    public void beginTransaction() {
        tx = session.beginTransaction();
    }

    public void commitTransaction() {
        tx.success();
        tx.close();
        tx = null;
    }
    }
}
