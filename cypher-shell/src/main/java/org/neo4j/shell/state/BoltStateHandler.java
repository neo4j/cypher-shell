package org.neo4j.shell.state;

import org.neo4j.driver.internal.logging.ConsoleLogging;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.StatementRunner;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.Connector;
import org.neo4j.shell.TransactionHandler;
import org.neo4j.shell.TriFunction;
import org.neo4j.shell.exception.CommandException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Handles interactions with the driver
 */
public class BoltStateHandler implements TransactionHandler, Connector {
    private final TriFunction<String, AuthToken, Config, Driver> driverProvider;
    protected Driver driver;
    protected Session session;
    protected Transaction tx = null;

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
        tx = session.beginTransaction();
    }

    @Override
    public void commitTransaction() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (!isTransactionOpen()) {
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
        if (!isTransactionOpen()) {
            throw new CommandException("There is no open transaction to rollback");
        }
        tx.failure();
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

        final AuthToken authToken;
        if (!connectionConfig.username().isEmpty() && !connectionConfig.password().isEmpty()) {
            authToken = AuthTokens.basic(connectionConfig.username(), connectionConfig.password());
        } else {
            authToken = null;
        }

        try {
            driver = getDriver(connectionConfig, authToken);
            session = driver.session();
            // Bug in Java driver forces us to run a statement to make it actually connect
            session.run("RETURN 1").consume();
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
    public Optional<BoltResult> runCypher(@Nonnull String cypher,
                                          @Nonnull Map<String, Object> queryParams) throws CommandException {
        StatementRunner statementRunner = getStatementRunner();
        StatementResult statementResult = statementRunner.run(cypher, queryParams);

        if (statementResult == null) {
            return Optional.empty();
        }

        // calling list()/consume() is what actually executes cypher on the server
        return Optional.of(new BoltResult(statementResult.list(), statementResult.consume()));
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
                tx.failure();
                tx.close();
                tx = null;
            }
        }
    }

    /**
     * Returns an appropriate runner, depending on the current transaction state.
     *
     * @return a statementrunner to execute cypher, or throws an exception if not connected
     */
    @Nonnull
    private StatementRunner getStatementRunner() throws CommandException {
        if (!isConnected()) {
            throw new CommandException("Not connected to Neo4j");
        }
        if (isTransactionOpen()) {
            return tx;
        }
        return session;
    }

    private Driver getDriver(@Nonnull ConnectionConfig connectionConfig, @Nullable AuthToken authToken) {
        Config config = Config.build()
                .withLogging(new ConsoleLogging(Level.OFF))
                .withEncryptionLevel(connectionConfig.encryption()).toConfig();
        return driverProvider.apply(connectionConfig.driverUrl(), authToken, config);
    }
}
