package org.neo4j.shell.test.bolt;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.Neo4jException;

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
        return null;
    }

    @Override
    public Session session( String bookmark )
    {
        return null;
    }

    @Override
    public Session session( AccessMode mode, String bookmark )
    {
        return null;
    }

    @Override
    public void close() throws Neo4jException {
    }
}
