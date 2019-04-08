package org.neo4j.shell.exception;

import org.neo4j.shell.log.AnsiFormattedText;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A type of exception where the message can formatted with Ansi codes.
 */
public class AnsiFormattedException extends Exception {
    private final AnsiFormattedText message;

    public AnsiFormattedException(@Nullable String message) {
        super(message);
        this.message = AnsiFormattedText.from(message);
    }

    public AnsiFormattedException(@Nullable String message, Throwable cause) {
        super(message, cause);
        this.message = AnsiFormattedText.from(message);
    }

    public AnsiFormattedException(@Nonnull AnsiFormattedText message) {
        super(message.plainString());
        this.message = message;
    }

    @Nonnull
    public AnsiFormattedText getFormattedMessage() {
        return message;
    }
}
