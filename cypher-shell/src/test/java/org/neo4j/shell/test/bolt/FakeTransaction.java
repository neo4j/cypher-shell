package org.neo4j.shell.test.bolt;

import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;

import java.util.Map;

public class FakeTransaction implements Transaction {
    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public Result run(String query, Value parameters) {
        return null;
    }

    @Override
    public Result run(String query, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public Result run(String query, Record parameters) {
        return null;
    }

    @Override
    public Result run(String query) {
        return null;
    }

    @Override
    public Result run(Query query) {
        return null;
    }
}
