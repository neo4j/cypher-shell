package org.neo4j.shell.exception;

/**
 * An action that takes no parameters and returns no values, but may have a side-effect and may throw an exception.
 *
 * @param <E> The type of exception this action may throw.
 */
@FunctionalInterface
public interface ThrowingAction<E extends Exception>
{
    /**
     * Apply the action for some or all of its side-effects to take place, possibly throwing an exception.
     *
     * @throws E the exception that performing this action may throw.
     */
    void apply() throws E;
}
