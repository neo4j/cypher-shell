package org.neo4j.shell.state;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.summary.ResultSummary;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A class holds the result from executing some Cypher.
 */
public class BoltResult {
    private final List<Record> records;
    private StatementResult statementResult;

    public BoltResult(@Nonnull List<Record> records, StatementResult statementResult) {
        //Not calling statementResult.list() because it executes cypher in the server
        this.records = records;
        this.statementResult = statementResult;
    }

    @Nonnull
    public List<Record> getRecords() {
        return records;
    }

    @Nonnull
    public List<String> getKeys() {
        return statementResult.keys();
    }

    @Nonnull
    public ResultSummary getSummary() {
        return statementResult.summary();
    }
}
