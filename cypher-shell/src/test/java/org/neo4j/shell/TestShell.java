package org.neo4j.shell;


import org.neo4j.shell.exception.CommandException;

public class TestShell extends CypherShell {

    public TestShell() {
        super(new ConnectionConfig("", 1, "", ""));
    }

    @Override
    public void connect(ConnectionConfig connectionConfig) throws CommandException {
        this.session = new TestSession();
    }

    @Override
    public void disconnect() throws CommandException {
        this.session = null;
    }
}
