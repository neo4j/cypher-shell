package org.neo4j.shell.state;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.shell.ConnectionConfig;
import org.neo4j.shell.TestSession;
import org.neo4j.shell.exception.CommandException;

import javax.annotation.Nonnull;

/**
 * Bolt state with faked bolt interactions
 */
public class OfflineBoltStateHandler extends BoltStateHandler {
    @Override
    public void connect(@Nonnull ConnectionConfig connectionConfig) throws CommandException {
        connect();
    }

    @Override
    public void disconnect() throws CommandException {
        this.session = null;
    }

    public Transaction getCurrentTransaction() {
        return tx;
    }

    public Session getCurrentSession() {
        return session;
    }

    public void connect() {
        this.session = new TestSession();
    }
}
