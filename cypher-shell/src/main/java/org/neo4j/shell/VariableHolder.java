package org.neo4j.shell;

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
    Optional set(@Nonnull String name, @Nonnull String valueString);

    /**
     *
     * @return map of all currently set variables and their values
     */
    @Nonnull
    Map<String, Object> getAll();

    /**
     *
     * @param name of variable to delete
     */
    Optional remove(@Nonnull String name);
}
