package org.neo4j.shell;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * An object which keeps a record of past commands
 */
public interface Historian {
    /**
     *
     * @return a list of all past commands in the history, in order of execution (first command sorted first).
     */
    @Nonnull
    List<String> getHistory();

    Historian empty = new Historian() {
        @Nonnull
        @Override
        public List<String> getHistory() {
            return new ArrayList<>();
        }
    };
}
