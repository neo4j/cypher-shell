package org.neo4j.shell;

import jline.console.history.History;
import org.fusesource.jansi.AnsiRenderer;
import org.neo4j.driver.internal.logging.ConsoleLogging;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.shell.cli.CliArgHelper;
import org.neo4j.shell.cli.InteractiveShellRunner;
import org.neo4j.shell.cli.NonInteractiveShellRunner;
import org.neo4j.shell.cli.StringShellRunner;
import org.neo4j.shell.commands.Disconnect;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.prettyprint.PrettyPrinter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.internal.CLibrary.STDIN_FILENO;
import static org.fusesource.jansi.internal.CLibrary.isatty;
import static org.neo4j.shell.BoltHelper.getSensibleMsg;

/**
 * A possibly interactive shell for evaluating cypher statements.
 */
public class CypherShell implements Shell {
    protected InputStream in = System.in;
    protected PrintStream out = System.out;
    protected PrintStream err = System.err;

    // Final space to catch newline
    protected static final Pattern cmdNamePattern = Pattern.compile("^\\s*(?<name>[^\\s]+)\\b(?<args>.*)\\s*$");
    protected final CommandHelper commandHelper;
    protected final String host;
    protected final int port;
    protected final String username;
    protected final String password;
    protected Driver driver;
    protected Session session;
    protected ShellRunner runner = null;
    protected Transaction tx = null;
    protected final Map<String, Object> queryParams = new HashMap<>();

    public CypherShell(@Nonnull String host, int port, @Nonnull String username, @Nonnull String password) {
        super();
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        commandHelper = new CommandHelper(this);
    }

    public int run(@Nonnull CliArgHelper.CliArgs cliArgs) {
        int exitCode;

        try {
            connect(host, port, username, password);

            runner = getShellRunner(cliArgs);
            runner.run();

            exitCode = 0;
        } catch (ExitException e) {
            exitCode = e.getCode();
        } catch (ClientException e) {
            // When connect throws
            printError(getSensibleMsg(e));
            exitCode = 1;
        } catch (Throwable t) {
            printError(t.getMessage());
            exitCode = 1;
        }

        return exitCode;
    }

    @Override
    public void executeLine(@Nonnull final String line) throws ExitException, CommandException {
        // See if it's a shell command
        Optional<CommandExecutable> cmd = getCommandExecutable(line);
        if (cmd.isPresent()) {
            executeCmd(cmd.get());
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
    @Override
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
            result = tx.run(cypher, queryParams);
        } else {
            result = session.run(cypher, queryParams);
        }

        printOut(PrettyPrinter.format(result));
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    @Nonnull
    Optional<CommandExecutable> getCommandExecutable(@Nonnull final String line) {
        Matcher m = cmdNamePattern.matcher(line);
        if (!m.matches()) {
            return Optional.empty();
        }

        String name = m.group("name");
        String args = m.group("args");

        Command cmd = commandHelper.getCommand(name);

        if (cmd == null) {
            return Optional.empty();
        }

        return Optional.of(() -> cmd.execute(args));
    }

    void executeCmd(@Nonnull final CommandExecutable cmdExe) throws ExitException, CommandException {
        cmdExe.execute();
    }

    /**
     * Open a session to Neo4j
     */
    @Override
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
            silentDisconnect();
            throw t;
        }
    }

    /**
     * Disconnect from Neo4j, clearing up any session resources, but don't give any output.
     */
    private void silentDisconnect() {
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

    @Override
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

    @Override
    public void printOut(@Nonnull final String msg) {
        out.println(ansi().render(msg));
    }

    @Override
    public void printError(@Nonnull final String msg) {
        err.println(ansi().render(msg));
    }

    @Override
    @Nonnull
    public InputStream getInputStream() {
        return in;
    }

    @Override
    @Nonnull
    public PrintStream getOutputStream() {
        return out;
    }

    /**
     * @return true if the shell is a TTY, false otherwise (e.g., we are reading from a file)
     */
    private boolean isInteractive() {
        return 1 == isatty(STDIN_FILENO);
    }

    /**
     * Get an appropriate shellrunner depending on the given arguments and if we are running in a TTY.
     *
     * @param cliArgs
     * @return a ShellRunner
     * @throws IOException
     */
    protected ShellRunner getShellRunner(@Nonnull CliArgHelper.CliArgs cliArgs) throws IOException {
        if (cliArgs.getCypher().isPresent()) {
            return new StringShellRunner(this, cliArgs);
        } else if (isInteractive()) {
            return new InteractiveShellRunner(this);
        } else {
            return new NonInteractiveShellRunner(this, cliArgs);
        }
    }

    @Nonnull
        //TODO:DELETE IT - PRAVEENA
    Optional<Transaction> getCurrentTransaction() {
        return Optional.ofNullable(tx);
    }

    @Override
    public void beginTransaction() throws CommandException {
        if (getCurrentTransaction().isPresent()) {
            throw new CommandException("There is already an open transaction");
        }
        tx = session.beginTransaction();
    }

    @Override
    public void commitTransaction() throws CommandException {
        if (!getCurrentTransaction().isPresent()) {
            throw new CommandException("There is no open transaction to commit");
        }
        tx.success();
        tx.close();
        tx = null;
    }

    @Override
    public void rollbackTransaction() throws CommandException {
        if (!getCurrentTransaction().isPresent()) {
            throw new CommandException("There is no open transaction to rollback");
        }
        tx.failure();
        tx.close();
        tx = null;
    }

    @Nonnull
    @Override
    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    @Override
    public void set(@Nonnull String name, String valueString) {
        Record record = doCypherSilently("RETURN " + valueString + " as " + name).single();
        getQueryParams().put(name, record.get(name).asObject());
    }

    /**
     * Run a cypher statement, and return the result. Is not stored in history.
     */
    private StatementResult doCypherSilently(@Nonnull final String cypher) {
        final StatementResult result;
        if (tx != null) {
            result = tx.run(cypher, queryParams);
        } else {
            result = session.run(cypher, queryParams);
        }
        return result;
    }
}
