package org.neo4j.shell.state;

import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.summary.ResultSummary;

/**
 * Wrapper around {@link Result}. Might or might not be materialized.
 */
public class StatementBoltResult implements BoltResult {

    private final Result result;

    public StatementBoltResult(Result result) {
        this.result = result;
    }

    @Nonnull
    @Override
    public List<String> getKeys() {
        return result.keys();
    }

    @Nonnull
    @Override
    public List<Record> getRecords() {
        return result.list();
    }

    @Nonnull
    @Override
    public Iterator<Record> iterate() {
        return result;
    }

    @Nonnull
    @Override
    public ResultSummary getSummary() {
        return result.consume();
    }
}
