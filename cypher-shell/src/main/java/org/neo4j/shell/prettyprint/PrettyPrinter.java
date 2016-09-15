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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Print the result from neo4j in a intelligible fashion.
 */
public class PrettyPrinter {
    private static final String COMMA_SEPARATOR = ", ";

    private static final String COLON_SEPARATOR = ": ";
    private static final String COLON = ":";
    private static final String SPACE = " ";
    public static final String BACKTICK = "`";
    private static final Pattern ALPHA_NUMERIC = Pattern.compile("^[a-zA-Z0-9_]*$");
    private StatisticsCollector statisticsCollector;

    public PrettyPrinter(@Nonnull Format format) {
        this.statisticsCollector = new StatisticsCollector(format);
    }

    public String format(@Nonnull final BoltResult result) {
        StringBuilder sb = new StringBuilder();
        List<Record> records = result.getRecords();
        if (!records.isEmpty()) {
            sb.append(records.get(0).keys().stream().collect(Collectors.joining(COMMA_SEPARATOR)));
            sb.append("\n");
            sb.append(records.stream().map(this::formatRecord).collect(Collectors.joining("\n")));
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

    private String formatRecord(@Nonnull final Record record) {
        return record.values().stream().map(this::formatValue).collect(Collectors.joining(COMMA_SEPARATOR));
    }

    @Nonnull
    private String formatValue(@Nonnull final Value value) {
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

    private String pathAsString(Path path) {
        List<String> list = new LinkedList<>();

        if (path.start() != null) {
            list.add(nodeAsString(path.start()));
        }

        path.iterator().forEachRemaining(segment -> {
            if (segment.relationship() != null) {
                list.add("-" + relationshipAsString(segment.relationship()) + "->");
            }
            if (segment.end() != null) {
                list.add(nodeAsString(segment.end()));
            }
        });
        return list.stream().collect(Collectors.joining());
    }

    private String listAsString(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        sb.append(list.stream().collect(Collectors.joining(COMMA_SEPARATOR)));
        return sb.append("]").toString();
    }

    private String mapAsString(Map<String, Object> map) {
        if (map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("{");
        sb.append(
                map.entrySet().stream()
                        .map(e -> escapeIfNeeded(e.getKey()) + COLON_SEPARATOR + e.getValue())
                        .collect(Collectors.joining(COMMA_SEPARATOR)));
        return sb.append("}").toString();
    }

    private String relationshipAsString(Relationship relationship) {
        List<String> relationshipAsString = new ArrayList<>();
        relationshipAsString.add(COLON + escapeIfNeeded(relationship.type()));
        relationshipAsString.add(mapAsString(relationship.asMap(this::formatValue)));

        return "[" + joinWithSpace(relationshipAsString) + "]";
    }

    private String nodeAsString(@Nonnull final Node node) {
        List<String> nodeAsString = new ArrayList<>();
        nodeAsString.add(collectNodeLabels(node));
        nodeAsString.add(mapAsString(node.asMap(this::formatValue)));

        return "(" + joinWithSpace(nodeAsString) + ")";
    }

    private String collectNodeLabels(@Nonnull Node node) {
        StringBuilder sb = new StringBuilder();
        node.labels().forEach(label -> sb.append(COLON).append(escapeIfNeeded(label)));
        return sb.toString();
    }

    private String joinWithSpace(List<String> strings) {
        return strings.stream().filter(str -> isNotBlank(str)).collect(Collectors.joining(SPACE));
    }

    private static boolean isNotBlank(String string) {
        return string != null && !string.trim().isEmpty();
    }

    private String escapeIfNeeded(String string) {
        Matcher matcher = ALPHA_NUMERIC.matcher(string);
        if (!matcher.matches()) {
            return BACKTICK + string + BACKTICK;
        }
        return string;
    }

}
