package org.neo4j.shell.state;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.summary.ResultSummary;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * @author mh
 * @since 26.01.18
 */
public interface BoltResult {
    @Nonnull
    List<Record> getRecords();

    @Nonnull
    Iterator<Record> iterate();

    @Nonnull
    ResultSummary getSummary();
}
