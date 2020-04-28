package org.neo4j.shell.prettyprint;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import org.neo4j.driver.Value;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.driver.summary.Plan;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.shell.prettyprint.OutputFormatter.NEWLINE;

public class TablePlanFormatterTest
{
    TablePlanFormatter tablePlanFormatter = new TablePlanFormatter();

    @Test
    public void renderShortDetails() {
        Plan plan = mock(Plan.class);
        Map<String, Value> args = Collections.singletonMap("Details", new StringValue("x.prop AS prop"));
        when(plan.arguments()).thenReturn(args);
        when(plan.operatorType()).thenReturn("Projection");

        assertThat(tablePlanFormatter.formatPlan( plan ), is(String.join(NEWLINE,
                                         "+-------------+----------------+",
                                         "| Operator    | Details        |",
                                         "+-------------+----------------+",
                                         "| +Projection | x.prop AS prop |",
                                         "+-------------+----------------+", "")));
    }

    @Test
    public void renderExactMaxLengthDetails() {
        Plan plan = mock(Plan.class);
        String details = stringOfLength(TablePlanFormatter.MAX_DETAILS_COLUMN_WIDTH);
        Map<String, Value> args = Collections.singletonMap("Details", new StringValue(details));
        when(plan.arguments()).thenReturn(args);
        when(plan.operatorType()).thenReturn("Projection");

        assertThat(tablePlanFormatter.formatPlan( plan ), containsString("| +Projection | " + details + " |"));
    }

    @Test
    public void truncateTooLongDetails() {
        Plan plan = mock(Plan.class);
        String details = stringOfLength(TablePlanFormatter.MAX_DETAILS_COLUMN_WIDTH + 1);
        Map<String, Value> args = Collections.singletonMap("Details", new StringValue(details));
        when(plan.arguments()).thenReturn(args);
        when(plan.operatorType()).thenReturn("Projection");

        assertThat(tablePlanFormatter.formatPlan( plan ), containsString("| +Projection | " + details.substring( 0, TablePlanFormatter.MAX_DETAILS_COLUMN_WIDTH - 3 ) + "... |"));
    }

    private String stringOfLength(int length) {
        StringBuilder strBuilder = new StringBuilder();

        for(int i=0; i<length; i++) {
            strBuilder.append('a');
        }

        return strBuilder.toString();
    }
}