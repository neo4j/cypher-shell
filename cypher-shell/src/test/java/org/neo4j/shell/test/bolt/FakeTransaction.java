package org.neo4j.shell.test.bolt;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.TypeSystem;

import java.util.concurrent.CompletionStage;
import java.util.Map;

public class FakeTransaction implements Transaction {
    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void success() {

    }

    @Override
    public void failure() {

    }

    @Override
    public void close() {

    }

    @Override
    public CompletionStage<Void> commitAsync() {
        return null;
    }

    @Override
    public CompletionStage<Void> rollbackAsync() {
        return null;
    }

    @Override
    public StatementResult run(String query, Value parameters) {
        return null;
    }

    @Override
    public StatementResult run(String query, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public StatementResult run(String query, Record parameters) {
        return null;
    }

    @Override
    public StatementResult run(String query) {
        return null;
    }

    @Override
    public StatementResult run(Statement statement) {
        return null;
    }

    @Override
    public TypeSystem typeSystem() {
        return null;
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync( String statement, Map<String,Object> parameters)
    {
        return null;
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(String statementTemplate, Value parameters) {
        return null;
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(String statementTemplate, Record statementParameters) {
        return null;
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(String statementTemplate) {
        return null;
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(Statement statement) {
        return null;
    }
}
