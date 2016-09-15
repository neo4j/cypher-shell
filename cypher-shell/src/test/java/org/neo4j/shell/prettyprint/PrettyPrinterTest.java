package org.neo4j.shell.prettyprint;

import org.junit.Test;
import org.mockito.Matchers;
import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.summary.SummaryCounters;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.state.BoltResult;

import java.util.Collections;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
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

    @Test
    public void prettyPrintList() throws Exception {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record1 = mock(Record.class);
        Record record2 = mock(Record.class);
        Value value1 = mock(Value.class);
        Value value2 = mock(Value.class);


        when(value1.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.LIST());
        when(value2.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.LIST());

        when(value1.asList(Matchers.anyObject())).thenReturn(asList("val1_1", "val1_2"));
        when(value2.asList(Matchers.anyObject())).thenReturn(asList("val2_1"));

        when(record1.keys()).thenReturn(asList("col1", "col2"));
        when(record1.values()).thenReturn(asList(value1, value2));
        when(record2.values()).thenReturn(asList(value2));

        when(result.getRecords()).thenReturn(asList(record1, record2));

        // when
        String actual = new PrettyPrinter(Format.PLAIN).format(result);

        // then
        assertThat(actual, is("col1, col2\n[val1_1, val1_2], [val2_1]\n[val2_1]"));
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

        when(record.keys()).thenReturn(asList("col1", "col2"));
        when(record.values()).thenReturn(asList(value));

        when(result.getRecords()).thenReturn(asList(record));

        // when
        String actual = new PrettyPrinter(Format.PLAIN).format(result);

        // then
        assertThat(actual, is("col1, col2\n" +
                "(:label1:label2 {prop2: prop2_value, prop1: prop1_value})"));
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
        when(record.values()).thenReturn(asList(value));

        when(result.getRecords()).thenReturn(asList(record));

        // when
        String actual = new PrettyPrinter(Format.PLAIN).format(result);

        // then
        assertThat(actual, is("rel\n[:RELATIONSHIP_TYPE {prop2: prop2_value, prop1: prop1_value}]"));
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
        nodeProp.put("prop2", "\"\"");


        when(relVal.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.RELATIONSHIP());
        when(nodeVal.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.NODE());

        when(relVal.asRelationship()).thenReturn(relationship);
        when(relationship.type()).thenReturn("RELATIONSHIP,TYPE");
        when(relationship.asMap(anyObject())).thenReturn(unmodifiableMap(relProp));


        when(nodeVal.asNode()).thenReturn(node);
        when(node.labels()).thenReturn(asList("label 1", "label2"));
        when(node.asMap(anyObject())).thenReturn(unmodifiableMap(nodeProp));


        when(record.keys()).thenReturn(asList("rel", "node"));
        when(record.values()).thenReturn(asList(relVal, nodeVal));

        when(result.getRecords()).thenReturn(asList(record));

        // when
        String actual = new PrettyPrinter(Format.PLAIN).format(result);

        // then
        assertThat(actual, is("rel, node\n[:`RELATIONSHIP,TYPE` {prop2: prop2_value, prop1: \"prop1, value\"}], " +
                "(:`label 1`:label2 {prop2: \"\", prop1: \"prop1:value\"})"));
    }

    @Test
    public void prettyPrintPaths() throws Exception {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record = mock(Record.class);
        Value value = mock(Value.class);

        Node start = mock(Node.class);
        HashMap<String, Object> startProperties = new HashMap<>();
        startProperties.put("prop1", "prop1_value");
        when(start.labels()).thenReturn(asList("start"));

        Node middle = mock(Node.class);
        when(middle.labels()).thenReturn(asList("middle"));

        Node end = mock(Node.class);
        HashMap<String, Object> endProperties = new HashMap<>();
        endProperties.put("prop2", "prop2_value");
        when(end.labels()).thenReturn(asList("end"));

        Path path = mock(Path.class);
        when(path.start()).thenReturn(start);

        Relationship relationship = mock(Relationship.class);
        when(relationship.type()).thenReturn("RELATIONSHIP_TYPE");


        Path.Segment segment1 = mock(Path.Segment.class);
        when(segment1.start()).thenReturn(start);
        when(segment1.end()).thenReturn(middle);
        when(segment1.relationship()).thenReturn(relationship);

        Path.Segment segment2 = mock(Path.Segment.class);
        when(segment2.start()).thenReturn(middle);
        when(segment2.end()).thenReturn(end);
        when(segment2.relationship()).thenReturn(relationship);

        when(value.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.PATH());
        when(value.asPath()).thenReturn(path);
        when(path.iterator()).thenReturn(asList(segment1, segment2).iterator());
        when(start.asMap(anyObject())).thenReturn(unmodifiableMap(startProperties));
        when(end.asMap(anyObject())).thenReturn(unmodifiableMap(endProperties));

        when(record.keys()).thenReturn(asList("path"));
        when(record.values()).thenReturn(asList(value));

        when(result.getRecords()).thenReturn(asList(record));

        // when
        String actual = new PrettyPrinter(Format.PLAIN).format(result);

        // then
        assertThat(actual, is("path\n" +
                "(:start {prop1: prop1_value})-[:RELATIONSHIP_TYPE]->" +
                "(:middle)-[:RELATIONSHIP_TYPE]->(:end {prop2: prop2_value})"));
    }
}
