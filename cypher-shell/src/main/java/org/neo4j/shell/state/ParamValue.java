package org.neo4j.shell.state;

import java.util.Objects;

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

    @Override
    public String toString()
    {
        return "ParamValue{" +
               "valueAsString='" + valueAsString + '\'' +
               ", value=" + value +
               '}';
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        ParamValue that = (ParamValue) o;
        return valueAsString.equals( that.valueAsString ) &&
               Objects.equals( value, that.value );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( valueAsString, value );
    }
}
