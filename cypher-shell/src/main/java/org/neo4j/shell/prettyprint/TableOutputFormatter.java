package org.neo4j.shell.prettyprint;

import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TableOutputFormatter implements OutputFormatter {

    private static final int SAMPLE_ROWS = 100;
    private final int maxWidth;
    private final boolean wrap;

    public TableOutputFormatter(int maxWidth, boolean wrap) {
        this.maxWidth = maxWidth;
        this.wrap = wrap;
    }

    @Override
    public void format(@Nonnull BoltResult result, @Nonnull Consumer<String> output) {
        Iterator<Record> rows = result.iterate();
        if (!rows.hasNext()) return;


        Record firstRow = rows.next();
        String[] columns = firstRow.keys().toArray(new String[0]);
        if (columns.length == 0) return;

        List<Record> topRows = sampleRows(rows, firstRow, SAMPLE_ROWS);

        formatValues(columns,topRows, rows, output);
    }

    private List<Record> sampleRows(Iterator<Record> rows, Record firstRow, int count) {
        List<Record> topRows = new ArrayList<>(count);
        topRows.add(firstRow);
        while (rows.hasNext() && topRows.size() < count) topRows.add(rows.next());
        return topRows;
    }

    private MapValue rowToValue(Record firstRow) {
        return new MapValue(firstRow.asMap(v -> v));
    }

    private void formatValues(String[] columns, @Nonnull List<Record> topRows, Iterator<Record> records, Consumer<String> output) {
        int[] columnSizes = calculateColumnSizes(columns, topRows);

        int totalWidth = 1;
        for (int columnSize : columnSizes) totalWidth += columnSize + 3;

        if (maxWidth != -1) {
            float ratio = (float)maxWidth / (float)totalWidth;
            totalWidth = 1;
            for (int i = 0; i < columnSizes.length; i++) {
                columnSizes[i] = Math.round(columnSizes[i]*ratio); // todo better distribution (of remainder)
                totalWidth += columnSizes[i] + 3;
            }
        }

        StringBuilder builder = new StringBuilder(totalWidth);
        String headerLine = createString(columns, columnSizes, builder);
        int lineWidth = totalWidth - 2;
        String dashes = "+" + OutputFormatter.repeat('-', lineWidth) + "+";

        output.accept(dashes);
        output.accept(headerLine);
        output.accept(dashes);

        for (Record record : topRows) {
            output.accept(createString(columns, columnSizes, record, builder));
        }
        while (records.hasNext()) {
            output.accept(createString(columns, columnSizes, records.next(), builder));
        }
        output.accept(dashes);
    }

    @Nonnull
    public String formatFooter(@Nonnull BoltResult result) {
        int rows = result.getRecords().size();
        ResultSummary summary = result.getSummary();
        return String.format("%d row%s available after %d ms, consumed after another %d ms", rows, rows != 1 ? "s" : "", summary.resultAvailableAfter(MILLISECONDS), summary.resultConsumedAfter(MILLISECONDS));
    }

    @Nonnull
    private String createString(@Nonnull String[] columns, @Nonnull int[] columnSizes, @Nonnull Record m, StringBuilder sb) {
        sb.setLength(0);
        String[] row = new String[columns.length];
        for (int i = 0; i < row.length; i++) {
            row[i] = formatValue(m.get(i));
        }
        formatRow(sb, columnSizes, row);
        return sb.toString();
    }

    private void formatRow(StringBuilder sb, int[] columnSizes, String[] row) {
        sb.append("|");
        boolean remainder = false;
        for (int i = 0; i < row.length; i++) {
            sb.append(" ");
            int length = columnSizes[i];
            String txt = row[i];
            if (txt != null) {
                if (txt.length() > length) {
                    row[i] = txt.substring(length);
                    remainder = true;
                } else row[i] = null;
                sb.append(OutputFormatter.rightPad(txt, length));
            } else {
                sb.append(OutputFormatter.repeat(' ', length));
            }
            sb.append(" |");
        }
        if (wrap && remainder) {
            sb.append(OutputFormatter.NEWLINE);
            formatRow(sb, columnSizes, row);
        }
    }

    @Nonnull
    private String createString(@Nonnull String[] columns, @Nonnull int[] columnSizes, StringBuilder sb) {
        sb.setLength(0);
        sb.append("|");
        for (int i = 0; i < columns.length; i++) {
            sb.append(" ");
            sb.append(OutputFormatter.rightPad(columns[i], columnSizes[i]));
            sb.append(" |");
        }
        return sb.toString();
    }

    @Nonnull
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

    @Override
    @Nonnull
    public String formatInfo(@Nonnull ResultSummary summary) {
        Map<String, Value> info = OutputFormatter.info(summary);
        if (info.isEmpty()) return "";
        String[] columns = info.keySet().toArray(new String[info.size()]);
        StringBuilder sb = new StringBuilder();
        Record record = new InternalRecord(asList(columns), info.values().toArray(new Value[info.size()]));
        formatValues(columns, Collections.singletonList(record), Collections.emptyIterator(), sb::append);
        return sb.toString();
    }

    @Override
    @Nonnull
    public String formatPlan(@Nullable ResultSummary summary) {
        if (summary == null || !summary.hasPlan()) return "";
        return new TablePlanFormatter().formatPlan(summary.plan());
    }

    @Override
    public Set<Capablities> capabilities() {
        return EnumSet.allOf(Capablities.class);
    }
}
