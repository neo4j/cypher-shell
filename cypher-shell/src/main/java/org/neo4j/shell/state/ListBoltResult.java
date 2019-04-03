package org.neo4j.shell.state;

import org.neo4j.driver.Record;
import org.neo4j.driver.summary.ResultSummary;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A fully materialized Cypher result.
 */
public class ListBoltResult implements BoltResult {

    private final List<String> keys;
    private final List<Record> records;
    private final ResultSummary summary;

    public ListBoltResult(@Nonnull List<Record> records, @Nonnull ResultSummary summary) {
        this(records, summary, records.isEmpty() ? Collections.emptyList() : records.get(0).keys());
    }

    public ListBoltResult(@Nonnull List<Record> records, @Nonnull ResultSummary summary, @Nonnull List<String> keys) {
        this.keys = keys;
        this.records = records;
        this.summary = summary;
    }

    @Nonnull
    @Override
    public List<String> getKeys() {
        return keys;
    }

    @Override
    @Nonnull
    public List<Record> getRecords() {
        return records;
    }

    @Override
    @Nonnull
    public Iterator<Record> iterate() {
        return records.iterator();
    }

    @Override
    @Nonnull
    public ResultSummary getSummary() {
        return summary;
    }
}
