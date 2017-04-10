package org.neo4j.shell.prettyprint;

import org.neo4j.driver.internal.types.TypeRepresentation;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.shell.state.BoltResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import static org.neo4j.shell.prettyprint.CypherVariablesFormatter.escape;

/**
 * @author mh
 * @since 09.04.17
 */
public abstract class OutputFormatter {

    protected static final String COMMA_SEPARATOR = ", ";
    protected static final String COLON_SEPARATOR = ": ";
    protected static final String COLON = ":";
    protected static final String SPACE = " ";
    protected static final String NEWLINE =  System.getProperty("line.separator");

    public abstract String format(@Nonnull BoltResult result);

    @Nonnull
    String formatValue(@Nonnull final Value value) {
        if (value==null) return "null";
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
        Node lastTraversed = path.start();
        if (lastTraversed != null) {
            list.add(nodeAsString(lastTraversed));
        }

        for (Path.Segment segment : path) {
            Relationship relationship = segment.relationship();
            if (relationship.startNodeId() == lastTraversed.id()) {
                //-[:r]->
                list.add("-" + relationshipAsString(relationship) + "->");
                list.add(nodeAsString(segment.end()));
                lastTraversed = segment.start();
            } else {
                list.add("<-" + relationshipAsString(relationship) + "-");
                list.add(nodeAsString(segment.end()));
                lastTraversed = segment.end();
            }
        }

        return list.stream().collect(Collectors.joining());
    }

    private String listAsString(List<String> list) {
        return list.stream().collect(Collectors.joining(COMMA_SEPARATOR,"[","]"));
    }

    private String mapAsString(Map<String, Object> map) {
        if (map.isEmpty()) {
            return "";
        }
        return map.entrySet().stream()
                        .map(e -> escape(e.getKey()) + COLON_SEPARATOR + e.getValue())
                        .collect(Collectors.joining(COMMA_SEPARATOR,"{","}"));
    }

    private String relationshipAsString(Relationship relationship) {
        List<String> relationshipAsString = new ArrayList<>();
        relationshipAsString.add(COLON + escape(relationship.type()));
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
        node.labels().forEach(label -> sb.append(COLON).append(escape(label)));
        return sb.toString();
    }

    private String joinWithSpace(List<String> strings) {
        return strings.stream().filter(OutputFormatter::isNotBlank).collect(Collectors.joining(SPACE));
    }

    private static boolean isNotBlank(String string) {
        return string != null && !string.trim().isEmpty();
    }
}
