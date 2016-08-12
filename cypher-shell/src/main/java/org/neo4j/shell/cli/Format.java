package org.neo4j.shell.cli;

public enum Format {
    // Intended for human consumption
    VERBOSE,
    // Intended for machine consumption (nothing except data is printed
    PLAIN;
    // TODO JSON, strictly intended for machine consumption with data formatted in JSON

    public static Format parse(String format) {
        if (format.equalsIgnoreCase(PLAIN.name())) {
            return PLAIN;
        }
        return VERBOSE;
    }
}
