package org.neo4j.shell;


import javax.annotation.Nonnull;

public abstract class TestShell extends CypherShell {

    TestShell() {
        super("", 1, "", "");
    }

    abstract void executeCypher(@Nonnull final String cypher);

    @Override
    public void connect(@Nonnull String host, int port,
                        @Nonnull String username, @Nonnull String password) throws CommandException {
        throw new RuntimeException("Test shell can't connect");
    }

    @Override
    public void disconnect() throws CommandException {
        throw new RuntimeException("Test shell can't disconnect");
    }

    @Override
    boolean isConnected() {
        return true;
    }
}
