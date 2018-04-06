package org.neo4j.shell.state;

/**
 * Handles queryparams value and user inputString
 */
public class ParamValue {
    private final String valueAsString;
    private final Object value;

    public ParamValue(String valueAsString, Object value) {
        this.valueAsString = valueAsString;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        return valueAsString;
    }
}
