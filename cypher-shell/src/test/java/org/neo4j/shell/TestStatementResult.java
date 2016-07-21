package org.neo4j.shell;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.util.Function;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestStatementResult implements StatementResult {

    private final List<Record> records;
    private int currentRecord = -1;

    public TestStatementResult() {
        records = new ArrayList<>();
    }

    public TestStatementResult(@Nonnull final List<Record> records) {
        this.records = records;
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
    public List<Record> list() {
        return records;
    }

    @Override
    public <T> List<T> list(Function<Record, T> mapFunction) {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public ResultSummary consume() {
        return new TestResultSummary();
    }

    /**
     * Supports fake parsing of very limited cypher statements, only for basic test purposes
     */
    public static TestStatementResult parseStatement(@Nonnull final String statement) {

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
                TestStatementResult statementResult = new TestStatementResult();
                statementResult.records.add(TestRecord.of(key, value));
                return statementResult;
            }
        }
        throw new IllegalArgumentException("No idea how to parse this statement");
    }
}
