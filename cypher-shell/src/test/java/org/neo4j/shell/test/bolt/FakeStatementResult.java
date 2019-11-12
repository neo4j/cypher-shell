package org.neo4j.shell.test.bolt;

import org.neo4j.driver.Record;
import org.neo4j.driver.StatementResult;
import org.neo4j.driver.exceptions.NoSuchRecordException;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.shell.test.Util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A fake StatementResult with fake records and fake values
 */
class FakeStatementResult implements StatementResult {

    private final List<Record> records;
    private int currentRecord = -1;

    FakeStatementResult() {
        records = new ArrayList<>();
    }

    @Override
    public List<String> keys() {
        return records.stream().map(r -> r.keys().get(0)).collect(Collectors.toList());
    }

    @Override
    public boolean hasNext() {
        return currentRecord + 1 < records.size();
    }

    @Override
    public Record next() {
        currentRecord += 1;
        return records.get(currentRecord);
    }

    @Override
    public Record single() throws NoSuchRecordException {
        if (records.size() == 1) {
            return records.get(0);
        }
        throw new NoSuchRecordException("There are more than records");
    }

    @Override
    public Record peek() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public Stream<Record> stream()
    {
        return records.stream();
    }

    @Override
    public List<Record> list() {
        return records;
    }

    @Override
    public <T> List<T> list(Function<Record, T> mapFunction) {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public ResultSummary consume() {
        return new FakeResultSummary();
    }

    /**
     * Supports fake parsing of very limited cypher statements, only for basic test purposes
     */
    static FakeStatementResult parseStatement(@Nonnull final String statement) {

        Pattern returnAsPattern = Pattern.compile("^return (.*) as (.*)$", Pattern.CASE_INSENSITIVE);
        Pattern returnPattern = Pattern.compile("^return (.*)$", Pattern.CASE_INSENSITIVE);

        // Be careful with order here
        for (Pattern p: Arrays.asList(returnAsPattern, returnPattern)) {
            Matcher m = p.matcher(statement);
            if (m.find()) {
                String value = m.group(1);
                String key = value;
                if (m.groupCount() > 1) {
                    key = m.group(2);
                }
                FakeStatementResult statementResult = new FakeStatementResult();
                statementResult.records.add(FakeRecord.of(key, value));
                return statementResult;
            }
        }
        throw new IllegalArgumentException("No idea how to parse this statement");
    }
}
