package org.neo4j.shell.prettyprint;

import org.neo4j.driver.internal.types.TypeRepresentation;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.shell.state.BoltResult;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

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

    static boolean isNotBlank(String string) {
        return string != null && !string.trim().isEmpty();
    }

    @Nonnull static String repeat(char c, int width) {
        char[] chars = new char[width];
        Arrays.fill(chars, c);
        return String.valueOf(chars);
    }

    @Nonnull static String rightPad(@Nonnull String str, int wantedSize) {
        int actualSize = str.length();
        if (actualSize > wantedSize) {
            return str.substring(0, wantedSize);
        } else if (actualSize < wantedSize) {
            return str + repeat(' ', wantedSize - actualSize);
        } else {
            return str;
        }
    }
}
