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
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.summary.ResultSummary;
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
        int[] columnSizes = calculateColumnSizes(columns, topRecords, records.hasNext());

        int totalWidth = 1;
        for (int columnSize : columnSizes) {
            totalWidth += columnSize + 3;
        }

        StringBuilder builder = new StringBuilder(totalWidth);
        String headerLine = formatRow(builder, columnSizes, columns, false);
        int lineWidth = totalWidth - 2;
        String dashes = "+" + OutputFormatter.repeat('-', lineWidth) + "+";

        output.printOut(dashes);
        output.printOut(headerLine);
        output.printOut(dashes);

        int numberOfRows = 0;
        for (Record record : topRecords) {
            output.printOut(formatRecord(builder, columnSizes, record, numberOfRows < topRecords.size() - 1 || records.hasNext()));
            numberOfRows++;
        }
        while (records.hasNext()) {
            Record next = records.next();
            output.printOut( formatRecord( builder, columnSizes, next, records.hasNext()));
            numberOfRows++;
        }
        output.printOut(String.format("%s%n", dashes));
        return numberOfRows;
    }

    /**
     * Calculate the size of the columns for table formatting
     * @param columns the column names
     * @param data (sample) data
     * @param moreDataAfterSamples if there is more data that should be written into the table after `data`
     * @return the column sizes
     */
    private int[] calculateColumnSizes(@Nonnull String[] columns, @Nonnull List<Record> data, boolean moreDataAfterSamples) {
        int[] columnSizes = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnSizes[i] = columns[i].length();
        }
        for (Record record : data) {
            for (int i = 0; i < columns.length; i++) {
                int len = columnLengthForValue(record.get(i), moreDataAfterSamples);
                if (columnSizes[i] < len) {
                    columnSizes[i] = len;
                }
            }
        }
        return columnSizes;
    }

    /**
     * The length of a column, where Numbers are always getting enough space to fit the highest number possible.
     *
     * @param value the value to calculate the length for
     * @param moreDataAfterSamples if there is more data that should be written into the table after `data`
     * @return the column size for this value.
     */
    private int columnLengthForValue(Value value, boolean moreDataAfterSamples) {
        if (value instanceof NumberValueAdapter && moreDataAfterSamples) {
            return 19; // The number of digits of Long.Max
        } else {
            return formatValue(value).length();
        }
    }

    private String formatRecord(StringBuilder sb, int[] columnSizes, Record record, boolean appendDashes) {
        sb.setLength(0);
        return formatRow(sb, columnSizes, formatValues(record), appendDashes);
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
     * @param appendDashes whether do have a line of dashes to separate the new row
     * @return the String result
     */
    private String formatRow(StringBuilder sb, int[] columnSizes, String[] row, boolean appendDashes) {
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
            formatRow(sb, columnSizes, row, appendDashes);
        }
        else if (appendDashes)
        {
            int linewidth = 0;
            for ( int columnSize : columnSizes )
            {
                linewidth += columnSize;
            }
            sb.append( OutputFormatter.NEWLINE );
            sb.append( "| " );
            sb.append( OutputFormatter.repeat( '-', linewidth + (columnSizes.length - 1) * 3 ) );
            sb.append( " |" );
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
