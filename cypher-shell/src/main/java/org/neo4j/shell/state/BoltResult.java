package org.neo4j.shell.state;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.summary.ResultSummary;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A class holds the result from executing some Cypher.
 */
public class BoltResult {
    private final List<Record> records;
    private final ResultSummary summary;

    public BoltResult(@Nonnull List<Record> records, @Nonnull ResultSummary summary) {

        this.records = records;
        this.summary = summary;
    }

    @Nonnull
    public List<Record> getRecords() {
        return records;
    }

    @Nonnull
    public ResultSummary getSummary() {
        return summary;
    }
}
