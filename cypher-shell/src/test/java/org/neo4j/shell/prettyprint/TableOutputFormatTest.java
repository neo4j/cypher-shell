package org.neo4j.shell.prettyprint;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalPath;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.summary.ProfiledPlan;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.StatementType;
import org.neo4j.driver.v1.summary.SummaryCounters;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.driver.v1.util.Function;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.state.BoltResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Stream;

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
    public void prettyPrintPlanInformation() throws Exception {
        // given
        ResultSummary resultSummary = mock(ResultSummary.class);
        ProfiledPlan plan = mock(ProfiledPlan.class);
        when(plan.dbHits()).thenReturn(1000L);
        when(plan.records()).thenReturn(20L);

        when(resultSummary.hasPlan()).thenReturn(true);
        when(resultSummary.hasProfile()).thenReturn(true);
        when(resultSummary.plan()).thenReturn(plan);
        when(resultSummary.profile()).thenReturn(plan);
        when(resultSummary.resultAvailableAfter(anyObject())).thenReturn(5L);
        when(resultSummary.resultConsumedAfter(anyObject())).thenReturn(7L);
        when(resultSummary.statementType()).thenReturn(StatementType.READ_ONLY);
        Map<String, Value> argumentMap = Values.parameters("Version", "3.1", "Planner", "COST", "Runtime", "INTERPRETED").asMap(v -> v);
        when(plan.arguments()).thenReturn(argumentMap);

        BoltResult result = mock(BoltResult.class);
        when(result.getRecords()).thenReturn(Collections.emptyList());
        when(result.getSummary()).thenReturn(resultSummary);

        // when
        String actual = verbosePrinter.format(result);

        // then
        argumentMap.forEach((k,v) -> {
            assertThat(actual, CoreMatchers.containsString("| "+k));
            assertThat(actual, CoreMatchers.containsString("| "+v.toString()));
        });
    }

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

        Map<String,Value> recordMap = new LinkedHashMap<>();
        recordMap.put("col1",value);
        recordMap.put("col2",value);
        when(record.keys()).thenReturn(asList("col1", "col2"));
        when(record.get(eq("col1"))).thenReturn(value);
        when(record.get(eq("col2"))).thenReturn(value);
        when(record.<Value>asMap(anyObject())).thenReturn(recordMap);

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
        when(record.asMap(anyObject())).thenReturn(Collections.singletonMap("rel",value));
        when(result.getRecords()).thenReturn(asList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = verbosePrinter.format(result);

        // then
        assertThat(actual, containsString("| [:RELATIONSHIP_TYPE {prop2: prop2_value, prop1: prop1_value}] |"));
    }

    @Test
    public void prettyPrintPath() throws Exception {
        // given
        BoltResult result = mock(BoltResult.class);
        Record record = mock(Record.class);
        Value value = mock(Value.class);
        Path path = mock(Path.class);


        Node n1 = mock(Node.class);
        when(n1.id()).thenReturn(1L);
        when(n1.labels()).thenReturn(asList("L1"));
        when(n1.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Relationship r1 = mock(Relationship.class);
        when(r1.startNodeId()).thenReturn(2L);
        when(r1.type()).thenReturn("R1");
        when(r1.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Node n2 = mock(Node.class);
        when(n2.id()).thenReturn(2L);
        when(n2.labels()).thenReturn(asList("L2"));
        when(n2.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Relationship r2 = mock(Relationship.class);
        when(r2.startNodeId()).thenReturn(2L);
        when(r2.type()).thenReturn("R2");
        when(r2.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Node n3 = mock(Node.class);
        when(n3.id()).thenReturn(3L);
        when(n3.labels()).thenReturn(asList("L3"));
        when(n3.asMap(anyObject())).thenReturn(Collections.emptyMap());

        Path.Segment s1 = mock(Path.Segment.class);
        when(s1.relationship()).thenReturn(r1);
        when(s1.start()).thenReturn(n1);
        when(s1.end()).thenReturn(n2);

        Path.Segment s2 = mock(Path.Segment.class);
        when(s2.relationship()).thenReturn(r2);
        when(s2.start()).thenReturn(n2);
        when(s2.end()).thenReturn(n3);
   
        when(path.start()).thenReturn(n1);
        when(path.iterator()).thenAnswer((i) -> Arrays.asList(s1,s2).iterator());

        when(value.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.PATH());
        when(value.asPath()).thenReturn(path);

        when(record.keys()).thenReturn(asList("path"));
        when(record.get(eq("path"))).thenReturn(value);
        when(record.values()).thenReturn(asList(value));
        when(record.asMap(anyObject())).thenReturn(Collections.singletonMap("path",value));
        when(result.getRecords()).thenReturn(asList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = verbosePrinter.format(result);

        // then
        assertThat(actual, containsString("| (:L1)<-[:R1]-(:L2)-[:R2]->(:L3) |"));
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


        Map<String,Value> recordMap = new LinkedHashMap<>();
        recordMap.put("rel",relVal);
        recordMap.put("node",nodeVal);
        when(record.keys()).thenReturn(asList("rel", "node"));
        when(record.get(eq("rel"))).thenReturn(relVal);
        when(record.get(eq("node"))).thenReturn(nodeVal);

        when(record.<Value>asMap(anyObject())).thenReturn(recordMap);

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
        ResultSummary summary = mock(ResultSummary.class);
        when(summary.statement()).thenReturn(statement);
        when(result.keys()).thenReturn(cols);
        List<Record> records = new ArrayList<>();
        List<Object> input = asList(data);
        int width = cols.size();
        for (int row = 0; row < input.size() / width; row++) {
            records.add(record(cols, input.subList(row * width, (row + 1) * width)));
        }
        when(result.list()).thenReturn(records);
        when(result.consume()).thenReturn(summary);
        when(result.summary()).thenReturn(summary);
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
