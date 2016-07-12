package org.neo4j.shell.prettyprint;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.exceptions.value.Uncoercible;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Print the result from neo4j in a intelligible fashion.
 */
public class PrettyPrinter {
    public static String format(@Nonnull final StatementResult result) {
        // TODO: 6/22/16 Format nicely
        if (!result.hasNext()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String key: result.keys()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(key);
        }

        while (result.hasNext()) {
            sb.append("\n").append(format(result.next()));
        }
        return sb.toString();
    }

    private static String format(@Nonnull final Record record) {
        // TODO: 6/22/16 Format nicely
        StringBuilder sb = new StringBuilder();
        for (Value value: record.values()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(toString(value));
        }

        return sb.toString();
    }

    @Nonnull
    private static String toString(@Nonnull final Value value) {
        try {
            return toString(value.asList(PrettyPrinter::toString));
        } catch (Uncoercible ignored) {//NOPMD
        }
        try {
            return toString(value.asMap(PrettyPrinter::toString));
        } catch (Uncoercible ignored) {//NOPMD
        }
        try {
            return toString(value.asNode());
        } catch (Uncoercible ignored) {//NOPMD
        }
        try {
            return toString(value.asRelationship());
        } catch (Uncoercible ignored) {//NOPMD
        }
        try {
            return toString(value.asPath());
        } catch (Uncoercible ignored) {//NOPMD
        }

        return value.toString();
    }

    private static String toString(Path path) {
        List<String> list = new LinkedList<>();
        path.iterator().forEachRemaining(segment -> {
            if (segment.start() != null) {
                list.add(toString(segment.start()));
            }
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

        for (String item : list) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(item);
        }

        return sb.append("]").toString();
    }

    private static String toString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");

        for (String key : map.keySet()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(key).append(": ").append(map.get(key));
        }

        return sb.append("}").toString();
    }

    private static String toString(Relationship relationship) {
        return toString(relationship.asMap(PrettyPrinter::toString));
    }

    private static String toString(@Nonnull final Node node) {
        return toString(node.asMap(PrettyPrinter::toString));
    }
}
