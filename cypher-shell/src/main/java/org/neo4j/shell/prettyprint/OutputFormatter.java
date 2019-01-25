package org.neo4j.shell.prettyprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.neo4j.driver.internal.types.TypeRepresentation;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.summary.Plan;
import org.neo4j.driver.v1.summary.ProfiledPlan;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Point;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.shell.state.BoltResult;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.neo4j.shell.prettyprint.CypherVariablesFormatter.escape;

public interface OutputFormatter {

    String COMMA_SEPARATOR = ", ";
    String COLON_SEPARATOR = ": ";
    String COLON = ":";
    String SPACE = " ";
    String NEWLINE =  System.lineSeparator();

    @Nonnull String format(@Nonnull BoltResult result);

    @Nonnull default String formatValue(@Nonnull final Value value) {
        TypeRepresentation type = (TypeRepresentation) value.type();
        switch (type.constructor()) {
            case LIST:
                return listAsString(value.asList(this::formatValue));
            case MAP:
                return mapAsString(value.asMap(this::formatValue));
            case NODE:
                return nodeAsString(value.asNode());
            case RELATIONSHIP:
                return relationshipAsString(value.asRelationship());
            case PATH:
                return pathAsString(value.asPath());
            case POINT:
                return pointAsString(value.asPoint());
            case ANY:
            case BOOLEAN:
            case BYTES:
            case STRING:
            case NUMBER:
            case INTEGER:
            case FLOAT:
            case DATE:
            case TIME:
            case DATE_TIME:
            case LOCAL_TIME:
            case LOCAL_DATE_TIME:
            case DURATION:
            case NULL:
            default:
                return value.toString();
        }
    }

    @Nonnull
    default String pointAsString(Point point) {
        StringBuilder stringBuilder = new StringBuilder("point({");
        stringBuilder.append( "srid:" ).append( point.srid() ).append( "," );
        stringBuilder.append(" x:").append(point.x()).append(",");
        stringBuilder.append(" y:").append(point.y());
        double z = point.z();
        if (!Double.isNaN(z)) {
            stringBuilder.append(", z:").append(z);
        }
        stringBuilder.append("})");
        return stringBuilder.toString();
    }

    @Nonnull
    default String pathAsString(@Nonnull Path path) {
        List<String> list = new ArrayList<>(path.length());
        Node lastTraversed = path.start();
        if (lastTraversed != null) {
            list.add(nodeAsString(lastTraversed));

            for (Path.Segment segment : path) {
                Relationship relationship = segment.relationship();
                if (relationship.startNodeId() == lastTraversed.id()) {
                    list.add("-" + relationshipAsString(relationship) + "->");
                } else {
                    list.add("<-" + relationshipAsString(relationship) + "-");
                }
                list.add(nodeAsString(segment.end()));
                lastTraversed = segment.end();
            }
        }

        return String.join( "", list );
    }

    @Nonnull default String relationshipAsString(@Nonnull Relationship relationship) {
        List<String> relationshipAsString = new ArrayList<>();
        relationshipAsString.add(COLON + escape(relationship.type()));
        relationshipAsString.add(mapAsStringWithEmpty(relationship.asMap(this::formatValue)));

        return "[" + joinWithSpace(relationshipAsString) + "]";
    }

    @Nonnull default String nodeAsString(@Nonnull final Node node) {
        List<String> nodeAsString = new ArrayList<>();
        nodeAsString.add(collectNodeLabels(node));
        nodeAsString.add(mapAsStringWithEmpty(node.asMap(this::formatValue)));

        return "(" + joinWithSpace(nodeAsString) + ")";
    }

    @Nonnull static String collectNodeLabels(@Nonnull Node node) {
        StringBuilder sb = new StringBuilder();
        node.labels().forEach(label -> sb.append(COLON).append(escape(label)));
        return sb.toString();
    }

    @Nonnull static String listAsString(@Nonnull List<String> list) {
        return list.stream().collect(Collectors.joining(COMMA_SEPARATOR,"[","]"));
    }

    @Nonnull static String mapAsStringWithEmpty(@Nonnull Map<String, Object> map) {
        return map.isEmpty() ? "" : mapAsString(map);
    }

    @Nonnull static String mapAsString(@Nonnull Map<String, Object> map) {
        return map.entrySet().stream()
                .map(e -> escape(e.getKey()) + COLON_SEPARATOR + e.getValue())
                .collect(Collectors.joining(COMMA_SEPARATOR,"{","}"));
    }

    @Nonnull static String joinWithSpace(@Nonnull List<String> strings) {
        return strings.stream().filter(OutputFormatter::isNotBlank).collect(Collectors.joining(SPACE));
    }
    @Nonnull static  String joinNonBlanks(@Nonnull String delim, @Nonnull List<String> strings) {
        return strings.stream().filter(OutputFormatter::isNotBlank).collect(Collectors.joining(delim));
    }

    static boolean isNotBlank(String string) {
        return string != null && !string.trim().isEmpty();
    }

    @Nonnull static String repeat(char c, int times) {
        char[] chars = new char[times];
        Arrays.fill(chars, c);
        return String.valueOf(chars);
    }

    @Nonnull static String repeat(@Nonnull String c, int times) {
        StringBuilder sb = new StringBuilder(times*c.length());
        for (int i=0;i<times;i++) sb.append(c);
        return sb.toString();
    }

    @Nonnull static String rightPad(@Nonnull String str, int width) {
        return rightPad(str,width,' ');
    }
    @Nonnull static String rightPad(@Nonnull String str, int width, char c) {
        int actualSize = str.length();
        if (actualSize > width) {
            return str.substring(0, width);
        } else if (actualSize < width) {
            return str + repeat( c, width - actualSize);
        } else {
            return str;
        }
    }

    @Nonnull default String formatPlan(@Nonnull ResultSummary summary) {
        return "";
    }
    @Nonnull default String formatInfo(@Nonnull ResultSummary summary) {
        return "";
    }
    @Nonnull default String formatFooter(@Nonnull BoltResult result) {
        return "";
    }


    List<String> INFO = asList("Version", "Planner", "Runtime");

    @Nonnull
    static Map<String, Value> info(@Nonnull ResultSummary summary) {
        Map<String, Value> result = new LinkedHashMap<>();
        if (!summary.hasPlan()) return result;

        Plan plan = summary.plan();
        result.put("Plan", Values.value(summary.hasProfile() ? "PROFILE" : "EXPLAIN"));
        result.put("Statement", Values.value(summary.statementType().name()));
        Map<String, Value> arguments = plan.arguments();
        Value defaultValue = Values.value("");

        for (String key : INFO) {
            Value value = arguments.getOrDefault(key, arguments.getOrDefault(key.toLowerCase(), defaultValue));
            result.put(key, value);
        }
        result.put("Time", Values.value(summary.resultAvailableAfter(MILLISECONDS)+summary.resultConsumedAfter(MILLISECONDS)));
        if ( summary.hasProfile() ) result.put( "DbHits", Values.value( collectHits( summary.profile() ) ) );
        if (summary.hasProfile()) result.put("Rows", Values.value( summary.profile().records() ));
        return result;
    }

    static long collectHits(@Nonnull ProfiledPlan operator ) {
        long hits = operator.dbHits();
        hits = operator.children().stream().map( OutputFormatter::collectHits ).reduce(hits, (acc, subHits) -> acc + subHits );
        return hits;
    }

}
