package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.SummaryCounters;
import org.neo4j.shell.cli.Format;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticsCollector {
    private Format format;

    public StatisticsCollector(@Nonnull Format format) {
        this.format = format;
    }

    public String collect(@Nonnull ResultSummary summary) {
        if (Format.VERBOSE == format) {
            return collectStatistics(summary);
        } else {
            return "";
        }
    }

    private String collectStatistics(@Nonnull ResultSummary summary) {
        List<String> statistics = new ArrayList<>();
        SummaryCounters counters = summary.counters();
        if (counters.nodesCreated() != 0) {
            statistics.add(String.format("Added %d nodes", counters.nodesCreated()));
        }
        if (counters.nodesDeleted() != 0) {
            statistics.add(String.format("Deleted %d nodes", counters.nodesDeleted()));
        }
        if (counters.relationshipsCreated() != 0) {
            statistics.add(String.format("Created %d relationships", counters.relationshipsCreated()));
        }
        if (counters.relationshipsDeleted() != 0) {
            statistics.add(String.format("Deleted %d relationships", counters.relationshipsDeleted()));
        }
        if (counters.propertiesSet() != 0) {
            statistics.add(String.format("Set %d properties", counters.propertiesSet()));
        }
        if (counters.labelsAdded() != 0) {
            statistics.add(String.format("Added %d labels", counters.labelsAdded()));
        }
        if (counters.labelsRemoved() != 0) {
            statistics.add(String.format("Removed %d labels", counters.labelsRemoved()));
        }
        if (counters.indexesAdded() != 0) {
            statistics.add(String.format("Added %d indexes", counters.indexesAdded()));
        }
        if (counters.indexesRemoved() != 0) {
            statistics.add(String.format("Removed %d indexes", counters.indexesRemoved()));
        }
        if (counters.constraintsAdded() != 0) {
            statistics.add(String.format("Added %d constraints", counters.constraintsAdded()));
        }
        if (counters.constraintsRemoved() != 0) {
            statistics.add(String.format("Removed %d constraints", counters.constraintsRemoved()));
        }
        return statistics.stream().collect(Collectors.joining(", "));
    }
}
