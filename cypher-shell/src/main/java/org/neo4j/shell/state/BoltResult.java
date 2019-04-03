package org.neo4j.shell.state;

import org.neo4j.driver.Record;
import org.neo4j.driver.summary.ResultSummary;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * The result of executing some Cypher over bolt.
 */
public interface BoltResult {

    @Nonnull
    List<String> getKeys();

    @Nonnull
    List<Record> getRecords();

    @Nonnull
    Iterator<Record> iterate();

    @Nonnull
    ResultSummary getSummary();
}
