package org.neo4j.shell;

import org.neo4j.shell.exception.CommandException;
import org.neo4j.shell.state.ParamValue;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

/**
 * An object which keeps variables and allows them them to be set/unset.
 */
public interface VariableHolder {
    /**
     *  @param name of variable to set value for
     * @param valueString to interpret the value from
     */
    Optional set(@Nonnull String name, @Nonnull String valueString) throws CommandException;

    /**
     *
     * @return map of all currently set variables and their values
     */
    @Nonnull
    Map<String, Object> getAll();

    /**
     * @return map of all currently set variables and their values corresponding to the user valueString
     */
    @Nonnull
    Map<String, ParamValue> getAllAsUserInput();
}
