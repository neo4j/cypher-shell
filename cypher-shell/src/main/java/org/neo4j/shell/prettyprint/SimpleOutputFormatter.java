package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SimpleOutputFormatter implements OutputFormatter {

    @Override
    public void format(@Nonnull BoltResult result, @Nonnull LinePrinter output) {
        Iterator<Record> records = result.iterate();
        if (records.hasNext()) {
            Record firstRow = records.next();
            output.printOut(String.join(COMMA_SEPARATOR, firstRow.keys()));
            output.printOut(formatRecord(firstRow));
            while (records.hasNext()) {
                output.printOut(formatRecord(records.next()));
            }
        }
    }

    @Nonnull
    private String formatRecord(@Nonnull final Record record) {
        return record.values().stream().map(this::formatValue).collect(Collectors.joining(COMMA_SEPARATOR));
    }

    @Nonnull
    @Override
    public String formatInfo(@Nonnull ResultSummary summary) {
        if (!summary.hasPlan()) {
            return "";
        }
        Map<String, Value> info = OutputFormatter.info(summary);
        return info.entrySet().stream()
                .map( e -> String.format("%s: %s",e.getKey(),e.getValue())).collect(Collectors.joining(NEWLINE));
    }

    @Override
    public Set<Capablities> capabilities() {
        return EnumSet.of(Capablities.info, Capablities.statistics, Capablities.result);
    }
}
