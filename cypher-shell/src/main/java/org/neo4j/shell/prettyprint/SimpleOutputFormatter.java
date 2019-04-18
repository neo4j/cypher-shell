package org.neo4j.shell.prettyprint;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.neo4j.shell.prettyprint.OutputFormatter.Capabilities.*;

public class SimpleOutputFormatter implements OutputFormatter {

    @Override
    public int formatAndCount(@Nonnull BoltResult result, @Nonnull LinePrinter output) {
        Iterator<Record> records = result.iterate();
        int numberOfRows = 0;
        if (records.hasNext()) {
            Record firstRow = records.next();
            output.printOut(String.join(COMMA_SEPARATOR, firstRow.keys()));
            output.printOut(formatRecord(firstRow));
            numberOfRows++;
            while (records.hasNext()) {
                output.printOut(formatRecord(records.next()));
                numberOfRows++;
            }
        }
        return numberOfRows;
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
    public Set<Capabilities> capabilities() {
        return EnumSet.of(INFO, STATISTICS, RESULT);
    }
}
