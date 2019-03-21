package org.neo4j.shell.exception;

import org.neo4j.shell.log.AnsiFormattedText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * And exception indicating that a command invocation failed.
 */
public class CommandException extends AnsiFormattedException {
    public CommandException(@Nullable String msg) {
        super(msg);
    }

    public CommandException(@Nullable String msg, Throwable cause) {
        super(msg, cause);
    }

    public CommandException(@Nonnull AnsiFormattedText append) {
        super(append);
    }
}
