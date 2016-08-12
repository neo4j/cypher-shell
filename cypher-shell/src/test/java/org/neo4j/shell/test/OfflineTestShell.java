package org.neo4j.shell.test;


import org.neo4j.shell.CypherShell;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.log.Logger;
import org.neo4j.shell.prettyprint.PrettyPrinter;
import org.neo4j.shell.state.BoltStateHandler;
import org.neo4j.shell.state.OfflineBoltStateHandler;

import static org.mockito.Mockito.mock;

/**
 * This class initializes a {@link CypherShell} with a fake
 * {@link org.neo4j.shell.state.BoltStateHandler} which allows for faked sessions and faked results to test some basic
 * shell functionality without requiring a full integration test.
 */
public class OfflineTestShell extends CypherShell {

    public OfflineTestShell(Logger logger) {
        this(logger, new OfflineBoltStateHandler(), mock(PrettyPrinter.class));
    }

    public OfflineTestShell(Logger logger, BoltStateHandler boltStateHandler, PrettyPrinter prettyPrinter) {
        super(logger, boltStateHandler, prettyPrinter);
    }

    @Override
    protected void addRuntimeHookToResetShell() {
        //Do Nothing
    }
}
