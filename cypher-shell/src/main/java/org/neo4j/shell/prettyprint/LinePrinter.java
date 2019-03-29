package org.neo4j.shell.prettyprint;

/**
 * Prints lines.
 */
@FunctionalInterface
public interface LinePrinter {
    void println( String line );
}
