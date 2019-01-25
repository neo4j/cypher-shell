package org.neo4j.shell.prettyprint;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import static java.lang.System.lineSeparator;

public class SimpleOutputFormatter implements OutputFormatter {

    @Override
    @Nonnull
    public String format(@Nonnull final BoltResult result) {
        StringBuilder sb = new StringBuilder();
        List<Record> records = result.getRecords();
        if (!records.isEmpty()) {
            sb.append( String.join( COMMA_SEPARATOR, records.get( 0 ).keys() ) );
            sb.append(lineSeparator());
            sb.append(records.stream().map(this::formatRecord).collect(Collectors.joining(NEWLINE)));
        }
        return sb.toString();
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
}
