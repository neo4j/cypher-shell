package org.neo4j.shell.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.AccessMode;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.summary.DatabaseInfo;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.Connector;
import org.neo4j.shell.DatabaseManager;
import org.neo4j.shell.TransactionHandler;
import org.neo4j.shell.TriFunction;
import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.NullLogging;

import static org.neo4j.shell.util.Versions.majorVersion;

/**
 * Handles interactions with the driver
 */
public class BoltStateHandler implements TransactionHandler, Connector, DatabaseManager {
    private final TriFunction<String, AuthToken, Config, Driver> driverProvider;
    protected Driver driver;
    Session session;
    private String version;
    private String activeDatabaseNameAsSetByUser;
    private String actualDatabaseNameAsReportedByServer;
    private final boolean isInteractive;
    private Bookmark systemBookmark;
    private Transaction tx = null;

    public BoltStateHandler(boolean isInteractive) {
        this(GraphDatabase::driver, isInteractive);
    }

    BoltStateHandler(TriFunction<String, AuthToken, Config, Driver> driverProvider,
                     boolean isInteractive) {
        this.driverProvider = driverProvider;
        activeDatabaseNameAsSetByUser = ABSENT_DB_NAME;
        this.isInteractive = isInteractive;
    }

    @Override
    public void setActiveDatabase(String databaseName) throws CommandException
    {
        if (isTransactionOpen()) {
            throw new CommandException("There is an open transaction. You need to close it before you can switch database.");
        }
        String previousDatabaseName = activeDatabaseNameAsSetByUser;
        activeDatabaseNameAsSetByUser = databaseName;
        try {
            if (isConnected()) {
                reconnect(true);
            }
        }
        catch (ClientException e) {
            if (isInteractive) {
                // We want to try to connect to the previous database
                activeDatabaseNameAsSetByUser = previousDatabaseName;
                try {
                    reconnect(true);
                }
                catch (Exception e2) {
                    e.addSuppressed(e2);
                }
            }
            throw e;
        }
    }

    @Override
    public String getActiveDatabaseAsSetByUser()
    {
        return activeDatabaseNameAsSetByUser;
    }

    @Override
    public String getActualDatabaseAsReportedByServer()
    {
        return actualDatabaseNameAsReportedByServer;
    }

    @Override
    public void beginTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (isTransactionOpen()) {
            throw new CommandException("There is already an open transaction");
        }
        tx = session.beginTransaction();
    }

    @Override
    public Optional<List<BoltResult>> commitTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (!isTransactionOpen()) {
            throw new CommandException("There is no open transaction to commit");
        }
        tx.commit();
        tx.close();
        tx = null;

        return Optional.empty();
    }

    @Override
    public void rollbackTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (!isTransactionOpen()) {
            throw new CommandException("There is no open transaction to rollback");
        }
        tx.rollback();
        tx.close();
        tx = null;
    }

    @Override
    public boolean isTransactionOpen() {
        return tx != null;
    }

    @Override
    public boolean isConnected() {
        return session != null && session.isOpen();
    }

    @Override
    public void connect(@Nonnull ConnectionConfig connectionConfig) throws CommandException {
        if (isConnected()) {
            throw new CommandException("Already connected");
        }

        final AuthToken authToken = AuthTokens.basic(connectionConfig.username(), connectionConfig.password());

        try {
            setActiveDatabase(connectionConfig.database());
            driver = getDriver(connectionConfig, authToken);
            reconnect();
        } catch (Throwable t) {
            try {
                silentDisconnect();
            } catch (Exception e) {
                t.addSuppressed(e);
            }
            throw t;
        }
    }

    private void reconnect() {
        reconnect(true);
    }

    private void reconnect(boolean keepBookmark) {
        SessionConfig.Builder builder = SessionConfig.builder();
        builder.withDefaultAccessMode(AccessMode.WRITE);
        if (!ABSENT_DB_NAME.equals(activeDatabaseNameAsSetByUser)) {
            builder.withDatabase( activeDatabaseNameAsSetByUser );
        }
        if (session != null && keepBookmark) {
            // Save the last bookmark and close the session
            final Bookmark bookmark = session.lastBookmark();
            session.close();
            builder.withBookmarks(bookmark);
        } else if (systemBookmark != null) {
            builder.withBookmarks(systemBookmark);
        }

        session = driver.session(builder.build());

        String query = activeDatabaseNameAsSetByUser.compareToIgnoreCase(SYSTEM_DB_NAME) == 0 ? "CALL db.indexes()" : "RETURN 1";

        resetActualDbName(); // Set this to null first in case run throws an exception
        Result run = session.run(query);
        ResultSummary summary = null;
        try {
            summary = run.consume();
        } finally {
            // Since run.consume() can throw the first time we have to go through this extra hoop to get the summary
            if (summary == null) {
                summary = run.consume();
            }
            this.version = summary.server().version();
            updateActualDbName(summary);
        }
    }

    @Nonnull
    @Override
    public String getServerVersion() {
        if (isConnected()) {
            if (version == null) {
                // On versions before 3.1.0-M09
                version = "";
            }
            if (version.startsWith("Neo4j/")) {
                // Want to return '3.1.0' and not 'Neo4j/3.1.0'
                version = version.substring(6);
            }
            return version;
        }
        return "";
    }

    @Nonnull
    public Optional<BoltResult> runCypher(@Nonnull String cypher,
                                          @Nonnull Map<String, Object> queryParams) throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (isTransactionOpen()) {
            // If this fails, don't try any funny business - just let it die
            return getBoltResult(cypher, queryParams);
        } else {
            try {
                // Note that PERIODIC COMMIT can't execute in a transaction, so if the user has not typed BEGIN, then
                // the statement should NOT be executed in a transaction.
                return getBoltResult(cypher, queryParams);
            } catch (SessionExpiredException e) {
                // Server is no longer accepting writes, reconnect and try again.
                // If it still fails, leave it up to the user
                reconnect();
                return getBoltResult(cypher, queryParams);
            }
        }
    }

    public void updateActualDbName(@Nonnull ResultSummary resultSummary) {
        actualDatabaseNameAsReportedByServer = getActualDbName(resultSummary);
    }

    public void changePassword(@Nonnull ConnectionConfig connectionConfig) {
        if (!connectionConfig.passwordChangeRequired()) {
            return;
        }

        if (isConnected()) {
            silentDisconnect();
        }

        final AuthToken authToken = AuthTokens.basic(connectionConfig.username(), connectionConfig.password());

        try {
            driver = getDriver(connectionConfig, authToken);

            SessionConfig.Builder builder = SessionConfig.builder()
                    .withDefaultAccessMode(AccessMode.WRITE)
                    .withDatabase(SYSTEM_DB_NAME);
            session = driver.session(builder.build());

            String command;
            Value parameters;
            if (majorVersion(getServerVersion()) >= 4) {
                command = "ALTER CURRENT USER SET PASSWORD FROM $o TO $n";
                parameters = Values.parameters("o", connectionConfig.password(), "n", connectionConfig.newPassword());
            } else {
                command = "CALL dbms.security.changePassword($n)";
                parameters = Values.parameters("n", connectionConfig.newPassword());
            }

            Result run = session.run(command, parameters);
            run.consume();

            // If successful, use the new password when reconnecting
            connectionConfig.setPassword(connectionConfig.newPassword());
            connectionConfig.setNewPassword(null);

            // Save a system bookmark to make sure we wait for the password change to propagate on reconnection
            systemBookmark = session.lastBookmark();

            silentDisconnect();
        } catch (Throwable t) {
            try {
                silentDisconnect();
            } catch (Exception e) {
                t.addSuppressed(e);
            }
            throw t;
        }
    }

    /**
     * @throws SessionExpiredException when server no longer serves writes anymore
     */
    @Nonnull
    private Optional<BoltResult> getBoltResult(@Nonnull String cypher, @Nonnull Map<String, Object> queryParams) throws SessionExpiredException {
        Result statementResult;

        if (isTransactionOpen()){
            statementResult = tx.run(new Query(cypher, queryParams));
        } else {
            statementResult = session.run(new Query(cypher, queryParams));
        }

        if (statementResult == null) {
            return Optional.empty();
        }

        return Optional.of(new StatementBoltResult(statementResult));
    }

    private String getActualDbName(@Nonnull ResultSummary resultSummary) {
        DatabaseInfo dbInfo = resultSummary.database();
        return dbInfo.name() == null ? ABSENT_DB_NAME : dbInfo.name();
    }

    private void resetActualDbName() {
        actualDatabaseNameAsReportedByServer = null;
    }

    /**
     * Disconnect from Neo4j, clearing up any session resources, but don't give any output.
     * Intended only to be used if connect fails.
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
            resetActualDbName();
        }
    }

    /**
     * Reset the current session. This rolls back any open transactions.
     */
    public void reset() {
        if (isConnected()) {
            session.reset();

            // Clear current state
            if (isTransactionOpen()) {
                // Bolt has already rolled back the transaction but it doesn't close it properly
                tx.rollback();
                tx.close();
                tx = null;
            }
        }
    }

    /**
     * Used for testing purposes
     */
    public void disconnect() {
         reset();
         silentDisconnect();
    }

    private Driver getDriver(@Nonnull ConnectionConfig connectionConfig, @Nullable AuthToken authToken) {
        Config.ConfigBuilder configBuilder = Config.builder().withLogging(NullLogging.NULL_LOGGING);
        if (connectionConfig.encryption()) {
            configBuilder = configBuilder.withEncryption();
        } else {
            configBuilder = configBuilder.withoutEncryption();
        }
        return driverProvider.apply(connectionConfig.driverUrl(), authToken, configBuilder.build());
    }

    private List<BoltResult> executeWithRetry(List<Query> transactionStatements, BiFunction<Query, Transaction, BoltResult> biFunction) {
        return session.writeTransaction(tx ->
                transactionStatements.stream()
                        .map(transactionStatement -> biFunction.apply(transactionStatement, tx))
                        .collect(Collectors.toList()));

    }
}
