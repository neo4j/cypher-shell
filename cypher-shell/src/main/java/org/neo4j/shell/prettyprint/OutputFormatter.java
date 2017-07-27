package org.neo4j.shell.prettyprint;

import org.neo4j.driver.internal.types.TypeRepresentation;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.summary.Plan;
import org.neo4j.driver.v1.summary.ResultSummary;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.shell.state.BoltResult;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.neo4j.shell.prettyprint.CypherVariablesFormatter.escape;

/**
 * @author mh
 * @since 09.04.17
 */
public interface OutputFormatter {

    String COMMA_SEPARATOR = ", ";
    String COLON_SEPARATOR = ": ";
    String COLON = ":";
    String SPACE = " ";
    String NEWLINE =  System.getProperty("line.separator");

    @Nonnull String format(@Nonnull BoltResult result);

    @Nonnull default String formatValue(@Nonnull final Value value) {
        TypeRepresentation type = (TypeRepresentation) value.type();
        switch (type.constructor()) {
            case LIST_TyCon:
                return listAsString(value.asList(this::formatValue));
            case MAP_TyCon:
                return mapAsString(value.asMap(this::formatValue));
            case NODE_TyCon:
                return nodeAsString(value.asNode());
            case RELATIONSHIP_TyCon:
                return relationshipAsString(value.asRelationship());
            case PATH_TyCon:
                return pathAsString(value.asPath());
            case ANY_TyCon:
            case BOOLEAN_TyCon:
            case STRING_TyCon:
            case NUMBER_TyCon:
            case INTEGER_TyCon:
            case FLOAT_TyCon:
            case NULL_TyCon:
            default:
                return value.toString();
        }
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

        return list.stream().collect(Collectors.joining());
    }

    @Nonnull default String relationshipAsString(@Nonnull Relationship relationship) {
        List<String> relationshipAsString = new ArrayList<>();
        relationshipAsString.add(COLON + escape(relationship.type()));
        relationshipAsString.add(mapAsString(relationship.asMap(this::formatValue)));

        return "[" + joinWithSpace(relationshipAsString) + "]";
    }

    @Nonnull default String nodeAsString(@Nonnull final Node node) {
        List<String> nodeAsString = new ArrayList<>();
        nodeAsString.add(collectNodeLabels(node));
        nodeAsString.add(mapAsString(node.asMap(this::formatValue)));

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

    @Nonnull static String mapAsString(@Nonnull Map<String, Object> map) {
        if (map.isEmpty()) {
            return "";
        }
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
        if (summary.hasProfile()) result.put("DbHits", Values.value( summary.profile().dbHits() ));
        if (summary.hasProfile()) result.put("Rows", Values.value( summary.profile().records() ));
        return result;
    }

}
