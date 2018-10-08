package org.neo4j.shell.test.bolt;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.TypeSystem;

import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * A fake session which returns fake StatementResults
 */
public class FakeSession implements Session {
    private boolean open = true;

    @Override
    public Transaction beginTransaction() {
        return null;
    }

    @Override
    public Transaction beginTransaction( TransactionConfig config )
    {
        return null;
    }

    @Override
    public Transaction beginTransaction(String bookmark) {
        return null;
    }

    @Override
    public CompletionStage<Transaction> beginTransactionAsync() {
        return null;
    }

    @Override
    public CompletionStage<Transaction> beginTransactionAsync( TransactionConfig config )
    {
        return null;
    }

    @Override
    public <T> T readTransaction(TransactionWork<T> work) {
        return null;
    }

    @Override
    public <T> T readTransaction( TransactionWork<T> work, TransactionConfig config )
    {
        return null;
    }

    @Override
    public <T> CompletionStage<T> readTransactionAsync(TransactionWork<CompletionStage<T>> work) {
        return null;
    }

    @Override
    public <T> CompletionStage<T> readTransactionAsync( TransactionWork<CompletionStage<T>> work, TransactionConfig config )
    {
        return null;
    }

    @Override
    public <T> T writeTransaction(TransactionWork<T> work) {
        return null;
    }

    @Override
    public <T> T writeTransaction( TransactionWork<T> work, TransactionConfig config )
    {
        return null;
    }

    @Override
    public <T> CompletionStage<T> writeTransactionAsync(TransactionWork<CompletionStage<T>> work) {
        return null;
    }

    @Override
    public <T> CompletionStage<T> writeTransactionAsync( TransactionWork<CompletionStage<T>> work, TransactionConfig config )
    {
        return null;
    }

    @Override
    public StatementResult run( String statement, TransactionConfig config )
    {
        return FakeStatementResult.parseStatement(statement);
    }

    @Override
    public StatementResult run( String statement, Map<String,Object> parameters, TransactionConfig config )
    {
        return FakeStatementResult.parseStatement(statement);
    }

    @Override
    public StatementResult run( Statement statement, TransactionConfig config )
    {
        return new FakeStatementResult();
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync( String statement, TransactionConfig config )
    {
        return null;
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync( String statement, Map<String,Object> parameters, TransactionConfig config )
    {
        return null;
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync( Statement statement, TransactionConfig config )
    {
        return null;
    }

    @Override
    public String lastBookmark() {
        return null;
    }

    @Override
    public void reset() {
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
    public CompletionStage<Void> closeAsync() {
        return null;
    }

    @Override
    public StatementResult run(String statementTemplate, Value parameters) {
        return FakeStatementResult.parseStatement(statementTemplate);
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(String statementTemplate, Value parameters) {
        return null;
    }

    @Override
    public StatementResult run(String statementTemplate, Map<String, Object> statementParameters) {
        return FakeStatementResult.parseStatement(statementTemplate);
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(String statementTemplate,
                                                           Map<String, Object> statementParameters) {
        return null;
    }

    @Override
    public StatementResult run(String statementTemplate, Record statementParameters) {
        return FakeStatementResult.parseStatement(statementTemplate);
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(String statementTemplate, Record statementParameters) {
        return null;
    }

    @Override
    public StatementResult run(String statementTemplate) {
        return FakeStatementResult.parseStatement(statementTemplate);
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(String statementTemplate) {
        return null;
    }

    @Override
    public StatementResult run(Statement statement) {
        return new FakeStatementResult();
    }

    @Override
    public CompletionStage<StatementResultCursor> runAsync(Statement statement) {
        return null;
    }

    @Override
    public TypeSystem typeSystem() {
        return null;
    }
}
