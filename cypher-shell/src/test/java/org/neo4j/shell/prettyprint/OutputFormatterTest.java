package org.neo4j.shell.prettyprint;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.internal.BoltServerAddress;
import org.neo4j.driver.internal.summary.InternalDatabaseInfo;
import org.neo4j.driver.internal.summary.InternalResultSummary;
import org.neo4j.driver.internal.summary.InternalServerInfo;
import org.neo4j.driver.internal.util.ServerVersion;
import org.neo4j.driver.internal.value.ListValue;
import org.neo4j.driver.internal.value.MapValue;
import org.neo4j.driver.Statement;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.summary.ProfiledPlan;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.StatementType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.neo4j.driver.internal.summary.InternalProfiledPlan.PROFILED_PLAN_FROM_VALUE;

public class OutputFormatterTest
{
    @Test
    public void shouldReportTotalDBHits() {
        Value labelScan = buildOperator( "NodeByLabelScan", 1002L, 1001L, null );
        Value filter = buildOperator( "Filter", 1402, 280, labelScan );
        Value planMap = buildOperator( "ProduceResults", 0, 280, filter );

        ProfiledPlan plan = PROFILED_PLAN_FROM_VALUE.apply( planMap );
        ResultSummary summary = new InternalResultSummary(
                new Statement( "PROFILE MATCH (n:LABEL) WHERE 20 < n.age < 35 return n" ),
                new InternalServerInfo( new BoltServerAddress( "localhost:7687" ), ServerVersion.vInDev ),
                new InternalDatabaseInfo("neo4j"),
                StatementType.READ_ONLY,
                null,
                plan,
                plan,
                Collections.emptyList(),
                39,
                55 );

        // When
        Map<String,Value> info = OutputFormatter.info( summary );

        //Then
        assertThat( info.get( "DbHits" ).asLong(), equalTo( 2404L ) );
    }

    private Value buildOperator( String operator, long dbHits, long rows, Value child ) {
        Map<String, Value> operatorMap = new HashMap<>();
        operatorMap.put( "operatorType", Values.value( operator ) );
        operatorMap.put( "dbHits", Values.value( dbHits ) );
        operatorMap.put( "rows", Values.value( rows ) );
        if ( child != null ) {
            operatorMap.put( "children", new ListValue( child ) );
        }
        return new MapValue( operatorMap );
    }
}
