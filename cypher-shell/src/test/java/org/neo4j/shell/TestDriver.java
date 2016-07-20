package org.neo4j.shell;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.Neo4jException;

/**
 * A fake driver which returns a fake session
 */
public class TestDriver implements Driver {
    @Override
    public Session session() {
        return new TestSession();
    }

    @Override
    public void close() throws Neo4jException {

    }
}
