package org.neo4j.shell.state;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.summary.ResultSummary;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper around {@link StatementResult}. Might or might not be materialized.
 */
public class StatementBoltResult implements BoltResult {

    private final StatementResult result;

    public StatementBoltResult(StatementResult result) {
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
        return result.summary();
    }
}
