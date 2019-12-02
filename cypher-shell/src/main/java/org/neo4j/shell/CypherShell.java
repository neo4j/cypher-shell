package org.neo4j.shell;

import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.shell.commands.Command;
import org.neo4j.shell.commands.CommandExecutable;
import org.neo4j.shell.commands.CommandHelper;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.exception.ExitException;
import org.neo4j.shell.prettyprint.LinePrinter;
import org.neo4j.shell.prettyprint.PrettyConfig;
import org.neo4j.shell.prettyprint.PrettyPrinter;
import org.neo4j.shell.state.BoltResult;
import org.neo4j.shell.state.BoltStateHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A possibly interactive shell for evaluating cypher statements.
 */
public class CypherShell implements StatementExecuter, Connector, TransactionHandler, DatabaseManager {
    // Final space to catch newline
    private static final Pattern cmdNamePattern = Pattern.compile("^\\s*(?<name>[^\\s]+)\\b(?<args>.*)\\s*$");
    private final ParameterMap parameterMap;
    private final LinePrinter linePrinter;
    private final BoltStateHandler boltStateHandler;
    private final PrettyPrinter prettyPrinter;
    private CommandHelper commandHelper;
    private String lastNeo4jErrorCode;

    public CypherShell(@Nonnull LinePrinter linePrinter,
                       @Nonnull PrettyConfig prettyConfig,
                       boolean isInteractive,
                       ParameterMap parameterMap) {
        this(linePrinter, new BoltStateHandler(isInteractive), new PrettyPrinter(prettyConfig), parameterMap);
    }

    protected CypherShell(@Nonnull LinePrinter linePrinter,
                          @Nonnull BoltStateHandler boltStateHandler,
                          @Nonnull PrettyPrinter prettyPrinter,
                          ParameterMap parameterMap) {
        this.linePrinter = linePrinter;
        this.boltStateHandler = boltStateHandler;
        this.prettyPrinter = prettyPrinter;
        this.parameterMap = parameterMap;
        addRuntimeHookToResetShell();
    }

    /**
     * @param text to trim
     * @return text without trailing semicolons
     */
    protected static String stripTrailingSemicolons(@Nonnull String text) {
        int end = text.length();
        while (end > 0 && text.substring(0, end).endsWith(";")) {
            end -= 1;
        }
        return text.substring(0, end);
    }

    @Override
    public void execute(@Nonnull final String cmdString) throws ExitException, CommandException {
        // See if it's a shell command
        final Optional<CommandExecutable> cmd = getCommandExecutable(cmdString);
        if (cmd.isPresent()) {
            executeCmd(cmd.get());
            return;
        }

        // Else it will be parsed as Cypher, but for that we need to be connected
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }

        executeCypher(cmdString);
    }

    @Override
    public String lastNeo4jErrorCode() {
        return lastNeo4jErrorCode;
    }

    /**
     * Executes a piece of text as if it were Cypher. By default, all of the cypher is executed in single statement
     * (with an implicit transaction).
     *
     * @param cypher non-empty cypher text to executeLine
     */
    private void executeCypher(@Nonnull final String cypher) throws CommandException {
        try {
            final Optional<BoltResult> result = boltStateHandler.runCypher(cypher, parameterMap.allParameterValues());
            result.ifPresent(boltResult -> {
                prettyPrinter.format(boltResult, linePrinter);
                boltStateHandler.updateActualDbName(boltResult.getSummary());
            });
            lastNeo4jErrorCode = null;
        } catch (Neo4jException e) {
            lastNeo4jErrorCode = getErrorCode(e);
            throw e;
        }
    }

    @Override
    public boolean isConnected() {
        return boltStateHandler.isConnected();
    }

    @Nonnull
    protected Optional<CommandExecutable> getCommandExecutable(@Nonnull final String line) {
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

        return Optional.of(() -> cmd.execute(stripTrailingSemicolons(args)));
    }

    protected void executeCmd(@Nonnull final CommandExecutable cmdExe) throws ExitException, CommandException {
        cmdExe.execute();
    }

    /**
     * Open a session to Neo4j
     *
     * @param connectionConfig
     */
    @Override
    public void connect(@Nonnull ConnectionConfig connectionConfig) throws CommandException {
        boltStateHandler.connect(connectionConfig);
    }

    @Nonnull
    @Override
    public String getServerVersion() {
        return boltStateHandler.getServerVersion();
    }

    @Override
    public void beginTransaction() throws CommandException {
        boltStateHandler.beginTransaction();
    }

    @Override
    public Optional<List<BoltResult>> commitTransaction() throws CommandException {
        try {
            Optional<List<BoltResult>> results = boltStateHandler.commitTransaction();
            results.ifPresent(boltResult -> boltResult.forEach(result -> prettyPrinter.format(result, linePrinter)));
            lastNeo4jErrorCode = null;
            return results;
        } catch (Neo4jException e) {
            lastNeo4jErrorCode = getErrorCode(e);
            throw e;
        }
    }

    @Override
    public void rollbackTransaction() throws CommandException {
        boltStateHandler.rollbackTransaction();
    }

    @Override
    public boolean isTransactionOpen() {
        return boltStateHandler.isTransactionOpen();
    }

    void setCommandHelper(@Nonnull CommandHelper commandHelper) {
        this.commandHelper = commandHelper;
    }

    @Override
    public void reset() {
        boltStateHandler.reset();
    }

    protected void addRuntimeHookToResetShell() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::reset));
    }

    @Override
    public void setActiveDatabase(String databaseName) throws CommandException
    {
        try {
            boltStateHandler.setActiveDatabase(databaseName);
            lastNeo4jErrorCode = null;
        } catch (Neo4jException e) {
            lastNeo4jErrorCode = getErrorCode(e);
            throw e;
        }
    }

    @Override
    public String getActiveDatabaseAsSetByUser()
    {
        return boltStateHandler.getActiveDatabaseAsSetByUser();
    }

    @Override
    public String getActualDatabaseAsReportedByServer()
    {
        return boltStateHandler.getActualDatabaseAsReportedByServer();
    }

    /**
     * @return the parameter map.
     */
    public ParameterMap getParameterMap()
    {
        return parameterMap;
    }

    public void changePassword(@Nonnull ConnectionConfig connectionConfig) {
        boltStateHandler.changePassword(connectionConfig);
    }

    /**
     * Used for testing purposes
     */
    public void disconnect() {
        boltStateHandler.disconnect();
    }

    private String getErrorCode(Neo4jException e) {
        Neo4jException statusException = e;

        // If we encountered a later suppressed Neo4jException we use that as the basis for the status instead
        Throwable[] suppressed = e.getSuppressed();
        for (Throwable t : suppressed) {
            if (t instanceof Neo4jException) {
                statusException = (Neo4jException) t;
            }
        }

        if (statusException instanceof ServiceUnavailableException) {
            // Unwrap possible transient Neo4jExceptions
            Throwable cause = statusException.getCause();
            if (cause instanceof Neo4jException) {
                return ((Neo4jException) cause).code();
            }
            // Treat this the same way as a DatabaseUnavailable error for now.
            return DATABASE_UNAVAILABLE_ERROR_CODE;
        }
        return statusException.code();
    }
}
