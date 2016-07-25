package org.neo4j.shell.test.bolt;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.TypeSystem;

import java.util.Map;

/**
 * A fake transaction
 */
public class FakeTransaction implements Transaction {

    private Boolean success = null;
    private boolean open = true;

    @Override
    public void success() {
        this.success = true;
    }

    @Override
    public void failure() {
        this.success = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        this.open = false;
    }

    @Override
    public StatementResult run(String cypher, Value parameters) {
        return new FakeStatementResult();
    }

    @Override
    public StatementResult run(String cypher, Map<String, Object> statementParameters) {
        return new FakeStatementResult();
    }

    @Override
    public StatementResult run(String cypher, Record statementParameters) {
        return new FakeStatementResult();
    }

    @Override
    public StatementResult run(String cypher) {
        return new FakeStatementResult();
    }

    @Override
    public StatementResult run(Statement statement) {
        return new FakeStatementResult();
    }

    @Override
    public TypeSystem typeSystem() {
        return null;
    }

    public boolean isSuccess() {
        if (success == null) {
            throw new NullPointerException("Success/Failure was never called");
        }
        return success;
    }
}
