package org.neo4j.shell.test.bolt;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Metrics;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.async.AsyncSession;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.types.TypeSystem;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class FakeDriver implements Driver {
    @Override
    public boolean isEncrypted() {
        return false;
    }

    @Override
    public Session session() {
        return new FakeSession();
    }

    @Override
    public Session session( SessionConfig sessionConfig) {
        return new FakeSession();
    }

    @Override
    public void close() throws Neo4jException {
    }

    @Override
    public CompletionStage<Void> closeAsync() {
        return null;
    }

    @Override
    public Metrics metrics() {
        return null;
    }

    @Override
    public boolean isMetricsEnabled() {
        return false;
    }

    @Override
    public RxSession rxSession() {
        return null;
    }

    @Override
    public RxSession rxSession( SessionConfig sessionConfig ) {
        return null;
    }

    @Override
    public AsyncSession asyncSession() {
        return null;
    }

    @Override
    public AsyncSession asyncSession( SessionConfig sessionConfig ) {
        return null;
    }

    @Override
    public TypeSystem defaultTypeSystem() {
        return null;
    }

    @Override
    public void verifyConnectivity() {
    }

    @Override
    public CompletionStage<Void> verifyConnectivityAsync() {
        return null;
    }

    @Override
    public boolean supportsMultiDb()
    {
        return true;
    }

    @Override
    public CompletionStage<Boolean> supportsMultiDbAsync()
    {
        return completedFuture(true);
    }
}
