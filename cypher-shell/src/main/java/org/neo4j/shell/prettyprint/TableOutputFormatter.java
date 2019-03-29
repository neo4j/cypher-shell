package org.neo4j.shell.prettyprint;

import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TableOutputFormatter implements OutputFormatter {

    private final boolean wrap;
    private final int numSampleRows;

    public TableOutputFormatter(boolean wrap, int numSampleRows) {
        this.wrap = wrap;
        this.numSampleRows = numSampleRows;
    }

    @Override
    public void format(@Nonnull BoltResult result, @Nonnull LinePrinter output) {
        String[] columns = result.getKeys().toArray(new String[0]);
        if (columns.length == 0) {
            return;
        }

        Iterator<Record> records = result.iterate();
        formatResult(columns, records, output);
    }

    private List<Record> take(Iterator<Record> records, int count) {
        List<Record> topRecords = new ArrayList<>(count);
        while (records.hasNext() && topRecords.size() < count) {
            topRecords.add(records.next());
        }
        return topRecords;
    }

    private void formatResult(String[] columns,
                              Iterator<Record> records,
                              LinePrinter output) {

        List<Record> topRecords = take(records, numSampleRows);
        int[] columnSizes = calculateColumnSizes(columns, topRecords);

        int totalWidth = 1;
        for (int columnSize : columnSizes) {
            totalWidth += columnSize + 3;
        }

        StringBuilder builder = new StringBuilder(totalWidth);
        String headerLine = formatRow(builder, columnSizes, columns);
        int lineWidth = totalWidth - 2;
        String dashes = "+" + OutputFormatter.repeat('-', lineWidth) + "+";

        output.println(dashes);
        output.println(headerLine);
        output.println(dashes);

        for (Record record : topRecords) {
            output.println(formatRecord(builder, columnSizes, record));
        }
        while (records.hasNext()) {
            output.println(formatRecord(builder, columnSizes, records.next()));
        }
        output.println(dashes);
    }

    private int[] calculateColumnSizes(@Nonnull String[] columns, @Nonnull List<Record> data) {
        int[] columnSizes = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnSizes[i] = columns[i].length();
        }
        for (Record record : data) {
            for (int i = 0; i < columns.length; i++) {
                int len = formatValue(record.get(i)).length();
                if (columnSizes[i] < len) {
                    columnSizes[i] = len;
                }
            }
        }
        return columnSizes;
    }

    private String formatRecord(StringBuilder sb, int[] columnSizes, Record record) {
        sb.setLength(0);
        return formatRow(sb, columnSizes, formatValues(record));
    }

    private String[] formatValues(Record record) {
        String[] row = new String[record.size()];
        for (int i = 0; i < row.length; i++) {
            row[i] = formatValue(record.get(i));
        }
        return row;
    }

    private String formatRow(StringBuilder sb, int[] columnSizes, String[] row) {
        sb.append("|");
        boolean remainder = false;
        for (int i = 0; i < row.length; i++) {
            sb.append(" ");
            int length = columnSizes[i];
            String txt = row[i];
            if (txt != null) {
                if (txt.length() > length) {
                    if (wrap) {
                        sb.append(txt, 0, length);
                        row[i] = txt.substring(length);
                        remainder = true;
                    } else {
                        sb.append(txt, 0, length - 1);
                        sb.append("…");
                    }
                } else {
                    row[i] = null;
                    sb.append(OutputFormatter.rightPad(txt, length));
                }
            } else {
                sb.append(OutputFormatter.repeat(' ', length));
            }
            sb.append(" |");
        }
        if (wrap && remainder) {
            sb.append(OutputFormatter.NEWLINE);
            formatRow(sb, columnSizes, row);
        }
        return sb.toString();
    }

    @Override
    @Nonnull
    public String formatFooter(@Nonnull BoltResult result) {
        int rows = result.getRecords().size();
        ResultSummary summary = result.getSummary();
        return String.format("%d row%s available after %d ms, " +
                        "consumed after another %d ms", rows, rows != 1 ? "s" : "",
                summary.resultAvailableAfter(MILLISECONDS),
                summary.resultConsumedAfter(MILLISECONDS));
    }

    @Override
    @Nonnull
    public String formatInfo(@Nonnull ResultSummary summary) {
        Map<String, Value> info = OutputFormatter.info(summary);
        if (info.isEmpty()) {
            return "";
        }
        String[] columns = info.keySet().toArray(new String[0]);
        StringBuilder sb = new StringBuilder();
        Record record = new InternalRecord(asList(columns), info.values().toArray(new Value[0]));
        formatResult(columns, Collections.singletonList(record).iterator(), sb::append);
        return sb.toString();
    }

    @Override
    @Nonnull
    public String formatPlan(@Nullable ResultSummary summary) {
        if (summary == null || !summary.hasPlan()) {
            return "";
        }
        return new TablePlanFormatter().formatPlan(summary.plan());
    }

    @Override
    public Set<Capablities> capabilities() {
        return EnumSet.allOf(Capablities.class);
    }
}
