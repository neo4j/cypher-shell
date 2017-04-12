package org.neo4j.shell.prettyprint;

import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.internal.util.Iterables;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TableOutputFormatter implements OutputFormatter {

    @Override
    @Nonnull
    public String format(@Nonnull final BoltResult result) {
        List<Value> data = result.getRecords().stream().map(r -> new MapValue(r.<Value>asMap(v -> v))).collect(Collectors.toList());
        return formatValues(data);
    }

    @Nonnull
    String formatValues(@Nonnull List<Value> data) {
        if (data.isEmpty()) return "";
        List<String> columns = Iterables.asList(data.get(0).keys());
        if (columns.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        Map<String, Integer> columnSizes = calculateColumnSizes(columns, data);
        String headerLine = createString(columns, columnSizes);
        int lineWidth = headerLine.length() - 2;
        String dashes = "+" + OutputFormatter.repeat('-', lineWidth) + "+";

        sb.append(dashes).append(NEWLINE);
        sb.append(headerLine).append(NEWLINE);
        sb.append(dashes).append(NEWLINE);

        for (Value record : data) {
            sb.append(createString(columns, columnSizes, record)).append(NEWLINE);
        }
        sb.append(dashes).append(NEWLINE);
        return sb.toString();
    }

    @Nonnull
    public String formatFooter(@Nonnull BoltResult result) {
        int rows = result.getRecords().size();
        ResultSummary summary = result.getSummary();
        return String.format("%d row%s available after %d ms, consumed after another %d ms", rows, rows != 1 ? "s" : "", summary.resultAvailableAfter(MILLISECONDS), summary.resultConsumedAfter(MILLISECONDS));
    }

    @Nonnull
    private String createString(@Nonnull List<String> columns, @Nonnull Map<String, Integer> columnSizes, @Nonnull Value m) {
        StringBuilder sb = new StringBuilder("|");
        for (String column : columns) {
            sb.append(" ");
            Integer length = columnSizes.get(column);
            String txt = formatValue(m.get(column));
            String value = OutputFormatter.rightPad(txt, length);
            sb.append(value);
            sb.append(" |");
        }
        return sb.toString();
    }

    @Nonnull
    private String createString(@Nonnull List<String> columns, @Nonnull Map<String, Integer> columnSizes) {
        StringBuilder sb = new StringBuilder("|");
        for (String column : columns) {
            sb.append(" ");
            sb.append(OutputFormatter.rightPad(column, columnSizes.get(column)));
            sb.append(" |");
        }
        return sb.toString();
    }

    @Nonnull
    private Map<String, Integer> calculateColumnSizes(@Nonnull List<String> columns, @Nonnull List<Value> data) {
        Map<String, Integer> columnSizes = new LinkedHashMap<>();
        for (String column : columns) {
            columnSizes.put(column, column.length());
        }
        for (Value record : data) {
            for (String column : columns) {
                int len = formatValue(record.get(column)).length();
                int existing = columnSizes.get(column);
                if (existing < len) {
                    columnSizes.put(column, len);
                }
            }
        }
        return columnSizes;
    }

    @Override
    @Nonnull
    public String formatInfo(@Nonnull ResultSummary summary) {
        Map<String, Value> info = OutputFormatter.info(summary);
        return formatValues(Collections.singletonList(new MapValue(info)));
    }

    @Override
    @Nonnull
    public String formatPlan(@Nullable ResultSummary summary) {
        if (summary == null || !summary.hasPlan()) return "";
        return new TablePlanFormatter().formatPlan(summary.plan());
    }
}
