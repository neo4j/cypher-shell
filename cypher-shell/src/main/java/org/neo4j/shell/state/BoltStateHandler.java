package org.neo4j.shell.state;

import org.neo4j.driver.internal.logging.ConsoleLogging;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.Connector;
import org.neo4j.shell.TransactionHandler;
import org.neo4j.shell.TriFunction;
import org.neo4j.shell.exception.CommandException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Handles interactions with the driver
 */
public class BoltStateHandler implements TransactionHandler, Connector {
    private final TriFunction<String, AuthToken, Config, Driver> driverProvider;
    protected Driver driver;
    protected Session session;
    private String version;
    private List<Statement> transactionStatements;

    public BoltStateHandler() {
        this(GraphDatabase::driver);
    }

    BoltStateHandler(TriFunction<String, AuthToken, Config, Driver> driverProvider) {
        this.driverProvider = driverProvider;
    }

    @Override
    public void beginTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (isTransactionOpen()) {
            throw new CommandException("There is already an open transaction");
        }
        transactionStatements = new ArrayList<>();
    }

    @Override
    public Optional<List<BoltResult>> commitTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (!isTransactionOpen()) {
            throw new CommandException("There is no open transaction to commit");
        }
        return captureResults(transactionStatements);

    }

    @Override
    public void rollbackTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (!isTransactionOpen()) {
            throw new CommandException("There is no open transaction to rollback");
        }
        clearTransactionStatements();
    }

    @Override
    public boolean isTransactionOpen() {
        return transactionStatements != null;
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
            driver = getDriver(connectionConfig, authToken);
            session = driver.session();
            // Bug in Java driver forces us to run a statement to make it actually connect
            StatementResult run = session.run( "RETURN 1" );
            this.version = run.summary().server().version();
            run.consume();
        } catch (Throwable t) {
            try {
                silentDisconnect();
            } catch (Exception e) {
                t.addSuppressed(e);
            }
            throw t;
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
        if (this.transactionStatements != null) {
            transactionStatements.add(new Statement(cypher, queryParams));
            return Optional.empty();
        } else {
            List<Statement> transactionStatements = asList(new Statement(cypher, queryParams));
            BoltResult boltResult = captureResults(transactionStatements).get().get(0);
            return Optional.of(boltResult);
        }
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
                clearTransactionStatements();
            }
        }
    }

    List<Statement> getTransactionStatements() {
        return this.transactionStatements;
    }

    private void clearTransactionStatements() {
        this.transactionStatements = null;
    }

    private Driver getDriver(@Nonnull ConnectionConfig connectionConfig, @Nullable AuthToken authToken) {
        Config config = Config.build()
                              .withLogging(new ConsoleLogging(Level.OFF))
                              .withEncryptionLevel(connectionConfig.encryption()).toConfig();
        return driverProvider.apply(connectionConfig.driverUrl(), authToken, config);
    }

    private Optional<List<BoltResult>> captureResults(@Nonnull List<Statement> transactionStatements) {
        List<BoltResult> results = executeWithRetry(transactionStatements, (statement, transaction) -> {
            // calling list()/consume() is what actually executes cypher on the server
            StatementResult sr = transaction.run(statement);
            return new BoltResult(sr.list(), sr.consume());
        });

        clearTransactionStatements();
        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results);
    }

    private List<BoltResult> executeWithRetry(List<Statement> transactionStatements, BiFunction<Statement, Transaction, BoltResult> biFunction) {
        return session.writeTransaction(tx ->
                transactionStatements.stream()
                        .map(transactionStatement -> biFunction.apply(transactionStatement, tx))
                        .collect(Collectors.toList()));

    }
}
