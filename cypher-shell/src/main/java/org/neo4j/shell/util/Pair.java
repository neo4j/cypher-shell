package org.neo4j.shell.util;

import javax.annotation.Nonnull;

/**
 * A pair of objects
 */
public class Pair<A, B> {
    private final A first;
    private final B second;

    public Pair(@Nonnull A first, @Nonnull B second) {
        this.first = first;
        this.second = second;
    }

    @Nonnull
    public A getFirst() {
        return first;
    }

    @Nonnull
    public B getSecond() {
        return second;
    }
}
