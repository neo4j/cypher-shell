package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.Record;
import org.neo4j.shell.state.BoltResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class TableOutputFormatter extends OutputFormatter {

    @Override
    public String format(@Nonnull final BoltResult result) {
        List<Record> data = result.getRecords();
        if (data.isEmpty()) return "";
        List<String> columns = data.get(0).keys();
        if (columns.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        Map<String, Integer> columnSizes = calculateColumnSizes(columns, data);
        String headerLine = createString(columns, columnSizes);
        int lineWidth = headerLine.length() - 2;
        String dashes = "+" + repeat('-', lineWidth) + "+";

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

    private String repeat(char c, int width) {
        char[] chars = new char[width];
        Arrays.fill(chars, c);
        return String.valueOf(chars);
    }

    private String createString(List<String> columns, Map<String, Integer> columnSizes, Record m) {
        StringBuilder sb = new StringBuilder("|");
        for (String column : columns) {
            sb.append(" ");
            Integer length = columnSizes.get(column);
            String txt = formatValue(m.get(column));
            String value = makeSize(txt, length);
            sb.append(value);
            sb.append(" |");
        }
        return sb.toString();
    }

    private String createString(List<String> columns, Map<String, Integer> columnSizes) {
        StringBuilder sb = new StringBuilder("|");
        for (String column : columns) {
            sb.append(" ");
            sb.append(makeSize(column, columnSizes.get(column)));
            sb.append(" |");
        }
        return sb.toString();
    }

    private Map<String, Integer> calculateColumnSizes(List<String> columns, List<Record> data) {
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

    private String makeSize(String txt, int wantedSize) {
        int actualSize = txt.length();
        if (actualSize > wantedSize) {
            return txt.substring(0, wantedSize);
        } else if (actualSize < wantedSize) {
            return txt + repeat(' ', wantedSize - actualSize);
        } else {
            return txt;
        }
    }
}
