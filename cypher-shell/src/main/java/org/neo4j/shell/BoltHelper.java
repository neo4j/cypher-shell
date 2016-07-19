package org.neo4j.shell;

import javax.annotation.Nonnull;
import java.security.cert.CertificateException;

/**
 * Utility functions with regards to communication with Neo4j
 */
public class BoltHelper {
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
    public static String getSensibleMsg(@Nonnull final Throwable e) {
        final String msg;
        Throwable cause = getRootCause(e);

        if (cause instanceof CertificateException) {
            // These seem to have sensible error messages in them
            msg = cause.getMessage();
        } else {
            msg = cause.getMessage();
        }

        return msg;
    }
}
