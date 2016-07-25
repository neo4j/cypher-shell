package org.neo4j.shell;

import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.summary.*;

import java.util.List;

/**
 * A fake result summary
 */
public class TestResultSummary implements ResultSummary {
    @Override
    public Statement statement() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public SummaryCounters counters() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public StatementType statementType() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public boolean hasPlan() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public boolean hasProfile() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public Plan plan() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public ProfiledPlan profile() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }

    @Override
    public List<Notification> notifications() {
        throw new Util.NotImplementedYetException("Not implemented yet");
    }
}
