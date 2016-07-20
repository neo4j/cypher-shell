package org.neo4j.shell;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.TypeSystem;

import java.util.Map;

public class TestTransaction implements Transaction {

    private String lastCypherStatement = null;
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
        lastCypherStatement = cypher;
        return new TestStatementResult();
    }

    @Override
    public StatementResult run(String cypher, Map<String, Object> statementParameters) {
        lastCypherStatement = cypher;
        return new TestStatementResult();
    }

    @Override
    public StatementResult run(String cypher, Record statementParameters) {
        lastCypherStatement = cypher;
        return new TestStatementResult();
    }

    @Override
    public StatementResult run(String cypher) {
        lastCypherStatement = cypher;
        return new TestStatementResult();
    }

    @Override
    public StatementResult run(Statement statement) {
        return new TestStatementResult();
    }

    @Override
    public TypeSystem typeSystem() {
        return null;
    }

    public String getLastCypherStatement() {
        return lastCypherStatement;
    }

    public boolean isSuccess() {
        if (success == null) {
            throw new NullPointerException("Success/Failure was never called");
        }
        return success;
    }
}
