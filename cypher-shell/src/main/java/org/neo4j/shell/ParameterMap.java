package org.neo4j.shell;

import org.neo4j.cypher.internal.evaluator.EvaluationException;
import org.neo4j.shell.state.ParamValue;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * An object which keeps named parameters and allows them them to be set/unset.
 */
public interface ParameterMap {
    /**
     * @param name of variable to set value for
     * @param valueString to interpret the value from
     * @return the evaluated value
     */
    Object setParameter(@Nonnull String name, @Nonnull String valueString) throws EvaluationException;

    /**
     * @return map of all currently set variables and their values
     */
    @Nonnull
    Map<String, Object> allParameterValues();

    /**
     * @return map of all currently set variables and their values corresponding to the user valueString
     */
    @Nonnull
    Map<String, ParamValue> getAllAsUserInput();
}
