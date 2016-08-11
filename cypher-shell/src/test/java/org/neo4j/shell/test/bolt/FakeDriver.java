package org.neo4j.shell.test.bolt;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.Neo4jException;

/**
 * A fake driver which returns a fake session
 */
public class FakeDriver implements Driver {
    //    @Override
    public boolean isEncrypted() {
        throw new UnsupportedOperationException("no implementation yet");
    }

    @Override
    public Session session() {
        return new FakeSession();
    }

    @Override
    public void close() throws Neo4jException {

    }
}
