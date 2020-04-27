package org.neo4j.shell.cli;

import javax.annotation.Nonnull;

public enum Encryption
{
    TRUE,
    FALSE,
    DEFAULT;

    public static Encryption parse( @Nonnull String format) {
        if (format.equalsIgnoreCase(TRUE.name())) {
            return TRUE;
        } else if (format.equalsIgnoreCase( FALSE.name() )) {
            return FALSE;
        } else {
            return DEFAULT;
        }
    }
}
