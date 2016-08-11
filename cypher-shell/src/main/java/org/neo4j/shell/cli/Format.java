package org.neo4j.shell.cli;

public enum Format {
    VERBOSE,
    PLAIN;

    public static Format parse(String format) {
        if (format.equalsIgnoreCase(PLAIN.name())) {
            return PLAIN;
        }
        return VERBOSE;
    }
}
