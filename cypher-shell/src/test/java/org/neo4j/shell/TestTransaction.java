package org.neo4j.shell;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.TypeSystem;

import java.util.Map;

public class TestTransaction implements Transaction {

    private String lastCypherStatement = null;

    @Override
    public void success() {

    }

    @Override
    public void failure() {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() {

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
}
