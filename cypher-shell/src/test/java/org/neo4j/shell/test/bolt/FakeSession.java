package org.neo4j.shell.test.bolt;

import java.util.Map;

import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;

/**
 * A fake session which returns fake StatementResults
 */
public class FakeSession implements Session {
    private boolean open = true;

    @Override
    public Transaction beginTransaction() {
        return new FakeTransaction();
    }

    @Override
    public Transaction beginTransaction(TransactionConfig config) {
        return new FakeTransaction();
    }

    @Override
    public <T> T readTransaction(TransactionWork<T> work) {
        return null;
    }

    @Override
    public <T> T readTransaction(TransactionWork<T> work, TransactionConfig config ) {
        return null;
    }

    @Override
    public <T> T writeTransaction(TransactionWork<T> work) {
        return null;
    }

    @Override
    public <T> T writeTransaction(TransactionWork<T> work, TransactionConfig config) {
        return null;
    }

    @Override
    public Result run(String statement, TransactionConfig config) {
        return FakeResult.parseStatement(statement);
    }

    @Override
    public Result run(String statement, Map<String,Object> parameters, TransactionConfig config) {
        return FakeResult.parseStatement(statement);
    }

    @Override
    public Result run(Query statement, TransactionConfig config) {
        return new FakeResult();
    }

    @Override
    public Bookmark lastBookmark() {
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
    public Result run(String statementTemplate, Value parameters) {
        return FakeResult.parseStatement(statementTemplate);
    }

    @Override
    public Result run(String statementTemplate, Map<String, Object> statementParameters) {
        return FakeResult.parseStatement(statementTemplate);
    }

    @Override
    public Result run(String statementTemplate, Record statementParameters) {
        return FakeResult.parseStatement(statementTemplate);
    }

    @Override
    public Result run(String statementTemplate) {
        return FakeResult.parseStatement(statementTemplate);
    }

    @Override
    public Result run(Query statement) {
        return new FakeResult();
    }
}
