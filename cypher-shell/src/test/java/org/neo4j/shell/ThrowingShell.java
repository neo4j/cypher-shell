package org.neo4j.shell;

import org.neo4j.shell.TestShell;

import javax.annotation.Nonnull;

public class ThrowingShell extends TestShell {
    @Override
    protected void executeCypher(@Nonnull String line) {
        throw new RuntimeException("Unexpected cypher execution");
    }
}
