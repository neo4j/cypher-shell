package org.neo4j.shell;


import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.log.Logger;

import javax.annotation.Nonnull;

public class TestShell extends CypherShell {

    public TestShell(Logger logger) {
        super(logger);
    }

    @Override
    public void connect(@Nonnull ConnectionConfig connectionConfig) throws CommandException {
        this.session = new TestSession();
    }

    @Override
    public void disconnect() throws CommandException {
        this.session = null;
    }
}
