package org.neo4j.shell;


import org.neo4j.shell.exception.CommandException;

import javax.annotation.Nonnull;

public class TestShell extends CypherShell {

    public TestShell() {
        super("", 1, "", "");
    }

    public void connect() throws CommandException {
        connect("", 0, "", "");
    }

    @Override
    public void connect(@Nonnull String host, int port,
                        @Nonnull String username, @Nonnull String password) throws CommandException {
        this.session = new TestSession();
    }

    @Override
    public void disconnect() throws CommandException {
        this.session = null;
    }
}
