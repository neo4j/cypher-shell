package org.neo4j.shell.prettyprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.internal.value.NumberValueAdapter;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.shell.state.BoltResult;

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
    public int formatAndCount(@Nonnull BoltResult result, @Nonnull LinePrinter output) {
        String[] columns = result.getKeys().toArray(new String[0]);
        if (columns.length == 0) {
            return 0;
        }

        Iterator<Record> records = result.iterate();
        return formatResultAndCountRows(columns, records, output);
    }

    private List<Record> take(Iterator<Record> records, int count) {
        List<Record> topRecords = new ArrayList<>(count);
        while (records.hasNext() && topRecords.size() < count) {
            topRecords.add(records.next());
        }
        return topRecords;
    }

    private int formatResultAndCountRows(String[] columns,
                                         Iterator<Record> records,
                                         LinePrinter output) {

        List<Record> topRecords = take(records, numSampleRows);
        int[] columnSizes = calculateColumnSizes(columns, topRecords);

        int totalWidth = 1;
        for (int columnSize : columnSizes) {
            totalWidth += columnSize + 3;
        }

        StringBuilder builder = new StringBuilder(totalWidth);
        String headerLine = formatRow(builder, columnSizes, columns, new boolean[columnSizes.length]);
        int lineWidth = totalWidth - 2;
        String dashes = "+" + OutputFormatter.repeat('-', lineWidth) + "+";

        output.printOut(dashes);
        output.printOut(headerLine);
        output.printOut(dashes);

        int numberOfRows = 0;
        for (Record record : topRecords) {
            output.printOut(formatRecord(builder, columnSizes, record));
            numberOfRows++;
        }
        while (records.hasNext()) {
            output.printOut(formatRecord(builder, columnSizes, records.next()));
            numberOfRows++;
        }
        output.printOut(String.format("%s%n", dashes));
        return numberOfRows;
    }

    private int[] calculateColumnSizes(@Nonnull String[] columns, @Nonnull List<Record> data) {
        int[] columnSizes = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnSizes[i] = columns[i].length();
        }
        for (Record record : data) {
            for (int i = 0; i < columns.length; i++) {
                int len = columnLengthForValue(record.get(i));
                if (columnSizes[i] < len) {
                    columnSizes[i] = len;
                }
            }
        }
        return columnSizes;
    }

    /**
     * The length of a column, where Numbers are always getting enough space to fit the highest number possible.
     */
    private int columnLengthForValue(Value value) {
        if (value instanceof NumberValueAdapter ) {
            return 19; // The number of digits of Long.Max
        } else {
            return formatValue(value).length();
        }
    }

    private String formatRecord(StringBuilder sb, int[] columnSizes, Record record) {
        sb.setLength(0);
        return formatRow(sb, columnSizes, formatValues(record), new boolean[columnSizes.length]);
    }

    private String[] formatValues(Record record) {
        String[] row = new String[record.size()];
        for (int i = 0; i < row.length; i++) {
            row[i] = formatValue(record.get(i));
        }
        return row;
    }

    /**
     * Format one row of data.
     *
     * @param sb the StringBuilder to use
     * @param columnSizes the size of all columns
     * @param row the data
     * @param continuation for each column whether it holds the remainder of data that did not fit in the column
     * @return the String result
     */
    private String formatRow(StringBuilder sb, int[] columnSizes, String[] row, boolean[] continuation) {
        if (!continuation[0]) {
            sb.append("|");
        } else {
            sb.append("\\");
        }
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
                        continuation[i] = true;
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
            if (i == row.length -1 || !continuation[i+1]) {
                sb.append(" |");
            } else {
                sb.append(" \\");
            }
        }
        if (wrap && remainder) {
            sb.append(OutputFormatter.NEWLINE);
            formatRow(sb, columnSizes, row, continuation);
        }
        return sb.toString();
    }

    @Override
    @Nonnull
    public String formatFooter(@Nonnull BoltResult result, int numberOfRows) {
        ResultSummary summary = result.getSummary();
        return String.format("%d row%s available after %d ms, " +
                        "consumed after another %d ms", numberOfRows, numberOfRows != 1 ? "s" : "",
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
        formatResultAndCountRows(columns, Collections.singletonList(record).iterator(), line -> sb.append( line).append( OutputFormatter.NEWLINE) );
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
    public Set<Capabilities> capabilities() {
        return EnumSet.allOf(Capabilities.class);
    }
}
