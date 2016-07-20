package org.neo4j.shell;


import org.neo4j.shell.log.Logger;
import org.neo4j.shell.state.OfflineBoltStateHandler;

public class TestShell extends CypherShell {

    public TestShell(Logger logger) {
        super(logger, new OfflineBoltStateHandler());
    }
}
