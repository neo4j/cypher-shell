package org.neo4j.shell.prettyprint;

import org.neo4j.driver.internal.types.TypeRepresentation;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.shell.cli.Format;
import org.neo4j.shell.state.BoltResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Print the result from neo4j in a intelligible fashion.
 */
public class PrettyPrinter {

    public static final String COMMA_SEPARATOR = ", ";
    private static final String COLON_SEPARATOR = ": ";
    public static final String COLON = ":";
    private StatisticsCollector statisticsCollector;

    public PrettyPrinter(@Nonnull Format format) {
        this.statisticsCollector = new StatisticsCollector(format);
    }

    public String format(@Nonnull final BoltResult result) {
        StringBuilder sb = new StringBuilder();
        List<Record> records = result.getRecords();
        if (!records.isEmpty()) {
            // TODO respect format
            sb.append(records.get(0).keys().stream().collect(Collectors.joining(COMMA_SEPARATOR)));
            sb.append("\n");
            sb.append(records.stream().map(PrettyPrinter::format).collect(Collectors.joining("\n")));
        }

        String statistics = statisticsCollector.collect(result.getSummary());
        if (!statistics.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(statistics);
        }
        return sb.toString();
    }

    private static String format(@Nonnull final Record record) {
        return record.values().stream().map(PrettyPrinter::toString).collect(Collectors.joining(COMMA_SEPARATOR));
    }

    @Nonnull
    private static String toString(@Nonnull final Value value) {
        TypeRepresentation type = (TypeRepresentation) value.type();
        switch (type.constructor()) {
            case LIST_TyCon:
                return toString(value.asList(PrettyPrinter::toString));
            case MAP_TyCon:
                return toString(value.asMap(PrettyPrinter::toString));
            case NODE_TyCon:
                return toString(value.asNode());
            case RELATIONSHIP_TyCon:
                return toString(value.asRelationship());
            case PATH_TyCon:
                return toString(value.asPath());
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

    private static String toString(Path path) {
        List<String> list = new LinkedList<>();

        if (path.start() != null) {
            list.add(toString(path.start()));
        }

        path.iterator().forEachRemaining(segment -> {
            if (segment.relationship() != null) {
                list.add(toString(segment.relationship()));
            }
            if (segment.end() != null) {
                list.add(toString(segment.end()));
            }
        });
        return toString(list);
    }

    private static String toString(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        sb.append(list.stream().collect(Collectors.joining(COMMA_SEPARATOR)));
        return sb.append("]").toString();
    }

    private static String toString(Map<String, Object> map) {
        if (map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("{");
        sb.append(
                map.keySet().stream()
                        .map(e -> e + COLON_SEPARATOR + map.get(e))
                        .collect(Collectors.joining(COMMA_SEPARATOR)));
        return sb.append("}").toString();
    }

    private static String toString(Relationship relationship) {

        String type = COLON + relationship.type();

        List<String> relationshipAsString = new ArrayList<>();
        relationshipAsString.add(type);
        relationshipAsString.add(toString(relationship.asMap(PrettyPrinter::toString)));

        return "[" +
                relationshipAsString.stream().filter(str -> isNotBlank(str)).collect(Collectors.joining(" ")) +
                "]";
    }

    private static String toString(@Nonnull final Node node) {
        StringBuilder sb = new StringBuilder();
        node.labels().forEach(label -> sb.append(COLON).append(label));

        List<String> nodeAsString = new ArrayList<>();
        nodeAsString.add(sb.toString());
        nodeAsString.add(toString(node.asMap(PrettyPrinter::toString)));

        return "(" +
                nodeAsString.stream().filter(str -> isNotBlank(str)).collect(Collectors.joining(" ")) +
                ")";
    }

    private static boolean isNotBlank(String string) {
        return string != null && !string.trim().isEmpty();
    }
}
