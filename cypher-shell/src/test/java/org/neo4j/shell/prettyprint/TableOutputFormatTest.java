package org.neo4j.shell.prettyprint;

import org.junit.Test;
import org.mockito.Matchers;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalPath;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.internal.net.BoltServerAddress;
import org.neo4j.driver.internal.summary.InternalServerInfo;
import org.neo4j.driver.internal.summary.InternalSummaryCounters;
import org.neo4j.driver.internal.summary.SummaryBuilder;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.SummaryCounters;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.state.BoltResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TableOutputFormatTest {

    private final PrettyPrinter verbosePrinter = new PrettyPrinter(Format.VERBOSE);

    @Test
    public void prettyPrintNode() throws Exception {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record = mock(Record.class);
        Value value = mock(Value.class);

        Node node = mock(Node.class);
        HashMap<String, Object> propertiesAsMap = new HashMap<>();
        propertiesAsMap.put("prop1", "prop1_value");
        propertiesAsMap.put("prop2", "prop2_value");

        when(value.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.NODE());

        when(value.asNode()).thenReturn(node);
        when(node.labels()).thenReturn(asList("label1", "label2"));
        when(node.asMap(anyObject())).thenReturn(unmodifiableMap(propertiesAsMap));

        when(record.keys()).thenReturn(asList("col1", "col2"));
        when(record.get(eq("col1"))).thenReturn(value);
        when(record.get(eq("col2"))).thenReturn(value);

        when(record.values()).thenReturn(asList(value));

        when(result.getRecords()).thenReturn(asList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = verbosePrinter.format(result);

        // then
        assertThat(actual, containsString("| (:label1:label2 {prop2: prop2_value, prop1: prop1_value}) |"));
    }

    @Test
    public void prettyPrintRelationships() throws Exception {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record = mock(Record.class);
        Value value = mock(Value.class);

        Relationship relationship = mock(Relationship.class);
        HashMap<String, Object> propertiesAsMap = new HashMap<>();
        propertiesAsMap.put("prop1", "prop1_value");
        propertiesAsMap.put("prop2", "prop2_value");

        when(value.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.RELATIONSHIP());

        when(value.asRelationship()).thenReturn(relationship);
        when(relationship.type()).thenReturn("RELATIONSHIP_TYPE");
        when(relationship.asMap(anyObject())).thenReturn(unmodifiableMap(propertiesAsMap));

        when(record.keys()).thenReturn(asList("rel"));
        when(record.get(eq("rel"))).thenReturn(value);
        when(record.values()).thenReturn(asList(value));

        when(result.getRecords()).thenReturn(asList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = verbosePrinter.format(result);

        // then
        assertThat(actual, containsString("| [:RELATIONSHIP_TYPE {prop2: prop2_value, prop1: prop1_value}] |"));
    }

    @Test
    public void printRelationshipsAndNodesWithEscapingForSpecialCharacters() throws Exception {
        BoltResult result = mock(BoltResult.class);

        Record record = mock(Record.class);
        Value relVal = mock(Value.class);
        Value nodeVal = mock(Value.class);

        Relationship relationship = mock(Relationship.class);
        HashMap<String, Object> relProp = new HashMap<>();
        relProp.put("prop1", "\"prop1, value\"");
        relProp.put("prop2", "prop2_value");

        Node node = mock(Node.class);
        HashMap<String, Object> nodeProp = new HashMap<>();
        nodeProp.put("prop1", "\"prop1:value\"");
        nodeProp.put("1prop2", "\"\"");
        nodeProp.put("ä", "not-escaped");


        when(relVal.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.RELATIONSHIP());
        when(nodeVal.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.NODE());

        when(relVal.asRelationship()).thenReturn(relationship);
        when(relationship.type()).thenReturn("RELATIONSHIP,TYPE");
        when(relationship.asMap(anyObject())).thenReturn(unmodifiableMap(relProp));


        when(nodeVal.asNode()).thenReturn(node);
        when(node.labels()).thenReturn(asList("label `1", "label2"));
        when(node.asMap(anyObject())).thenReturn(unmodifiableMap(nodeProp));


        when(record.keys()).thenReturn(asList("rel", "node"));
        when(record.get(eq("rel"))).thenReturn(relVal);
        when(record.get(eq("node"))).thenReturn(nodeVal);

        when(record.values()).thenReturn(asList(relVal, nodeVal));

        when(result.getRecords()).thenReturn(asList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = verbosePrinter.format(result);

        // then
        assertThat(actual, containsString("| [:`RELATIONSHIP,TYPE` {prop2: prop2_value, prop1: \"prop1, value\"}] |"));
        assertThat(actual, containsString("| (:`label ``1`:label2 {prop1: \"prop1:value\", `1prop2`: \"\", ä: not-escaped})"));
    }

    @Test
    public void basicTable() throws Exception
    {
        // GIVEN
        StatementResult result = mockResult( asList( "c1", "c2" ), "a", 42 );
        // WHEN
        String table = formatResult( result );
        // THEN
        assertThat( table, containsString( "| c1  | c2 |" ) );
        assertThat( table, containsString( "| \"a\" | 42 |" ) );
        assertThat( table, containsString( "1 row" ) );
    }

    @Test
    public void twoRows() throws Exception
    {
        // GIVEN
        StatementResult result = mockResult( asList( "c1", "c2" ), "a", 42, "b", 43 );
        // WHEN
        String table = formatResult( result );
        // THEN
        assertThat( table, containsString( "| \"a\" | 42 |" ) );
        assertThat( table, containsString( "| \"b\" | 43 |" ) );
        assertThat( table, containsString( "2 rows" ) );
    }

    @Test
    public void formatCollections() throws Exception
    {
        // GIVEN
        StatementResult result = mockResult( asList( "a", "b", "c" ), singletonMap( "a", 42 ), asList( 12, 13 ),
                singletonMap( "a", asList( 14, 15 ) ) );
        // WHEN
        String table = formatResult( result );
        // THEN
        assertThat( table, containsString( "| {a: 42} | [12, 13] | {a: [14, 15]} |" ) );
    }

    @Test
    public void formatEntities() throws Exception
    {
        // GIVEN
        Map<String,Value> properties = singletonMap( "name", Values.value( "Mark" ) );
        Map<String,Value> relProperties = singletonMap( "since", Values.value( 2016 ) );
        InternalNode node = new InternalNode( 12, asList( "Person" ), properties );
        InternalRelationship relationship = new InternalRelationship( 24, 12, 12, "TEST", relProperties );
        StatementResult result =
                mockResult( asList( "a", "b", "c" ), node, relationship, new InternalPath( node, relationship, node ) );
        // WHEN
        String table = formatResult( result );
        // THEN
        assertThat( table, containsString( "| (:Person {name: \"Mark\"}) | [:TEST {since: 2016}] |" ) );
        assertThat( table, containsString(
                "| (:Person {name: \"Mark\"})-[:TEST {since: 2016}]->(:Person {name: \"Mark\"}) |" ) );
    }

    private String formatResult(StatementResult result) {
        return new TableOutputFormatter().format(new BoltResult(result.list(), result.summary()));
    }

    private StatementResult mockResult(List<String> cols, Object... data) {
        StatementResult result = mock(StatementResult.class);
        Statement statement = mock(Statement.class);
        ResultSummary summary = new SummaryBuilder(statement, null).build();
        when(result.keys()).thenReturn(cols);
        List<Record> records = new ArrayList<>();
        List<Object> input = asList(data);
        int width = cols.size();
        for (int row = 0; row < input.size() / width; row++) {
            records.add(record(cols, input.subList(row * width, (row + 1) * width)));
        }
        when(result.list()).thenReturn(records);
        when(result.consume()).thenReturn(summary);
        return result;
    }

    private Record record(List<String> cols, List<Object> data) {
        assert cols.size() == data.size();
        Value[] values = new Value[data.size()];
        for (int i = 0; i < data.size(); i++) {
            values[i] = Values.value(data.get(i));
        }
        return new InternalRecord(cols, values);
    }
}
