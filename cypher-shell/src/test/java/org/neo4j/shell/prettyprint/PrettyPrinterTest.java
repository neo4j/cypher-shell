package org.neo4j.shell.prettyprint;

import org.junit.Test;
import org.mockito.Matchers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.neo4j.driver.internal.types.InternalTypeSystem;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
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

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo4j.driver.internal.util.Iterables.map;

public class PrettyPrinterTest {

    private final PrettyPrinter plainPrinter = new PrettyPrinter(Format.PLAIN);
    private final PrettyPrinter verbosePrinter = new PrettyPrinter(Format.VERBOSE);

    @Test
    public void returnStatisticsForEmptyRecords() {
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
        String actual = verbosePrinter.format(result);

        // then
        assertThat(actual, containsString("Added 10 nodes, Added 1 labels"));
    }

    @Test
    public void prettyPrintProfileInformation() {
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
        String actual = plainPrinter.format(result);

        // then
        String expected =
                format("Plan: \"PROFILE\"%n" +
                "Statement: \"READ_ONLY\"%n" +
                "Version: \"3.1\"%n" +
                "Planner: \"COST\"%n" +
                "Runtime: \"INTERPRETED\"%n" +
                "Time: 12%n" +
                "Rows: 20%n" +
                "DbHits: 1000");
        Stream.of(expected.split(lineSeparator())).forEach(e -> assertThat(actual, containsString(e)));
    }

    @Test
    public void prettyPrintExplainInformation() {
        // given
        ResultSummary resultSummary = mock(ResultSummary.class);
        ProfiledPlan plan = mock(ProfiledPlan.class);
        when(plan.dbHits()).thenReturn(1000L);
        when(plan.records()).thenReturn(20L);

        when(resultSummary.hasPlan()).thenReturn(true);
        when(resultSummary.hasProfile()).thenReturn(false);
        when(resultSummary.plan()).thenReturn(plan);
        when(resultSummary.resultAvailableAfter(anyObject())).thenReturn(5L);
        when(resultSummary.resultConsumedAfter(anyObject())).thenReturn(7L);
        when(resultSummary.statementType()).thenReturn(StatementType.READ_ONLY);
        Map<String, Value> argumentMap = Values.parameters("Version", "3.1", "Planner", "COST", "Runtime", "INTERPRETED").asMap(v -> v);
        when(plan.arguments()).thenReturn(argumentMap);

        BoltResult result = mock(BoltResult.class);
        when(result.getRecords()).thenReturn(Collections.emptyList());
        when(result.getSummary()).thenReturn(resultSummary);

        // when
        String actual = plainPrinter.format(result);

        // then
        String expected =
                format("Plan: \"EXPLAIN\"%n" +
                "Statement: \"READ_ONLY\"%n" +
                "Version: \"3.1\"%n" +
                "Planner: \"COST\"%n" +
                "Runtime: \"INTERPRETED\"%n" +
                "Time: 12");
        Stream.of(expected.split(lineSeparator())).forEach(e -> assertThat(actual, containsString(e)));
    }

    @Test
    public void prettyPrintList() {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record1 = mock(Record.class);
        Record record2 = mock(Record.class);
        Value value1 = mock(Value.class);
        Value value2 = mock(Value.class);


        when(value1.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.LIST());
        when(value2.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.LIST());

        when(value1.asList(Matchers.any(Function.class))).thenReturn(asList("val1_1", "val1_2"));
        when(value2.asList(Matchers.any(Function.class))).thenReturn(singletonList("val2_1"));

        when(record1.keys()).thenReturn(asList("col1", "col2"));
        when(record1.values()).thenReturn(asList(value1, value2));
        when(record2.values()).thenReturn(singletonList(value2));

        when(result.getRecords()).thenReturn(asList(record1, record2));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = plainPrinter.format(result);

        // then
        assertThat(actual, is(format("col1, col2%n[val1_1, val1_2], [val2_1]%n[val2_1]")));
    }

    @Test
    public void prettyPrintMaps() {
        checkMapForPrettyPrint(map(), format("map%n{}"));
        checkMapForPrettyPrint(map("abc", "def"), format("map%n{abc: def}"));
    }

    private void checkMapForPrettyPrint(Map<String, String> map, String expectedResult) {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record = mock(Record.class);
        Value value = mock(Value.class);

        when(value.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.MAP());

        when(value.asMap((Function<Value, String>) anyObject())).thenReturn(map);

        when(record.keys()).thenReturn(singletonList("map"));
        when(record.values()).thenReturn(singletonList(value));

        when(result.getRecords()).thenReturn(singletonList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = plainPrinter.format(result);

        // then
        assertThat(actual, is(expectedResult));
    }

    @Test
    public void prettyPrintNode() {
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
        when(record.values()).thenReturn(singletonList(value));

        when(result.getRecords()).thenReturn(singletonList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = plainPrinter.format(result);

        // then
        assertThat(actual, is(format("col1, col2%n") +
                "(:label1:label2 {prop2: prop2_value, prop1: prop1_value})"));
    }

    @Test
    public void prettyPrintRelationships() {
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

        when(record.keys()).thenReturn(singletonList("rel"));
        when(record.values()).thenReturn(singletonList(value));

        when(result.getRecords()).thenReturn(singletonList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = plainPrinter.format(result);

        // then
        assertThat(actual, is(format("rel%n[:RELATIONSHIP_TYPE {prop2: prop2_value, prop1: prop1_value}]")));
    }

    @Test
    public void printRelationshipsAndNodesWithEscapingForSpecialCharacters() {
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
        when(record.values()).thenReturn(asList(relVal, nodeVal));

        when(result.getRecords()).thenReturn(singletonList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = plainPrinter.format(result);

        // then
        assertThat(actual, is(format("rel, node%n[:`RELATIONSHIP,TYPE` {prop2: prop2_value, prop1: \"prop1, value\"}], " +
                "(:`label ``1`:label2 {prop1: \"prop1:value\", `1prop2`: \"\", ä: not-escaped})")));
    }

    @Test
    public void prettyPrintPaths() {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record = mock(Record.class);
        Value value = mock(Value.class);

        Node start = mock(Node.class);
        HashMap<String, Object> startProperties = new HashMap<>();
        startProperties.put("prop1", "prop1_value");
        when(start.labels()).thenReturn(singletonList("start"));
        when(start.id()).thenReturn(1L);

        Node middle = mock(Node.class);
        when(middle.labels()).thenReturn(singletonList("middle"));
        when(middle.id()).thenReturn(2L);

        Node end = mock(Node.class);
        HashMap<String, Object> endProperties = new HashMap<>();
        endProperties.put("prop2", "prop2_value");
        when(end.labels()).thenReturn(singletonList("end"));
        when(end.id()).thenReturn(3L);

        Path path = mock(Path.class);
        when(path.start()).thenReturn(start);

        Relationship relationship = mock(Relationship.class);
        when(relationship.type()).thenReturn("RELATIONSHIP_TYPE");
        when(relationship.startNodeId()).thenReturn(1L).thenReturn(3L);


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

        when(record.keys()).thenReturn(singletonList("path"));
        when(record.values()).thenReturn(singletonList(value));

        when(result.getRecords()).thenReturn(singletonList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = plainPrinter.format(result);

        // then
        assertThat(actual, is(format("path%n") +
                "(:start {prop1: prop1_value})-[:RELATIONSHIP_TYPE]->" +
                "(:middle)<-[:RELATIONSHIP_TYPE]-(:end {prop2: prop2_value})"));
    }

    @Test
    public void prettyPrintSingleNodePath() {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record = mock(Record.class);
        Value value = mock(Value.class);

        Node start = mock(Node.class);
        when(start.labels()).thenReturn( singletonList( "start" ) );
        when(start.id()).thenReturn(1L);

        Node end = mock(Node.class);
        when(end.labels()).thenReturn( singletonList( "end" ) );
        when(end.id()).thenReturn(2L);

        Path path = mock(Path.class);
        when(path.start()).thenReturn(start);

        Relationship relationship = mock(Relationship.class);
        when(relationship.type()).thenReturn("RELATIONSHIP_TYPE");
        when(relationship.startNodeId()).thenReturn(1L);


        Path.Segment segment1 = mock(Path.Segment.class);
        when(segment1.start()).thenReturn(start);
        when(segment1.end()).thenReturn(end);
        when(segment1.relationship()).thenReturn(relationship);

        when(value.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.PATH());
        when(value.asPath()).thenReturn(path);
        when(path.iterator()).thenReturn(singletonList( segment1 ).iterator());

        when(record.keys()).thenReturn(singletonList( "path" ) );
        when(record.values()).thenReturn(singletonList(value));

        when(result.getRecords()).thenReturn(singletonList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = plainPrinter.format(result);

        // then
        assertThat(actual, is(format("path%n(:start)-[:RELATIONSHIP_TYPE]->(:end)")));
    }

    @Test
    public void prettyPrintThreeSegmentPath() {
        // given
        BoltResult result = mock(BoltResult.class);

        Record record = mock(Record.class);
        Value value = mock(Value.class);

        Node start = mock(Node.class);
        when(start.labels()).thenReturn(singletonList("start"));
        when(start.id()).thenReturn(1L);

        Node second = mock(Node.class);
        when(second.labels()).thenReturn(singletonList("second"));
        when(second.id()).thenReturn(2L);

        Node third = mock(Node.class);
        when(third.labels()).thenReturn(singletonList("third"));
        when(third.id()).thenReturn(3L);

        Node end = mock(Node.class);
        when(end.labels()).thenReturn(singletonList("end"));
        when(end.id()).thenReturn(4L);

        Path path = mock(Path.class);
        when(path.start()).thenReturn(start);

        Relationship relationship = mock(Relationship.class);
        when(relationship.type()).thenReturn("RELATIONSHIP_TYPE");
        when(relationship.startNodeId()).thenReturn(1L).thenReturn(3L).thenReturn(3L);


        Path.Segment segment1 = mock(Path.Segment.class);
        when(segment1.start()).thenReturn(start);
        when(segment1.end()).thenReturn(second);
        when(segment1.relationship()).thenReturn(relationship);

        Path.Segment segment2 = mock(Path.Segment.class);
        when(segment2.start()).thenReturn(second);
        when(segment2.end()).thenReturn(third);
        when(segment2.relationship()).thenReturn(relationship);

        Path.Segment segment3 = mock(Path.Segment.class);
        when(segment3.start()).thenReturn(third);
        when(segment3.end()).thenReturn(end);
        when(segment3.relationship()).thenReturn(relationship);

        when(value.type()).thenReturn(InternalTypeSystem.TYPE_SYSTEM.PATH());
        when(value.asPath()).thenReturn(path);
        when(path.iterator()).thenReturn(asList(segment1, segment2, segment3).iterator());

        when(record.keys()).thenReturn(singletonList("path"));
        when(record.values()).thenReturn(singletonList(value));

        when(result.getRecords()).thenReturn(singletonList(record));
        when(result.getSummary()).thenReturn(mock(ResultSummary.class));

        // when
        String actual = plainPrinter.format(result);

        // then
        assertThat(actual, is(format("path%n") +
                "(:start)-[:RELATIONSHIP_TYPE]->" +
                "(:second)<-[:RELATIONSHIP_TYPE]-(:third)-[:RELATIONSHIP_TYPE]->(:end)"));
    }
}
