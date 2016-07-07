package org.neo4j.shell;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.util.Function;

import java.util.List;

public class TestStatementResult implements StatementResult {
    @Override
    public List<String> keys() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Record next() {
        return null;
    }

    @Override
    public Record single() throws NoSuchRecordException {
        return null;
    }

    @Override
    public Record peek() {
        return null;
    }

    @Override
    public List<Record> list() {
        return null;
    }

    @Override
    public <T> List<T> list(Function<Record, T> mapFunction) {
        return null;
    }

    @Override
    public ResultSummary consume() {
        return null;
    }
}
