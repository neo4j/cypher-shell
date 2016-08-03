package org.neo4j.shell.exception;

import org.neo4j.shell.log.AnsiFormattedText;

import javax.annotation.Nonnull;

/**
 * Utility functions with regards to exception messages
 */
public class Helper {
    @Nonnull
    private static Throwable getRootCause(@Nonnull final Throwable th) {
        Throwable cause = th;
        while(cause.getCause() != null )
        {
            cause = cause.getCause();
        }
        return cause;
    }


    /**
     * Interpret the cause of a Bolt exception and translate it into a sensible error message.
     */
    @Nonnull
    public static String getFormattedMessage(@Nonnull final Throwable e) {
        AnsiFormattedText msg = AnsiFormattedText.s().colorRed();
        final Throwable cause;

        if (e instanceof CypherSyntaxError) {
            cause = e;
        } else {
            //noinspection ThrowableResultOfMethodCallIgnored
            cause = getRootCause(e);
        }

        if (cause instanceof AnsiFormattedException) {
            msg = msg.append(((AnsiFormattedException) cause).getFormattedMessage());
        } else {
            if (cause.getMessage() != null ){
                msg = msg.append(cause.getMessage());
            } else {
                msg = msg.append(cause.getClass().getSimpleName());
            }
        }

        return msg.formattedString();
    }
}
