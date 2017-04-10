package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.Record;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TableOutputFormatter implements OutputFormatter {

    @Override
    @Nonnull
    public String format(@Nonnull final BoltResult result) {
        List<Record> data = result.getRecords();
        if (data.isEmpty()) return "";
        List<String> columns = data.get(0).keys();
        if (columns.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        Map<String, Integer> columnSizes = calculateColumnSizes(columns, data);
        String headerLine = createString(columns, columnSizes);
        int lineWidth = headerLine.length() - 2;
        String dashes = "+" + OutputFormatter.repeat('-', lineWidth) + "+";

        String row = (data.size() > 1) ? "rows" : "row";
        String footer = String.format("%d %s", data.size(), row);

        sb.append(dashes).append(NEWLINE);
        sb.append(headerLine).append(NEWLINE);
        sb.append(dashes).append(NEWLINE);

        for (Record record : data) {
            sb.append(createString(columns, columnSizes, record)).append(NEWLINE);
        }
        sb.append(dashes).append(NEWLINE);
        sb.append(footer).append(NEWLINE);
        return sb.toString();
    }

    @Nonnull private String createString(@Nonnull List<String> columns, @Nonnull Map<String, Integer> columnSizes, @Nonnull Record m) {
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

    @Nonnull private String createString(@Nonnull List<String> columns, @Nonnull Map<String, Integer> columnSizes) {
        StringBuilder sb = new StringBuilder("|");
        for (String column : columns) {
            sb.append(" ");
            sb.append(OutputFormatter.rightPad(column, columnSizes.get(column)));
            sb.append(" |");
        }
        return sb.toString();
    }

    @Nonnull private Map<String, Integer> calculateColumnSizes(@Nonnull List<String> columns, @Nonnull List<Record> data) {
        Map<String, Integer> columnSizes = new LinkedHashMap<>();
        for (String column : columns) {
            columnSizes.put(column, column.length());
        }
        for (Record record : data) {
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
}
