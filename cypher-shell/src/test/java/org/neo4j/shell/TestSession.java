package org.neo4j.shell;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.TypeSystem;

import java.util.Map;

public class TestSession implements Session {
    private boolean open = true;

    @Override
    public Transaction beginTransaction() {
        return new TestTransaction();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() {
        open = false;
    }

    @Override
    public StatementResult run(String statementTemplate, Value parameters) {
        return new TestStatementResult();
    }

    @Override
    public StatementResult run(String statementTemplate, Map<String, Object> statementParameters) {
        return new TestStatementResult();
    }

    @Override
    public StatementResult run(String statementTemplate, Record statementParameters) {
        return new TestStatementResult();
    }

    @Override
    public StatementResult run(String statementTemplate) {
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
}
