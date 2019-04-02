package org.neo4j.shell.prettyprint;

/**
 * Prints lines.
 */
@FunctionalInterface
public interface LinePrinter {

    /**
     * Print the designated line to configured output stream.
     *
     * @param line to print to the output stream
     */
    void printOut(String line );
}
