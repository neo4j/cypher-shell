package org.neo4j.shell;

import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;

public class ThrowingShell extends TestShell {
    public ThrowingShell(Logger logger) {
        super(logger);
    }

    @Override
    protected void executeCypher(@Nonnull String line) {
        throw new RuntimeException("Unexpected cypher execution");
    }
}
