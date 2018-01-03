package org.neo4j.shell.test.bolt;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.Neo4jException;

import java.util.concurrent.CompletionStage;

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
    public Session session(AccessMode mode) {
        return new FakeSession();
    }

    @Override
    public Session session(String bookmark) {
        return new FakeSession();
    }

    @Override
    public Session session(AccessMode mode, String bookmark) {
        return new FakeSession();
    }

    @Override
    public Session session(Iterable<String> bookmarks) {
        return new FakeSession();
    }

    @Override
    public Session session(AccessMode mode, Iterable<String> bookmarks) {
        return new FakeSession();
    }

    @Override
    public void close() throws Neo4jException {
    }

    @Override
    public CompletionStage<Void> closeAsync() {
        return null;
    }
}
