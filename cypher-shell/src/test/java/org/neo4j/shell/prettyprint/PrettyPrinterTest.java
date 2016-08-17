package org.neo4j.shell.prettyprint;

import org.junit.Test;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.SummaryCounters;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.state.BoltResult;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrettyPrinterTest {
    @Test
    public void returnStatisticsForEmptyRecords() throws Exception {
        // given
        ResultSummary resultSummary = mock(ResultSummary.class);
        SummaryCounters summaryCounters = mock(SummaryCounters.class);
        BoltResult result = mock(BoltResult.class);

        when(result.getRecords()).thenReturn(Collections.emptyList());
        when(result.getSummary()).thenReturn(resultSummary);
        when(resultSummary.counters()).thenReturn(summaryCounters);
        when(summaryCounters.labelsAdded()).thenReturn(1);
        when(summaryCounters.nodesCreated()).thenReturn(10);

        // when
        String actual = new PrettyPrinter(Format.VERBOSE).format(result);

        // then
        assertThat(actual, is("Added 10 nodes, Added 1 labels"));
    }
}
