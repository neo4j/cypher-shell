package org.neo4j.shell;

import org.neo4j.shell.log.Logger;
import org.neo4j.shell.prettyprint.PrettyPrinter;
import org.neo4j.shell.state.BoltStateHandler;

/**
 * This class initializes a {@link CypherShell} with a fake
 * {@link org.neo4j.shell.state.BoltStateHandler} which allows for faked sessions and faked results to test some basic
 * shell functionality without requiring a full integration test.
 */
public class OfflineTestShell extends CypherShell {

    public OfflineTestShell(Logger logger, BoltStateHandler boltStateHandler, PrettyPrinter prettyPrinter) {
        super(logger, boltStateHandler, prettyPrinter, new ShellParameterMap());
    }

    @Override
    protected void addRuntimeHookToResetShell() {
        //Do Nothing
    }
}
