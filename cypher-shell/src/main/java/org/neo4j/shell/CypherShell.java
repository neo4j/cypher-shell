package org.neo4j.shell;

import org.neo4j.driver.internal.logging.ConsoleLogging;
import org.neo4j.driver.v1.*;
import org.neo4j.shell.commands.Command;
import org.neo4j.shell.commands.CommandExecutable;
import org.neo4j.shell.commands.CommandHelper;
import org.neo4j.shell.commands.Disconnect;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.prettyprint.PrettyPrinter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A possibly interactive shell for evaluating cypher statements.
 */
public class CypherShell implements StatementExecuter, Connector, TransactionHandler, VariableHolder {
    private final Logger logger;

    // Final space to catch newline
    protected static final Pattern cmdNamePattern = Pattern.compile("^\\s*(?<name>[^\\s]+)\\b(?<args>.*)\\s*$");
    protected CommandHelper commandHelper;
    protected Driver driver;
    protected Session session;
    protected Transaction tx = null;
    protected final Map<String, Object> queryParams = new HashMap<>();

    public CypherShell(@Nonnull Logger logger) {
        this.logger = logger;
    }

    @Override
    public void execute(@Nonnull final String cmdString) throws ExitException, CommandException {
        // See if it's a shell command
        Optional<CommandExecutable> cmd = getCommandExecutable(cmdString);
        if (cmd.isPresent()) {
            executeCmd(cmd.get());
            return;
        }

        // Else it will be parsed as Cypher, but for that we need to be connected
        if (!isConnected()) {
            logger.printError("Not connected to Neo4j");
            return;
        }

        executeCypher(cmdString);
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

        logger.printOut(PrettyPrinter.format(result));
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    @Nonnull
    Optional<CommandExecutable> getCommandExecutable(@Nonnull final String line) {
        Matcher m = cmdNamePattern.matcher(line);
        if (commandHelper == null || !m.matches()) {
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
     *
     * @param connectionConfig
     */
    @Override
    public void connect(@Nonnull ConnectionConfig connectionConfig) throws CommandException {
        if (isConnected()) {
            throw new CommandException(String.format("Already connected. Call @|bold %s|@ first.",
                    Disconnect.COMMAND_NAME));
        }

        final AuthToken authToken;
        if (connectionConfig.username().isEmpty() && connectionConfig.password().isEmpty()) {
            authToken = null;
        } else if (!connectionConfig.username().isEmpty() && !connectionConfig.password().isEmpty()) {
            authToken = AuthTokens.basic(connectionConfig.username(), connectionConfig.password());
        } else if (connectionConfig.username().isEmpty()) {
            throw new CommandException("Specified password but no username");
        } else {
            throw new CommandException("Specified username but no password");
        }

        try {
            // TODO: 6/23/16 Expose some connection config functionality via cmdline arguments
            driver = GraphDatabase.driver(connectionConfig.driverUrl(), authToken, Config.build()
                    .withLogging(new ConsoleLogging(Level.OFF)).toConfig());
            session = driver.session();
            // Bug in Java driver forces us to runUntilEnd a statement to make it actually connect
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
        //TODO:DELETE IT - PRAVEENA
    Optional<Transaction> getCurrentTransaction() {
        return Optional.ofNullable(tx);
    }

    @Override
    public void beginTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (getCurrentTransaction().isPresent()) {
            throw new CommandException("There is already an open transaction");
        }
        tx = session.beginTransaction();
    }

    @Override
    public void commitTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (!getCurrentTransaction().isPresent()) {
            throw new CommandException("There is no open transaction to commit");
        }
        tx.success();
        tx.close();
        tx = null;
    }

    @Override
    public void rollbackTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (!getCurrentTransaction().isPresent()) {
            throw new CommandException("There is no open transaction to rollback");
        }
        tx.failure();
        tx.close();
        tx = null;
    }

    @Override
    @Nonnull
    public Optional set(@Nonnull String name, @Nonnull String valueString) {
        Record record = doCypherSilently("RETURN " + valueString + " as " + name).single();
        Object value = record.get(name).asObject();
        queryParams.put(name, value);
        return Optional.ofNullable(value);
    }

    @Override
    @Nonnull
    public Map<String, Object> getAll() {
        return queryParams;
    }

    @Override
    @Nonnull
    public Optional remove(@Nonnull String name) {
        return Optional.ofNullable(queryParams.remove(name));
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

    public void setCommandHelper(@Nonnull CommandHelper commandHelper) {
        this.commandHelper = commandHelper;
    }
}
